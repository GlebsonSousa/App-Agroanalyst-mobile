async function clima(lat, lon) {
  const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}` +
    '&daily=temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto&forecast_days=7';
  const r = await fetch(url, { signal: AbortSignal.timeout(15000) });
  if (!r.ok) throw new Error(`Open-Meteo HTTP ${r.status}`);
  const d = (await r.json()).daily;
  const dias = d.time.map((data, i) => ({
    data, max: d.temperature_2m_max[i], min: d.temperature_2m_min[i], chuva: d.precipitation_sum[i]
  }));
  return { hoje: dias[0], proximos: dias.slice(1) };
}

module.exports = { clima };
