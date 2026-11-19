const fs = require('fs');

const LIMITE_MS = 100;
const report = JSON.parse(fs.readFileSync('report.json', 'utf8'));
const agregado = report.aggregate;
const mediana = agregado.summaries['http.response_time'].median;
const contadores = agregado.counters;

const erros = Object.keys(contadores)
  .filter((k) => k.startsWith('vusers.failed') || k.startsWith('errors.') || /^http\.codes\.(?!200$)/.test(k))
  .filter((k) => contadores[k] > 0);

console.log(`Mediana do tempo de resposta: ${mediana} ms (limite: ${LIMITE_MS} ms)`);
console.log(`Requisições: ${contadores['http.requests']}, respostas 200: ${contadores['http.codes.200'] ?? 0}`);

if (erros.length) {
  console.error(`Erros encontrados: ${erros.map((k) => `${k}=${contadores[k]}`).join(', ')}`);
  console.error('Teste de performance reprovado!');
  process.exit(1);
}
if (mediana < LIMITE_MS) {
  console.log('Teste de performance aprovado!');
  process.exit(0);
}
console.error('Teste de performance reprovado!');
process.exit(1);
