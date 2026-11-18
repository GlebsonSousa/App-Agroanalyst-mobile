const fs = require('fs');
const path = require('path');

const WFS = 'https://geoservicos.ibge.gov.br/geoserver/ows?service=WFS&version=1.0.0&request=GetFeature&outputFormat=application/json&srsName=EPSG:4326&typeName=';
const TOLERANCIA = 0.004;

const CAMADAS = [
  { tipo: 'ferrovia', camada: 'CCAR:BC250_2025_fer_trecho_ferroviario_l',
    serve: (p) => ['Trecho para trem', 'Desconhecido'].includes(p.tipotrechoferrov) &&
      !['Abandonada', 'Destruída', 'Em construção'].includes(p.situacaofisica) && p.operacional !== 'Não' },
  { tipo: 'hidrovia', camada: 'CCAR:BC250_2025_hdv_trecho_hidroviario_l', serve: (p) => p.operacional !== 'Não' && p.situacaofisica !== 'Destruída' }
];

function distanciaAoSegmento(p, a, b) {
  const dx = b[0] - a[0], dy = b[1] - a[1];
  const t = dx || dy ? Math.max(0, Math.min(1, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / (dx * dx + dy * dy))) : 0;
  return Math.hypot(p[0] - (a[0] + t * dx), p[1] - (a[1] + t * dy));
}

function simplificar(pts, tol) {
  if (pts.length < 3) return pts;
  const manter = new Uint8Array(pts.length);
  manter[0] = manter[pts.length - 1] = 1;
  const pilha = [[0, pts.length - 1]];
  while (pilha.length) {
    const [i, j] = pilha.pop();
    let maior = 0, k = -1;
    for (let n = i + 1; n < j; n++) {
      const d = distanciaAoSegmento(pts[n], pts[i], pts[j]);
      if (d > maior) { maior = d; k = n; }
    }
    if (maior > tol) { manter[k] = 1; pilha.push([i, k], [k, j]); }
  }
  return pts.filter((_, n) => manter[n]);
}

(async () => {
  const saida = [];
  for (const c of CAMADAS) {
    const r = await fetch(WFS + c.camada, { signal: AbortSignal.timeout(180000) });
    if (!r.ok) throw new Error(`${c.camada}: HTTP ${r.status}`);
    for (const f of (await r.json()).features) {
      if (!c.serve(f.properties)) continue;
      const partes = f.geometry.type === 'MultiLineString' ? f.geometry.coordinates : [f.geometry.coordinates];
      for (const parte of partes) {
        const pontos = simplificar(parte, TOLERANCIA).map(([x, y]) => [Math.round(x * 1e4) / 1e4, Math.round(y * 1e4) / 1e4]);
        if (pontos.length >= 2) saida.push({ tipo: c.tipo, nome: f.properties.nome ?? f.properties.codtrechoferrov ?? null, pontos });
      }
    }
  }
  const texto = JSON.stringify(saida);
  fs.writeFileSync(path.join(__dirname, '../data/malha.json'), texto);
  fs.mkdirSync(path.join(__dirname, '../../app-android/app/src/main/res/raw'), { recursive: true });
  fs.writeFileSync(path.join(__dirname, '../../app-android/app/src/main/res/raw/malha.json'), texto);
  const n = (t) => saida.filter((l) => l.tipo === t);
  console.log('linhas', saida.length, 'ferrovia', n('ferrovia').length, 'hidrovia', n('hidrovia').length, 'pontos', saida.reduce((s, l) => s + l.pontos.length, 0), 'bytes', texto.length);
})().catch((e) => { console.error(e); process.exit(1); });
