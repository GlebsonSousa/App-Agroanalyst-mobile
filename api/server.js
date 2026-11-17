try { process.loadEnvFile(); } catch {} // .env local (Node 20.12+); no Render vêm do ambiente
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const multer = require('multer');
const { MongoClient, ObjectId, GridFSBucket } = require('mongodb');
const { Readable } = require('stream');
const { calcularFrete, distanciaEstimadaKm } = require('./frete');

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
const str = (v) => typeof v === 'string' && v.trim() !== '';

// ---------- auth ----------
function auth(req, res, next) {
  const m = /^Bearer (.+)$/.exec(req.headers.authorization || '');
  try { req.usuarioId = new ObjectId(jwt.verify(m?.[1] ?? '', JWT_SECRET).sub); next(); }
  catch { erro(res, 401, 'Sessão inválida ou expirada'); }
}

app.get('/', (_, res) => res.json({ mensagem: 'AgroAnalyst Mobile API' })); // ping de aquecimento

app.post('/usuarios/registrar', wrap(async (req, res) => {
  const { nome, email, senha } = req.body ?? {};
  if (!str(nome) || !str(email) || !str(senha)) return erro(res, 400, 'Informe nome, e-mail e senha');
  if (senha.length < 6) return erro(res, 400, 'A senha precisa de pelo menos 6 caracteres');
  try {
    await db.collection('usuarios').insertOne({
      nome: nome.trim(), email: email.trim().toLowerCase(),
      senhaHash: await bcrypt.hash(senha, 10), criadoEm: new Date(),
    });
  } catch (e) {
    if (e.code === 11000) return erro(res, 409, 'E-mail já cadastrado');
    throw e;
  }
  res.status(201).json({ mensagem: 'Usuário cadastrado' });
}));

app.post('/usuarios/login', wrap(async (req, res) => {
  const { email, senha } = req.body ?? {};
  const u = str(email) && str(senha) && await db.collection('usuarios').findOne({ email: email.trim().toLowerCase() });
  if (!u || !(await bcrypt.compare(senha, u.senhaHash))) return erro(res, 401, 'E-mail ou senha incorretos');
  res.json({ token: jwt.sign({ sub: u._id.toString() }, JWT_SECRET, { expiresIn: '12h' }), nome: u.nome });
}));

// ---------- plantios ----------
const plantioDto = (p) => ({
  id: p._id.toString(), nome: p.nome, cultura: p.cultura, areaHa: p.areaHa,
  produtividadeSacasHa: p.produtividadeSacasHa, sacasEstimadas: p.areaHa * p.produtividadeSacasHa,
  lat: p.lat, lon: p.lon, fotoId: p.fotoId ?? null, criado_em: p.criadoEm.toISOString(),
});

function lerPlantio(b) {
  const { nome, cultura, areaHa, produtividadeSacasHa, lat, lon, fotoId } = b ?? {};
  if (!str(nome) || !str(cultura)) return 'Informe nome e cultura';
  if (!num(areaHa) || areaHa <= 0 || !num(produtividadeSacasHa) || produtividadeSacasHa <= 0) return 'Área e produtividade devem ser maiores que zero';
  if (!num(lat) || Math.abs(lat) > 90 || !num(lon) || Math.abs(lon) > 180) return 'Coordenada inválida';
  if (fotoId != null && !oid(fotoId)) return 'fotoId inválido';
  return { nome: nome.trim(), cultura: cultura.trim(), areaHa, produtividadeSacasHa, lat, lon, fotoId: fotoId ?? null };
}

const plantios = () => db.collection('plantios');
// Filtra sempre por usuarioId do JWT; id alheio ou malformado responde 404.
async function meuPlantio(req, id) { const o = oid(id); return o && plantios().findOne({ _id: o, usuarioId: req.usuarioId }); }

