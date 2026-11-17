try { process.loadEnvFile(); } catch {}
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const multer = require('multer');
const { Readable } = require('stream');
const { MongoClient, ObjectId, GridFSBucket } = require('mongodb');
const { calcularFrete, distanciaEstimadaKm } = require('./frete');
const { atualizarPrecos, listarPrecos, serie, PRODUTOS } = require('./cepea');
const { producaoUf, producaoTop } = require('./sidra');
const { clima } = require('./clima');
const { baixarApk } = require('./download');
const { semear } = require('./seed');

const { MONGODB_URI, JWT_SECRET, PORT = 3000 } = process.env;
if (!MONGODB_URI || !JWT_SECRET) { console.error('Defina MONGODB_URI e JWT_SECRET'); process.exit(1); }

const app = express();
app.use(cors());
app.use(express.json());
const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 10 * 1024 * 1024 } });

let db, fotos;
const erro = (res, status, mensagem) => res.status(status).json({ mensagem });
const wrap = (fn) => (req, res) => fn(req, res).catch((e) => { console.error(e); erro(res, 500, 'Erro interno do servidor'); });
const oid = (s) => (ObjectId.isValid(s) ? new ObjectId(s) : null);
const num = (v) => typeof v === 'number' && Number.isFinite(v);
const texto = (v) => typeof v === 'string' && v.trim() !== '';

function auth(req, res, next) {
  const m = /^Bearer (.+)$/.exec(req.headers.authorization || '');
  try { req.usuarioId = new ObjectId(jwt.verify(m?.[1] ?? '', JWT_SECRET).sub); next(); }
  catch { erro(res, 401, 'Sessão inválida ou expirada'); }
}

app.get('/', (_, res) => res.json({ mensagem: 'AgroAnalyst Mobile API' }));
app.get('/download', baixarApk);

// ----- usuários -----
app.post('/usuarios/registrar', wrap(async (req, res) => {
  const { nome, email, senha } = req.body ?? {};
  if (!texto(nome) || !texto(email) || !texto(senha)) return erro(res, 400, 'Informe nome, e-mail e senha');
  if (senha.length < 6) return erro(res, 400, 'A senha precisa de pelo menos 6 caracteres');
  try {
    await db.collection('usuarios').insertOne({
      nome: nome.trim(), email: email.trim().toLowerCase(),
      senhaHash: await bcrypt.hash(senha, 10), criadoEm: new Date()
    });
  } catch (e) {
    if (e.code === 11000) return erro(res, 409, 'E-mail já cadastrado');
    throw e;
  }
  res.status(201).json({ mensagem: 'Usuário cadastrado' });
}));

app.post('/usuarios/login', wrap(async (req, res) => {
  const { email, senha } = req.body ?? {};
  const u = texto(email) && texto(senha) && await db.collection('usuarios').findOne({ email: email.trim().toLowerCase() });
  if (!u || !(await bcrypt.compare(senha, u.senhaHash))) return erro(res, 401, 'E-mail ou senha incorretos');
  res.json({ token: jwt.sign({ sub: u._id.toString() }, JWT_SECRET, { expiresIn: '12h' }), nome: u.nome });
}));

// ----- públicos -----
app.get('/publico/precos', wrap(async (_, res) => res.json(await listarPrecos(db))));

app.get('/publico/precos/status', wrap(async (_, res) => {
  const s = (await db.collection('parametros').findOne({ _id: 'precos' })) ?? {};
  res.json({ stale: s.stale ?? {}, tentativa: s.tentativa ?? {}, erro: s.erro ?? {} });
}));

app.get('/publico/precos/:cultura/serie', wrap(async (req, res) => {
  if (!PRODUTOS[req.params.cultura]) return erro(res, 404, 'Cultura não encontrada');
  const dias = Math.min(Math.max(parseInt(req.query.dias, 10) || 30, 1), 90);
  res.json(await serie(db, req.params.cultura, dias));
}));

const culturaValida = (req, res) => {
  if (PRODUTOS[req.query.cultura]) return true;
  erro(res, 400, `Informe cultura: ${Object.keys(PRODUTOS).join(', ')}`);
  return false;
};
const falhaExterna = (res, e) => res.status(502).json({ mensagem: 'Fonte de dados indisponível no momento', detalhe: e?.cause?.code ?? e?.message });

app.get('/publico/producao', wrap(async (req, res) => {
  if (!culturaValida(req, res)) return;
  if ((req.query.nivel ?? 'uf') !== 'uf') return erro(res, 400, 'Nível suportado: uf');
  try { res.json(await producaoUf(db, req.query.cultura)); } catch (e) { falhaExterna(res, e); }
}));

app.get('/publico/producao/top', wrap(async (req, res) => {
  if (!culturaValida(req, res)) return;
  const n = Math.min(Math.max(parseInt(req.query.n, 10) || 5, 1), 20);
  try { res.json(await producaoTop(db, req.query.cultura, n)); } catch (e) { falhaExterna(res, e); }
}));

