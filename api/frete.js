// Regras de frete portadas do sistema web do sistema web. Cálculo sempre no servidor.
// Constantes do rodoviário deduzidas do caso de teste do enunciado (diesel 6,00; pedágio 8,00/eixo/100 km; fixo 500).
// Parâmetros de ferroviário/hidroviário e fator de safra são valores de referência; conferir com o agro.js do sistema web.
const CFG = {
  sinuosidade: 1.3,
  diesel: 6,
  pedagioEixo100km: 8,
  custoFixoViagem: 500,
  velocidadeRodoviarioKmh: 60,
  fatorSafra: Array(12).fill(1),
  ferroviario: { custoTKm: 0.12, transbordoT: 15, velocidadeKmh: 25, horasTerminal: 24 },
  hidroviario: { custoTKm: 0.08, transbordoT: 20, velocidadeKmh: 15, horasTerminal: 36 },
};

const rad = (g) => (g * Math.PI) / 180;
function haversineKm(lat1, lon1, lat2, lon2) {
  const a = Math.sin(rad(lat2 - lat1) / 2) ** 2 +
    Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(rad(lon2 - lon1) / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(a));
}
const distanciaEstimadaKm = (p, d) => haversineKm(p.lat, p.lon, d.lat, d.lon) * CFG.sinuosidade;
const r2 = (n) => Math.round(n * 100) / 100;

function calcularFrete({ distanciaKm, sacas, kgPorSaca, precoSaca, modal, capacidadeT, kmPorLitro, eixos, mes = new Date().getMonth() }) {
  const toneladas = (sacas * kgPorSaca) / 1000;
  const fator = CFG.fatorSafra[mes];
  let freteTotal, duracaoH, viagens = null;
  if (modal === 'rodoviario') {
    viagens = Math.ceil(toneladas / capacidadeT);
    const comb = ((distanciaKm * 2) / kmPorLitro) * CFG.diesel;
    const pedagio = eixos * CFG.pedagioEixo100km * (distanciaKm / 100);
    freteTotal = viagens * (comb + pedagio + CFG.custoFixoViagem) * fator;
    duracaoH = distanciaKm / CFG.velocidadeRodoviarioKmh;
  } else if (modal === 'ferroviario' || modal === 'hidroviario') {
    const c = CFG[modal];
    freteTotal = toneladas * (distanciaKm * c.custoTKm + c.transbordoT) * fator;
    duracaoH = distanciaKm / c.velocidadeKmh + c.horasTerminal;
  } else throw new Error('Modal inválido');
  const receita = sacas * precoSaca;
  const fretePorSaca = freteTotal / sacas;
  return {
    distanciaKm: r2(distanciaKm), duracaoH: r2(duracaoH), viagens,
    freteTotal: r2(freteTotal), receitaTotal: r2(receita), margem: r2(receita - freteTotal),
    fretePorSaca: r2(fretePorSaca), liquidoSaca: r2(precoSaca - fretePorSaca),
  };
}

module.exports = { calcularFrete, distanciaEstimadaKm };