app.get('/plantios', auth, wrap(async (req, res) => {
  res.json((await plantios().find({ usuarioId: req.usuarioId }).sort({ criadoEm: -1 }).toArray()).map(plantioDto));
}));
app.post('/plantios', auth, wrap(async (req, res) => {
  const d = lerPlantio(req.body); if (typeof d === 'string') return erro(res, 400, d);
  const doc = { ...d, usuarioId: req.usuarioId, criadoEm: new Date() };
  doc._id = (await plantios().insertOne(doc)).insertedId;
  res.status(201).json(plantioDto(doc));
}));
app.get('/plantios/:id', auth, wrap(async (req, res) => {
  const p = await meuPlantio(req, req.params.id); p ? res.json(plantioDto(p)) : erro(res, 404, 'Plantio não encontrado');
}));
app.put('/plantios/:id', auth, wrap(async (req, res) => {
  const d = lerPlantio(req.body); if (typeof d === 'string') return erro(res, 400, d);
  const o = oid(req.params.id);
  const p = o && await plantios().findOneAndUpdate({ _id: o, usuarioId: req.usuarioId }, { $set: d }, { returnDocument: 'after' });
  p ? res.json(plantioDto(p)) : erro(res, 404, 'Plantio não encontrado');
}));
app.delete('/plantios/:id', auth, wrap(async (req, res) => {
  const o = oid(req.params.id);
  const r = o && await plantios().deleteOne({ _id: o, usuarioId: req.usuarioId });
  r?.deletedCount ? res.json({ mensagem: 'Plantio excluído' }) : erro(res, 404, 'Plantio não encontrado');
}));

// ---------- upload (GridFS) ----------
app.post('/upload', auth, upload.single('foto'), wrap(async (req, res) => {
  if (!req.file) return erro(res, 400, 'Envie a imagem na parte "foto"');
  if (!req.file.mimetype.startsWith('image/')) return erro(res, 400, 'O arquivo precisa ser uma imagem');
  const s = fotos.openUploadStream(req.file.originalname || 'foto', { contentType: req.file.mimetype });
  Readable.from(req.file.buffer).pipe(s).on('finish', () => res.status(201).json({ fileId: s.id.toString() }))
    .on('error', () => erro(res, 500, 'Falha ao gravar a foto'));
}));
app.get('/upload/:fileId', wrap(async (req, res) => {
  const o = oid(req.params.fileId);
  const f = o && (await fotos.find({ _id: o }).toArray())[0];
  if (!f) return erro(res, 404, 'Foto não encontrada');
  res.set('Content-Type', f.contentType || 'application/octet-stream');
  fotos.openDownloadStream(o).pipe(res);
}));

// ---------- destinos, grãos ----------
const destinoDto = (d) => ({ id: d._id.toString(), nome: d.nome, tipo: d.tipo, cidade: d.cidade, uf: d.uf, lat: d.lat, lon: d.lon, precoSaca: d.precoSaca, grao: d.grao });
app.get('/destinos', auth, wrap(async (_, res) => res.json((await db.collection('destinos').find().sort({ nome: 1 }).toArray()).map(destinoDto))));
app.get('/graos', auth, wrap(async (_, res) => res.json((await db.collection('graos').find().toArray()).map((g) => ({ id: g._id.toString(), nome: g.nome, kgPorSaca: g.kgPorSaca })))));

// ---------- frete e vendas ----------
const MODAIS = ['rodoviario', 'ferroviario', 'hidroviario'];
async function calcular(req, b) {
  const { plantioId, destinoId, sacas, precoSaca, modal, capacidadeT, kmPorLitro, eixos } = b ?? {};
  if (!Number.isInteger(sacas) || sacas <= 0 || !num(precoSaca) || precoSaca <= 0) return { status: 400, msg: 'Sacas e preço devem ser maiores que zero' };
  if (!MODAIS.includes(modal)) return { status: 400, msg: 'Modal inválido' };
  if (modal === 'rodoviario' && !(num(capacidadeT) && capacidadeT > 0 && num(kmPorLitro) && kmPorLitro > 0 && Number.isInteger(eixos) && eixos > 0))
    return { status: 400, msg: 'Informe capacidade, km/L e eixos do caminhão' };
  const p = await meuPlantio(req, plantioId);
  const o = oid(destinoId);
  const d = o && await db.collection('destinos').findOne({ _id: o });
  if (!p) return { status: 404, msg: 'Plantio não encontrado' };
  if (!d) return { status: 404, msg: 'Destino não encontrado' };
  const grao = await db.collection('graos').findOne({ nome: new RegExp(`^${d.grao}$`, 'i') });
  const r = calcularFrete({ distanciaKm: distanciaEstimadaKm(p, d), sacas, kgPorSaca: grao?.kgPorSaca ?? 60, precoSaca, modal, capacidadeT, kmPorLitro, eixos });
  return { r, p, d };
}
app.post('/frete/calcular', auth, wrap(async (req, res) => {
  const c = await calcular(req, req.body); c.r ? res.json(c.r) : erro(res, c.status, c.msg);
}));

