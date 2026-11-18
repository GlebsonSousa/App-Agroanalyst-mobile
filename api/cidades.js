const UFS = {
  acre: 'AC', alagoas: 'AL', amapa: 'AP', amazonas: 'AM', bahia: 'BA', ceara: 'CE', 'distrito federal': 'DF',
  'espirito santo': 'ES', goias: 'GO', maranhao: 'MA', 'mato grosso': 'MT', 'mato grosso do sul': 'MS',
  'minas gerais': 'MG', para: 'PA', paraiba: 'PB', parana: 'PR', pernambuco: 'PE', piaui: 'PI',
  'rio de janeiro': 'RJ', 'rio grande do norte': 'RN', 'rio grande do sul': 'RS', rondonia: 'RO',
  roraima: 'RR', 'santa catarina': 'SC', 'sao paulo': 'SP', sergipe: 'SE', tocantins: 'TO'
};

const UMA_HORA = 60 * 60 * 1000;
const cache = new Map();
const esperar = (ms) => new Promise((r) => setTimeout(r, ms));
const semAcento = (t) => t.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();

async function consultar(q) {
  const url = `https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(q)}&count=8&language=pt&countryCode=BR&format=json`;
  let ultimo;
  for (let i = 0; i < 3; i++) {
    const r = await fetch(url, { signal: AbortSignal.timeout(10000) });
    if (r.ok) return r.json();
    ultimo = new Error(`Geocoding HTTP ${r.status}`);
    if (r.status !== 429 && r.status < 500) break;
    await esperar(1000 * (i + 1));
  }
  throw ultimo;
}

async function buscarCidades(q) {
  const chave = semAcento(q);
  const guardado = cache.get(chave);
  if (guardado && Date.now() - guardado.em < UMA_HORA) return guardado.valor;
  const d = await consultar(q);
  const valor = (d.results ?? []).map((r) => ({
    nome: r.name, uf: UFS[semAcento(r.admin1 ?? '')] ?? r.admin1 ?? null,
    lat: r.latitude, lon: r.longitude
  }));
  if (cache.size > 500) cache.delete(cache.keys().next().value);
  cache.set(chave, { valor, em: Date.now() });
  return valor;
}

module.exports = { buscarCidades };
