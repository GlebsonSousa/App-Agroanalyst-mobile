const fs = require('fs');
const path = require('path');
const { PRODUTOS, FONTE, baixar, parsearIndicador } = require('../cepea');
const { TABELAS, buscarUf, buscarTop } = require('../sidra');

const destino = path.join(__dirname, '../../app-android/app/src/main/res/raw/semente.json');

(async () => {
  const semente = { geradoEm: new Date().toISOString(), precos: [], series: {}, producao: {}, top: {} };
  for (const [cultura, produto] of Object.entries(PRODUTOS)) {
    const linhas = parsearIndicador(await baixar(produto.pagina));
    const ultima = linhas[0];
    semente.precos.push({
      cultura, valor: ultima.valor, unidade: produto.unidade, variacaoDia: ultima.variacaoDia,
      data: ultima.data, fonte: FONTE, stale: false
    });
    semente.series[cultura] = linhas.slice().reverse().map((l) => ({ data: l.data, valor: l.valor }));
    semente.producao[cultura] = await buscarUf(TABELAS[cultura]);
    semente.top[cultura] = await buscarTop(TABELAS[cultura], 5);
    console.log(cultura, ultima.data, ultima.valor, semente.producao[cultura].length, semente.top[cultura][0]?.municipio);
  }
  fs.mkdirSync(path.dirname(destino), { recursive: true });
  fs.writeFileSync(destino, JSON.stringify(semente));
  console.log('gravado', destino, fs.statSync(destino).size, 'bytes');
})().catch((e) => { console.error(e); process.exit(1); });