const vendaDto = (v) => ({ id: v._id.toString(), plantioId: v.plantioId.toString(), destinoId: v.destinoId.toString(), destinoNome: v.destinoNome,
  destinoLat: v.destinoLat, destinoLon: v.destinoLon, sacas: v.sacas, precoSaca: v.precoSaca, modal: v.modal, distanciaKm: v.distanciaKm,
  duracaoH: v.duracaoH, viagens: v.viagens, freteTotal: v.freteTotal, receitaTotal: v.receitaTotal, margem: v.margem, status: v.status, criado_em: v.criadoEm.toISOString() });
const vendas = () => db.collection('vendas');
const STATUS = ['pendente', 'confirmada', 'entregue', 'cancelada'];

app.get('/vendas', auth, wrap(async (req, res) => res.json((await vendas().find({ usuarioId: req.usuarioId }).sort({ criadoEm: -1 }).toArray()).map(vendaDto))));
app.post('/vendas', auth, wrap(async (req, res) => {
  const c = await calcular(req, req.body); if (!c.r) return erro(res, c.status, c.msg);
  const { sacas, precoSaca, modal } = req.body;
  const doc = { usuarioId: req.usuarioId, plantioId: c.p._id, destinoId: c.d._id, destinoNome: c.d.nome, destinoLat: c.d.lat, destinoLon: c.d.lon,
    sacas, precoSaca, modal, ...c.r, status: 'pendente', criadoEm: new Date() };
  doc._id = (await vendas().insertOne(doc)).insertedId;
  res.status(201).json(vendaDto(doc));
}));
app.get('/vendas/:id', auth, wrap(async (req, res) => {
  const o = oid(req.params.id); const v = o && await vendas().findOne({ _id: o, usuarioId: req.usuarioId });
  v ? res.json(vendaDto(v)) : erro(res, 404, 'Venda não encontrada');
}));
app.patch('/vendas/:id/status', auth, wrap(async (req, res) => {
  if (!STATUS.includes(req.body?.status)) return erro(res, 400, `Status deve ser: ${STATUS.join(', ')}`);
  const o = oid(req.params.id);
  const v = o && await vendas().findOneAndUpdate({ _id: o, usuarioId: req.usuarioId }, { $set: { status: req.body.status } }, { returnDocument: 'after' });
  v ? res.json(vendaDto(v)) : erro(res, 404, 'Venda não encontrada');
}));

app.use((_, res) => erro(res, 404, 'Rota não encontrada'));

// ---------- subida + seed ----------
async function seed() {
  const g = db.collection('graos');
  for (const [nome, kgPorSaca] of [['soja', 60], ['milho', 60]]) await g.updateOne({ nome }, { $setOnInsert: { nome, kgPorSaca } }, { upsert: true });
  // Seis praças reais. precoSaca é um valor de referência único (R$ 128,40), não cotação.
  const d = db.collection('destinos');
  for (const x of [
    ['Porto de Santos', 'porto', 'Santos', 'SP', -23.9608, -46.3336],
    ['Porto de Paranaguá', 'porto', 'Paranaguá', 'PR', -25.5163, -48.5225],
    ['Porto de Rio Grande', 'porto', 'Rio Grande', 'RS', -32.035, -52.0986],
    ['Porto de Itaqui', 'porto', 'São Luís', 'MA', -2.5667, -44.3667],
    ['Porto de Vitória', 'porto', 'Vitória', 'ES', -20.3222, -40.338],
    ['Terminal de Rondonópolis', 'terminal', 'Rondonópolis', 'MT', -16.4673, -54.6372],
  ]) await d.updateOne({ nome: x[0] }, { $setOnInsert: { nome: x[0], tipo: x[1], cidade: x[2], uf: x[3], lat: x[4], lon: x[5], precoSaca: 128.4, grao: 'soja' } }, { upsert: true });
}

(async () => {
  const cli = await MongoClient.connect(MONGODB_URI);
  db = cli.db();
  fotos = new GridFSBucket(db, { bucketName: 'fotos' });
  await db.collection('usuarios').createIndex({ email: 1 }, { unique: true });
  await db.collection('plantios').createIndex({ usuarioId: 1 });
  await db.collection('vendas').createIndex({ usuarioId: 1 });
  await db.collection('graos').createIndex({ nome: 1 }, { unique: true });
  await seed();
  app.listen(PORT, () => console.log(`API na porta ${PORT}`));
})().catch((e) => { console.error(e); process.exit(1); });
