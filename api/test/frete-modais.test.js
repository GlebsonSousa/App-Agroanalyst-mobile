const test = require('node:test');
const assert = require('node:assert');
const { calcularFrete } = require('../frete');
const { PARAMETROS_PADRAO } = require('../parametros');

const base = { sacas: 500, kgPorSaca: 60, precoSaca: 100, mes: 0 };

test('rodoviario cobra pedagio proporcional a distancia', () => {
  const r = calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'rodoviario', distanciaKm: 200, capacidadeT: 30, kmPorLitro: 2.5, eixos: 5 });
  assert.strictEqual(r.viagens, 1);
  assert.strictEqual(r.freteTotal, 1540);
  assert.strictEqual(r.fretePorSaca, 3.08);
});

test('rodoviario calcula a duracao pela velocidade do caminhao', () => {
  const r = calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'rodoviario', distanciaKm: 600, capacidadeT: 30, kmPorLitro: 2.5, eixos: 5 });
  assert.strictEqual(r.duracaoH, 12.5);
});

test('ferroviario com pernas soma rodovia e ferrovia', () => {
  const pernas = [{ tipo: 'rodovia', km: 120 }, { tipo: 'ferrovia', km: 500 }];
  const r = calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'ferroviario', distanciaKm: 620, pernas });
  assert.strictEqual(r.duracaoH, 22.5);
});
