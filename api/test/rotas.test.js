const test = require('node:test');
const assert = require('node:assert');
const { grafo, maisProximo, caminho, rotaReal } = require('../rotas');

test('grafo ferroviario liga Rondonopolis a Santos', () => {
  const g = grafo('ferrovia');
  const o = maisProximo(g, { lat: -16.4673, lon: -54.6372 });
  const d = maisProximo(g, { lat: -23.9608, lon: -46.3336 });
  assert.ok(o.km < 100 && d.km < 100);
  const c = caminho(g, o.i, d.i);
  assert.ok(c && c.km > 1000 && c.km < 3000, `km ${c && c.km}`);
  assert.ok(c.pontos.length > 10);
});

test('grafo hidroviario tem nos', () => {
  assert.ok(grafo('hidrovia').nos.length > 100);
});

test('pontos sem malha proxima nao geram rota', async () => {
  const r = await rotaReal({ lat: -23.67, lon: -46.46 }, { lat: -23.96, lon: -46.33 }, 'hidroviario');
  assert.strictEqual(r, null);
});
