const TABELAS = {
  soja: { t: 1612, c: 81, p: 2713 },
  milho: { t: 1612, c: 81, p: 2711 },
  algodao: { t: 1612, c: 81, p: 2689 },
  trigo: { t: 1612, c: 81, p: 2716 },
  cafe: { t: 1613, c: 82, p: 2723 }
};

const UFS = {
  11: 'RO', 12: 'AC', 13: 'AM', 14: 'RR', 15: 'PA', 16: 'AP', 17: 'TO', 21: 'MA', 22: 'PI', 23: 'CE',
  24: 'RN', 25: 'PB', 26: 'PE', 27: 'AL', 28: 'SE', 29: 'BA', 31: 'MG', 32: 'ES', 33: 'RJ', 35: 'SP',
  41: 'PR', 42: 'SC', 43: 'RS', 50: 'MS', 51: 'MT', 52: 'GO', 53: 'DF'
};

const TRINTA_DIAS = 30 * 24 * 60 * 60 * 1000;

const valor = (v) => (/^-?\d+(\.\d+)?$/.test(v) ? Number(v) : null);

async function consultar(t, nivel, vars, c, p) {
  const url = `https://apisidra.ibge.gov.br/values/t/${t}/n${nivel}/all/v/${vars}/p/last/c${c}/${p}`;
  const r = await fetch(url, { signal: AbortSignal.timeout(30000) });
  if (!r.ok) throw new Error(`SIDRA HTTP ${r.status}`);
  return (await r.json()).slice(1);
}

async function buscarUf({ t, c, p }) {
  const linhas = await consultar(t, 3, '214,215,216,112', c, p);
  const porUf = new Map();
  for (const l of linhas) {
    const cod = l.D1C;
    const u = porUf.get(cod) ?? { uf: UFS[cod], codigoIbge: cod, ano: Number(l.D3C) };
    const v = valor(l.V);
    if (l.D2C === '214') u.quantidade = v;
    if (l.D2C === '215') u.valorProducao = v;
    if (l.D2C === '216') u.area = v;
    if (l.D2C === '112') u.rendimento = v;
    porUf.set(cod, u);
  }
  return [...porUf.values()].map((u) => ({
    uf: u.uf, codigoIbge: u.codigoIbge, valorProducao: u.valorProducao ?? null, quantidade: u.quantidade ?? null,
    area: u.area ?? null, rendimento: u.rendimento ?? null, ano: u.ano
  }));
}

async function buscarTop({ t, c, p }, n) {
  const linhas = await consultar(t, 6, '214,215', c, p);
  const porMun = new Map();
  for (const l of linhas) {
    const m = porMun.get(l.D1C) ?? { nome: l.D1N, ano: Number(l.D3C) };
    if (l.D2C === '214') m.quantidade = valor(l.V);
    if (l.D2C === '215') m.valorProducao = valor(l.V);
    porMun.set(l.D1C, m);
  }
  return [...porMun.values()]
    .filter((m) => m.valorProducao != null)
    .sort((a, b) => b.valorProducao - a.valorProducao)
    .slice(0, n)
    .map((m) => {
      const i = m.nome.lastIndexOf(' - ');
      return {
        municipio: i > 0 ? m.nome.slice(0, i) : m.nome, uf: i > 0 ? m.nome.slice(i + 3) : null,
        valorProducao: m.valorProducao, quantidade: m.quantidade ?? null, ano: m.ano
      };
    });
}

async function doCache(db, cultura, nivel, buscar) {
  const col = db.collection('producao');
  const doc = await col.findOne({ cultura, nivel });
  if (doc && Date.now() - new Date(doc.buscadoEm).getTime() < TRINTA_DIAS) return doc.linhas;
  try {
    const linhas = await buscar();
    await col.replaceOne({ cultura, nivel }, { cultura, nivel, linhas, buscadoEm: new Date() }, { upsert: true });
    return linhas;
  } catch (e) {
    if (doc) return doc.linhas;
    throw e;
  }
}

const producaoUf = (db, cultura) => doCache(db, cultura, 'uf', () => buscarUf(TABELAS[cultura]));
const producaoTop = (db, cultura, n) => doCache(db, cultura, `top${n}`, () => buscarTop(TABELAS[cultura], n));

module.exports = { TABELAS, buscarUf, buscarTop, producaoUf, producaoTop };
