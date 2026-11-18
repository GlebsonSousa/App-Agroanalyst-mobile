const malha = require('./data/malha.json');
const { haversineKm } = require('./frete');

const OSRM = 'https://router.project-osrm.org/route/v1/driving/';
const AGENTE = 'AgroAnalystMobile/2.0 (projeto academico Fatec Maua)';
const UM_DIA = 24 * 60 * 60 * 1000;
const LIMITE_ACESSO_KM = 300;
const LIGACAO_KM = 0.6;
const cache = new Map();

const r2 = (n) => Math.round(n * 100) / 100;
const r4 = (n) => Math.round(n * 1e4) / 1e4;

function lido(chave) {
  const c = cache.get(chave);
  return c && Date.now() - c.em < UM_DIA ? c.valor : null;
}
function lembrar(chave, valor) {
  if (cache.size > 500) cache.delete(cache.keys().next().value);
  cache.set(chave, { valor, em: Date.now() });
  return valor;
}

async function rotaRodovia(a, b) {
  const chave = `${a.lat.toFixed(3)},${a.lon.toFixed(3)};${b.lat.toFixed(3)},${b.lon.toFixed(3)}`;
  const guardado = lido(chave);
  if (guardado) return guardado;
  const r = await fetch(`${OSRM}${a.lon},${a.lat};${b.lon},${b.lat}?overview=simplified&geometries=geojson`, {
    headers: { 'User-Agent': AGENTE }, signal: AbortSignal.timeout(12000)
  });
  if (!r.ok) throw new Error(`OSRM HTTP ${r.status}`);
  const rota = (await r.json()).routes?.[0];
  if (!rota) throw new Error('OSRM sem rota');
  return lembrar(chave, {
    tipo: 'rodovia', km: r2(rota.distance / 1000),
    pontos: rota.geometry.coordinates.map(([x, y]) => [r4(x), r4(y)])
  });
}

function construirGrafo(tipo) {
  const ids = new Map(), nos = [], adj = [];
  const no = (x, y) => {
    const k = `${Math.round(x * 1000)},${Math.round(y * 1000)}`;
    let i = ids.get(k);
    if (i === undefined) { i = nos.length; ids.set(k, i); nos.push([x, y]); adj.push([]); }
    return i;
  };
  const ligar = (a, b, km) => { adj[a].push([b, km]); adj[b].push([a, km]); };
  for (const l of malha) {
    if (l.tipo !== tipo) continue;
    for (let i = 1; i < l.pontos.length; i++) {
      const a = no(...l.pontos[i - 1]), b = no(...l.pontos[i]);
      if (a !== b) ligar(a, b, haversineKm(nos[a][1], nos[a][0], nos[b][1], nos[b][0]));
    }
  }
  const celulas = new Map();
  nos.forEach(([x, y], i) => {
    const k = `${Math.floor(x / 0.01)},${Math.floor(y / 0.01)}`;
    (celulas.get(k) ?? celulas.set(k, []).get(k)).push(i);
  });
  nos.forEach(([x, y], i) => {
    const cx = Math.floor(x / 0.01), cy = Math.floor(y / 0.01);
    for (let dx = -1; dx <= 1; dx++) for (let dy = -1; dy <= 1; dy++) {
      for (const j of celulas.get(`${cx + dx},${cy + dy}`) ?? []) {
        if (j <= i) continue;
        const km = haversineKm(y, x, nos[j][1], nos[j][0]);
        if (km <= LIGACAO_KM && !adj[i].some(([n]) => n === j)) ligar(i, j, km);
      }
    }
  });
  return { nos, adj };
}

const grafos = {};
const grafo = (tipo) => (grafos[tipo] ??= construirGrafo(tipo));

function maisProximo(g, p) {
  let melhor = -1, menor = Infinity;
  const k = Math.cos((p.lat * Math.PI) / 180);
  g.nos.forEach(([x, y], i) => {
    const d = (x - p.lon) ** 2 * k * k + (y - p.lat) ** 2;
    if (d < menor) { menor = d; melhor = i; }
  });
  if (melhor < 0) return null;
  const [x, y] = g.nos[melhor];
  return { i: melhor, lon: x, lat: y, km: haversineKm(p.lat, p.lon, y, x) };
}

function caminho(g, origem, destino) {
  const dist = new Float64Array(g.nos.length).fill(Infinity);
  const anterior = new Int32Array(g.nos.length).fill(-1);
  const heap = [[0, origem]];
  dist[origem] = 0;
  const subir = (i) => { while (i > 0) { const p = (i - 1) >> 1; if (heap[p][0] <= heap[i][0]) break; [heap[p], heap[i]] = [heap[i], heap[p]]; i = p; } };
  const descer = (i) => {
    for (;;) {
      let m = i; const e = 2 * i + 1, d = e + 1;
      if (e < heap.length && heap[e][0] < heap[m][0]) m = e;
      if (d < heap.length && heap[d][0] < heap[m][0]) m = d;
      if (m === i) break;
      [heap[m], heap[i]] = [heap[i], heap[m]]; i = m;
    }
  };
  while (heap.length) {
    const [d, u] = heap[0];
    const ultimo = heap.pop();
    if (heap.length) { heap[0] = ultimo; descer(0); }
    if (d > dist[u]) continue;
    if (u === destino) break;
    for (const [v, km] of g.adj[u]) {
      if (d + km < dist[v]) { dist[v] = d + km; anterior[v] = u; heap.push([dist[v], v]); subir(heap.length - 1); }
    }
  }
  if (!Number.isFinite(dist[destino])) return null;
  const pontos = [];
  for (let n = destino; n !== -1; n = anterior[n]) pontos.push(g.nos[n]);
  return { km: dist[destino], pontos: pontos.reverse() };
}

async function acesso(de, para, fatorRota) {
  if (haversineKm(de.lat, de.lon, para.lat, para.lon) < 0.5) return null;
  try {
    return await rotaRodovia(de, para);
  } catch {
    return { tipo: 'rodovia', km: r2(haversineKm(de.lat, de.lon, para.lat, para.lon) * fatorRota), estimada: true, pontos: [[de.lon, de.lat], [para.lon, para.lat]] };
  }
}

async function rotaReal(origem, destino, modal, fatorRota = 1.3) {
  if (modal === 'rodoviario') {
    const p = await rotaRodovia(origem, destino);
    return { modal, pernas: [p], distanciaKm: p.km };
  }
  const tipo = modal === 'ferroviario' ? 'ferrovia' : 'hidrovia';
  const g = grafo(tipo);
  const o = maisProximo(g, origem), d = maisProximo(g, destino);
  if (!o || !d || o.km > LIMITE_ACESSO_KM || d.km > LIMITE_ACESSO_KM || o.i === d.i) return null;
  const trecho = caminho(g, o.i, d.i);
  if (!trecho) return null;
  const pernas = [];
  const entrada = await acesso(origem, o, fatorRota);
  if (entrada) pernas.push(entrada);
  pernas.push({ tipo, km: r2(trecho.km), pontos: trecho.pontos.map(([x, y]) => [r4(x), r4(y)]) });
  const saida = await acesso(d, destino, fatorRota);
  if (saida) pernas.push(saida);
  return { modal, pernas, distanciaKm: r2(pernas.reduce((s, p) => s + p.km, 0)) };
}

module.exports = { rotaReal, grafo, maisProximo, caminho };
