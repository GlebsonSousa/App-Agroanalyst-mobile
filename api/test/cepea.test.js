const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');
const { parsearIndicador } = require('../cepea');

test('milho: 16/11/2026 R$ 67,18, -0,24%', () => {
  const html = fs.readFileSync(path.join(__dirname, 'fixtures/milho.html'), 'utf8');
  const linhas = parsearIndicador(html);
  const dia = linhas.find((l) => l.data === '2026-11-16');
  assert.deepStrictEqual(dia, { data: '2026-11-16', valor: 67.18, variacaoDia: -0.24 });
  assert.ok(linhas.length >= 5);
});

test('html sem tabela devolve vazio', () => {
  assert.deepStrictEqual(parsearIndicador('<html></html>'), []);
});