app.get('/publico/clima', wrap(async (req, res) => {
  const lat = Number(req.query.lat), lon = Number(req.query.lon);
  if (!Number.isFinite(lat) || !Number.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180) return erro(res, 400, 'Coordenada inválida');
  try { res.json(await clima(lat, lon)); } catch (e) { falhaExterna(res, e); }
}));

app.get('/publico/pracas', wrap(async (_, res) => {
  const lista = await db.collection('pracas').find().sort({ nome: 1 }).toArray();
  res.json(lista.map((p) => ({ nome: p.nome, tipo: p.tipo, uf: p.uf, lat: p.lat, lon: p.lon })));
}));

const MODAIS = ['rodoviario', 'ferroviario', 'hidroviario'];

async function calcular(b) {
  const { origemLat, origemLon, destino, grao, sacas, precoSaca, modal, capacidadeT, kmPorLitro, eixos } = b ?? {};
  if (!num(origemLat) || Math.abs(origemLat) > 90 || !num(origemLon) || Math.abs(origemLon) > 180) return { status: 400, msg: 'Coordenada de origem inválida' };
  if (!Number.isInteger(sacas) || sacas <= 0 || !num(precoSaca) || precoSaca <= 0) return { status: 400, msg: 'Sacas e preço devem ser maiores que zero' };
  if (!MODAIS.includes(modal)) return { status: 400, msg: 'Modal inválido' };
  if (modal === 'rodoviario' && !(num(capacidadeT) && capacidadeT > 0 && num(kmPorLitro) && kmPorLitro > 0 && Number.isInteger(eixos) && eixos > 0))
    return { status: 400, msg: 'Informe capacidade, km/L e eixos do caminhão' };
  const praca = texto(destino) && await db.collection('pracas').findOne({ nome: destino });
  if (!praca) return { status: 404, msg: 'Destino não encontrado' };
  const g = await db.collection('graos').findOne({ nome: grao ?? 'soja' });
  if (!g) return { status: 400, msg: 'Grão não suportado' };
  const p = await db.collection('parametros').findOne({ _id: 'frete' });
  const distanciaKm = distanciaEstimadaKm({ lat: origemLat, lon: origemLon }, praca, p.fatorRota);
  const resultado = calcularFrete(p, { distanciaKm, sacas, kgPorSaca: g.kgPorSaca, precoSaca, modal, capacidadeT, kmPorLitro, eixos });
  return { resultado, grao: g.nome };
}

app.post('/publico/frete/calcular', wrap(async (req, res) => {
  const c = await calcular(req.body);
  c.resultado ? res.json(c.resultado) : erro(res, c.status, c.msg);
}));

app.get('/upload/:fileId', wrap(async (req, res) => {
  const o = oid(req.params.fileId);
  const f = o && (await fotos.find({ _id: o }).toArray())[0];
  if (!f) return erro(res, 404, 'Foto não encontrada');
  res.set('Content-Type', f.contentType || 'application/octet-stream');
  fotos.openDownloadStream(o).pipe(res);
}));

// ----- privados -----
const plantios = () => db.collection('plantios');

const plantioDto = (p) => ({
  id: p._id.toString(), nome: p.nome, cultura: p.cultura, areaHa: p.areaHa,
  produtividadeSacasHa: p.produtividadeSacasHa, sacasEstimadas: p.areaHa * p.produtividadeSacasHa,
  lat: p.lat, lon: p.lon, fotoId: p.fotoId ?? null, criado_em: p.criadoEm.toISOString()
});

function lerPlantio(b) {
  const { nome, cultura, areaHa, produtividadeSacasHa, lat, lon, fotoId } = b ?? {};
  if (!texto(nome) || !texto(cultura)) return 'Informe nome e cultura';
  if (!num(areaHa) || areaHa <= 0 || !num(produtividadeSacasHa) || produtividadeSacasHa <= 0) return 'Área e produtividade devem ser maiores que zero';
  if (!num(lat) || Math.abs(lat) > 90 || !num(lon) || Math.abs(lon) > 180) return 'Coordenada inválida';
  if (fotoId != null && !oid(fotoId)) return 'fotoId inválido';
  return { nome: nome.trim(), cultura: cultura.trim(), areaHa, produtividadeSacasHa, lat, lon, fotoId: fotoId ?? null };
}

async function meuPlantio(req, id) {
  const o = oid(id);
  return o && plantios().findOne({ _id: o, usuarioId: req.usuarioId });
}

app.get('/plantios', auth, wrap(async (req, res) => {
  res.json((await plantios().find({ usuarioId: req.usuarioId }).sort({ criadoEm: -1 }).toArray()).map(plantioDto));
}));

