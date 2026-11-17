const assert = require('assert');
const { calcularFrete } = require('./frete');
const r = calcularFrete({ distanciaKm: 1420, sacas: 3000, kgPorSaca: 60, precoSaca: 128.4,
  modal: 'rodoviario', capacidadeT: 30, kmPorLitro: 2.5, eixos: 6, mes: 0 });
assert.strictEqual(r.viagens, 6);
assert.strictEqual(r.freteTotal, 47985.6);
assert.strictEqual(r.receitaTotal, 385200);
assert.strictEqual(r.margem, 337214.4);
assert.strictEqual(r.fretePorSaca, 16);
assert.strictEqual(r.liquidoSaca, 112.4);
console.log('frete OK', r);
