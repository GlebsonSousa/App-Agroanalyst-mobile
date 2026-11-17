const MEIA_HORA = 30 * 60 * 1000;
const cache = new Map();

const esperar = (ms) => new Promise((r) => setTimeout(r, ms));

async function buscar(lat, lon) {
  const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}` +
    '&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto&forecast_days=7';
  let ultimo;
  for (let i = 0; i < 3; i++) {
    const r = await fetch(url, { signal: AbortSignal.timeout(15000) });
    if (r.ok) return r.json();
    ultimo = new Error(`Open-Meteo HTTP ${r.status}`);
    if (r.status !== 429 && r.status < 500) break;
    await esperar(1500 * (i + 1));
  }
  throw ultimo;
}

async function clima(lat, lon) {
  const chave = `${lat.toFixed(2)},${lon.toFixed(2)}`;
  const guardado = cache.get(chave);
  if (guardado && Date.now() - guardado.em < MEIA_HORA) return guardado.valor;
  try {
    const d = (await buscar(lat, lon)).daily;
    const dias = d.time.map((data, i) => ({
      data, max: d.temperature_2m_max[i], min: d.temperature_2m_min[i], chuva: d.precipitation_sum[i]
    }));
    const valor = { hoje: dias[0], proximos: dias.slice(1) };
    cache.set(chave, { valor, em: Date.now() });
    return valor;
  } catch (e) {
    if (guardado) return guardado.valor;
    throw e;
  }
}

module.exports = { clima };
