const cheerio = require('cheerio');

const PRODUTOS = {
  soja: { pagina: 'soja', unidade: 'R$/sc 60 kg' },
  milho: { pagina: 'milho', unidade: 'R$/sc 60 kg' },
  cafe: { pagina: 'cafe', unidade: 'R$/sc 60 kg' },
  algodao: { pagina: 'algodao', unidade: 'cent. R$/lp' },
  trigo: { pagina: 'trigo', unidade: 'R$/t' }
};

const FONTE = 'CEPEA/ESALQ';
const AGENTE = 'AgroAnalystMobile/2.0 (projeto academico Fatec Maua)';
const SEIS_HORAS = 6 * 60 * 60 * 1000;

const numero = (t) => Number(t.replace(/\./g, '').replace(',', '.').replace('%', ''));

function parsearIndicador(html) {
  const $ = cheerio.load(html);
  const linhas = [];
  $('#imagenet-indicador1 tbody tr').each((_, tr) => {
    const c = $(tr).find('td').map((_, td) => $(td).text().trim()).get();
    const m = /^(\d\d)\/(\d\d)\/(\d{4})$/.exec(c[0] ?? '');
    const valor = numero(c[1] ?? '');
    if (!m || !Number.isFinite(valor)) return;
    const variacao = numero(c[2] ?? '');
    linhas.push({ data: `${m[3]}-${m[2]}-${m[1]}`, valor, variacaoDia: Number.isFinite(variacao) ? variacao : null });
  });
  return linhas;
}

async function baixar(pagina) {
  const r = await fetch(`https://www.cepea.org.br/br/indicador/${pagina}.aspx`, {
    headers: { 'User-Agent': AGENTE },
    signal: AbortSignal.timeout(20000)
  });
  if (!r.ok) throw new Error(`CEPEA ${pagina}: HTTP ${r.status}`);
  return r.text();
}

async function atualizarPrecos(db, { forcar = false } = {}) {
  const status = db.collection('parametros');
  const precos = db.collection('precos');
  const atual = (await status.findOne({ _id: 'precos' })) ?? { _id: 'precos', stale: {}, tentativa: {} };
  const resultado = {};
  for (const [cultura, produto] of Object.entries(PRODUTOS)) {
    const ultima = atual.tentativa[cultura] ? new Date(atual.tentativa[cultura]).getTime() : 0;
    if (!forcar && Date.now() - ultima < SEIS_HORAS) { resultado[cultura] = 'ignorado'; continue; }
    atual.tentativa[cultura] = new Date().toISOString();
    try {
      const linhas = parsearIndicador(await baixar(produto.pagina));
      if (!linhas.length) throw new Error('tabela vazia');
      for (const l of linhas) {
        await precos.updateOne(
          { cultura, data: l.data },
          { $set: { ...l, cultura, unidade: produto.unidade, fonte: FONTE, coletadoEm: new Date() } },
          { upsert: true }
        );
      }
      atual.stale[cultura] = false;
      resultado[cultura] = `${linhas.length} linhas`;
    } catch (e) {
      atual.stale[cultura] = true;
      resultado[cultura] = `falhou: ${e.message}`;
    }
  }
  await status.replaceOne({ _id: 'precos' }, atual, { upsert: true });
  return resultado;
}

async function listarPrecos(db) {
  const status = (await db.collection('parametros').findOne({ _id: 'precos' })) ?? { stale: {} };
  const saida = [];
  for (const cultura of Object.keys(PRODUTOS)) {
    const l = await db.collection('precos').find({ cultura }).sort({ data: -1 }).limit(1).next();
    if (!l) continue;
    saida.push({
      cultura, valor: l.valor, unidade: l.unidade, variacaoDia: l.variacaoDia,
      data: l.data, fonte: l.fonte, stale: status.stale[cultura] !== false
    });
  }
  return saida;
}

async function serie(db, cultura, dias) {
  const linhas = await db.collection('precos').find({ cultura }).sort({ data: -1 }).limit(dias).toArray();
  return linhas.reverse().map((l) => ({ data: l.data, valor: l.valor }));
}

module.exports = { PRODUTOS, FONTE, baixar, parsearIndicador, atualizarPrecos, listarPrecos, serie };
