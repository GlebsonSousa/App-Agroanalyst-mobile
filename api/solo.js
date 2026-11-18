const ORIGEM = 'https://meteoserver-stfv.onrender.com/solo';
const TRINTA_DIAS = 30 * 24 * 60 * 60 * 1000;
const DOIS_MINUTOS = 2 * 60 * 1000;
const emAndamento = new Map();
const falhas = new Map();

const chave = (lat, lon) => `${lat.toFixed(2)},${lon.toFixed(2)}`;

async function buscarNoMeteoserver(lat, lon) {
  let ultimo;
  for (let i = 0; i < 2; i++) {
    try {
      const r = await fetch(`${ORIGEM}?lat=${lat.toFixed(2)}&lon=${lon.toFixed(2)}`, { signal: AbortSignal.timeout(280000) });
      if (r.ok) return await r.json();
      ultimo = new Error(`meteoserver HTTP ${r.status}`);
    } catch (e) {
      ultimo = e;
    }
  }
  throw ultimo;
}

function iniciar(db, k, lat, lon) {
  if (emAndamento.has(k)) return;
  const tarefa = buscarNoMeteoserver(lat, lon)
    .then((dados) => db.collection('solos').replaceOne({ _id: k }, { _id: k, dados, buscadoEm: new Date() }, { upsert: true }))
    .then(() => falhas.delete(k))
    .catch((e) => falhas.set(k, { mensagem: e.cause?.code ?? e.message, em: Date.now() }))
    .finally(() => emAndamento.delete(k));
  emAndamento.set(k, tarefa);
}

async function solo(db, lat, lon) {
  const k = chave(lat, lon);
  const doc = await db.collection('solos').findOne({ _id: k });
  if (doc && Date.now() - new Date(doc.buscadoEm).getTime() < TRINTA_DIAS) return { pronto: true, dados: doc.dados };
  const falha = falhas.get(k);
  if (falha && Date.now() - falha.em < DOIS_MINUTOS && !emAndamento.has(k)) return { erro: falha.mensagem };
  if (falha && !emAndamento.has(k)) falhas.delete(k);
  iniciar(db, k, lat, lon);
  return { pronto: false };
}

async function aquecerPontos(db, pontos) {
  for (const [lat, lon] of pontos) {
    const k = chave(lat, lon);
    if (await db.collection('solos').findOne({ _id: k })) continue;
    iniciar(db, k, lat, lon);
    await emAndamento.get(k);
  }
}

module.exports = { solo, aquecerPontos };
