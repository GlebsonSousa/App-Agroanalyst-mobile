const { PARAMETROS_PADRAO } = require('./parametros');

const GRAOS = [['soja', 60], ['milho', 60]];

const PRACAS = [
  ['Porto de Santos', 'porto', 'SP', -23.9608, -46.3336],
  ['Porto de Paranaguá', 'porto', 'PR', -25.5163, -48.5225],
  ['Porto de Rio Grande', 'porto', 'RS', -32.035, -52.0986],
  ['Porto de Itaqui', 'porto', 'MA', -2.5667, -44.3667],
  ['Porto de Vitória', 'porto', 'ES', -20.3222, -40.338],
  ['Terminal de Rondonópolis', 'terminal', 'MT', -16.4673, -54.6372]
];

async function semear(db) {
  for (const [nome, kgPorSaca] of GRAOS) {
    await db.collection('graos').updateOne({ nome }, { $setOnInsert: { nome, kgPorSaca } }, { upsert: true });
  }
  for (const [nome, tipo, uf, lat, lon] of PRACAS) {
    await db.collection('pracas').updateOne({ nome }, { $setOnInsert: { nome, tipo, uf, lat, lon } }, { upsert: true });
  }
  await db.collection('parametros').updateOne({ _id: 'frete' }, { $setOnInsert: PARAMETROS_PADRAO }, { upsert: true });
}

module.exports = { semear };