app.post('/plantios', auth, wrap(async (req, res) => {
  const d = lerPlantio(req.body);
  if (typeof d === 'string') return erro(res, 400, d);
  const doc = { ...d, usuarioId: req.usuarioId, criadoEm: new Date() };
  doc._id = (await plantios().insertOne(doc)).insertedId;
  res.status(201).json(plantioDto(doc));
}));

app.get('/plantios/:id', auth, wrap(async (req, res) => {
  const p = await meuPlantio(req, req.params.id);
  p ? res.json(plantioDto(p)) : erro(res, 404, 'Plantio não encontrado');
}));

app.put('/plantios/:id', auth, wrap(async (req, res) => {
  const d = lerPlantio(req.body);
  if (typeof d === 'string') return erro(res, 400, d);
  const o = oid(req.params.id);
  const p = o && await plantios().findOneAndUpdate({ _id: o, usuarioId: req.usuarioId }, { $set: d }, { returnDocument: 'after' });
  p ? res.json(plantioDto(p)) : erro(res, 404, 'Plantio não encontrado');
}));

app.delete('/plantios/:id', auth, wrap(async (req, res) => {
  const o = oid(req.params.id);
  const r = o && await plantios().deleteOne({ _id: o, usuarioId: req.usuarioId });
  if (!r?.deletedCount) return erro(res, 404, 'Plantio não encontrado');
  await db.collection('fretes').deleteMany({ plantioId: o });
  res.json({ mensagem: 'Plantio excluído' });
}));

const freteDto = (f) => ({
  id: f._id.toString(), destino: f.destino, grao: f.grao, sacas: f.sacas, precoSaca: f.precoSaca,
  modal: f.modal, resultado: f.resultado, criado_em: f.criadoEm.toISOString()
});

app.post('/plantios/:id/fretes', auth, wrap(async (req, res) => {
  const p = await meuPlantio(req, req.params.id);
  if (!p) return erro(res, 404, 'Plantio não encontrado');
  const c = await calcular(req.body);
  if (!c.resultado) return erro(res, c.status, c.msg);
  const { destino, sacas, precoSaca, modal } = req.body;
  const doc = { plantioId: p._id, destino, grao: c.grao, sacas, precoSaca, modal, resultado: c.resultado, criadoEm: new Date() };
  doc._id = (await db.collection('fretes').insertOne(doc)).insertedId;
  res.status(201).json(freteDto(doc));
}));

app.get('/plantios/:id/fretes', auth, wrap(async (req, res) => {
  const p = await meuPlantio(req, req.params.id);
  if (!p) return erro(res, 404, 'Plantio não encontrado');
  res.json((await db.collection('fretes').find({ plantioId: p._id }).sort({ criadoEm: -1 }).toArray()).map(freteDto));
}));

app.post('/upload', auth, upload.single('foto'), wrap(async (req, res) => {
  if (!req.file) return erro(res, 400, 'Envie a imagem na parte "foto"');
  if (!req.file.mimetype.startsWith('image/')) return erro(res, 400, 'O arquivo precisa ser uma imagem');
  const s = fotos.openUploadStream(req.file.originalname || 'foto', { contentType: req.file.mimetype });
  Readable.from(req.file.buffer).pipe(s)
    .on('finish', () => res.status(201).json({ fileId: s.id.toString() }))
    .on('error', () => erro(res, 500, 'Falha ao gravar a foto'));
}));

app.use((err, req, res, next) => {
  if (err instanceof multer.MulterError) return erro(res, 400, 'Arquivo inválido ou grande demais');
  if (err.type === 'entity.parse.failed') return erro(res, 400, 'JSON inválido');
  console.error(err);
  erro(res, 500, 'Erro interno do servidor');
});

app.use((_, res) => erro(res, 404, 'Rota não encontrada'));

(async () => {
  const cli = await MongoClient.connect(MONGODB_URI);
  db = cli.db();
  fotos = new GridFSBucket(db, { bucketName: 'fotos' });
  await db.collection('usuarios').createIndex({ email: 1 }, { unique: true });
  await db.collection('plantios').createIndex({ usuarioId: 1 });
  await db.collection('fretes').createIndex({ plantioId: 1 });
  await db.collection('precos').createIndex({ cultura: 1, data: 1 }, { unique: true });
  await db.collection('producao').createIndex({ cultura: 1, nivel: 1 }, { unique: true });
  await db.collection('pracas').createIndex({ nome: 1 }, { unique: true });
  await db.collection('graos').createIndex({ nome: 1 }, { unique: true });
  await semear(db);
  app.listen(PORT, () => console.log(`API na porta ${PORT}`));
  const rodarRaspagem = () => atualizarPrecos(db).then((r) => console.log('precos', r)).catch((e) => console.error('precos', e.message));
  rodarRaspagem();
  setInterval(rodarRaspagem, 6 * 60 * 60 * 1000);
})().catch((e) => { console.error(e); process.exit(1); });
