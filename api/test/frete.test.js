const test = require('node:test');
const assert = require('node:assert');
const { calcularFrete, haversineKm } = require('../frete');
const { PARAMETROS_PADRAO } = require('../parametros');

const base = { distanciaKm: 1420, sacas: 3000, kgPorSaca: 60, precoSaca: 128.4, mes: 0 };

test('rodoviario', () => {
  const r = calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'rodoviario', capacidadeT: 30, kmPorLitro: 2.5, eixos: 6 });
  assert.strictEqual(r.viagens, 6);
  assert.strictEqual(r.freteTotal, 47985.6);
  assert.strictEqual(r.receitaTotal, 385200);
  assert.strictEqual(r.margem, 337214.4);
  assert.strictEqual(r.fretePorSaca, 16);
  assert.strictEqual(r.liquidoSaca, 112.4);
});

test('ferroviario', () => {
  const r = calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'ferroviario' });
  assert.strictEqual(r.viagens, null);
  assert.strictEqual(r.freteTotal, 33912);
  assert.strictEqual(r.margem, 351288);
  assert.strictEqual(r.fretePorSaca, 11.3);
  assert.strictEqual(r.liquidoSaca, 117.1);
});

test('hidroviario', () => {
  const r = calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'hidroviario' });
  assert.strictEqual(r.freteTotal, 33912);
  assert.strictEqual(r.receitaTotal, 385200);
});

test('fator de safra multiplica o frete', () => {
  const p = { ...PARAMETROS_PADRAO, fatorSafra: Array(12).fill(1.1) };
  const r = calcularFrete(p, { ...base, modal: 'ferroviario' });
  assert.strictEqual(r.freteTotal, 37303.2);
});

test('modal invalido', () => {
  assert.throws(() => calcularFrete(PARAMETROS_PADRAO, { ...base, modal: 'aereo' }));
});

test('haversine Sao Paulo - Rio', () => {
  const d = haversineKm(-23.55, -46.63, -22.91, -43.17);
  assert.ok(d > 350 && d < 370);
});
