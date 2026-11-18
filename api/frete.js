const rad = (g) => (g * Math.PI) / 180;

function haversineKm(lat1, lon1, lat2, lon2) {
  const a = Math.sin(rad(lat2 - lat1) / 2) ** 2 +
    Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(rad(lon2 - lon1) / 2) ** 2;
  return 6371 * 2 * Math.asin(Math.sqrt(a));
}

const r2 = (n) => Math.round(n * 100) / 100;

function distanciaEstimadaKm(origem, destino, fatorRota) {
  return haversineKm(origem.lat, origem.lon, destino.lat, destino.lon) * fatorRota;
}

function calcularFrete(p, e) {
  const toneladas = (e.sacas * e.kgPorSaca) / 1000;
  const fator = p.fatorSafra[e.mes ?? new Date().getMonth()] ?? 1;
  let freteTotal, duracaoH, viagens = null;
  if (e.modal === 'rodoviario') {
    viagens = Math.ceil(toneladas / e.capacidadeT);
    const combustivel = ((e.distanciaKm * 2) / e.kmPorLitro) * p.diesel;
    const pedagio = e.eixos * p.pedagioEixo100km * (e.distanciaKm / 100);
    freteTotal = viagens * (combustivel + pedagio + p.custoFixoViagem) * fator;
    duracaoH = (e.distanciaKm / p.velocidadeCaminhaoKmh) * p.fatorTempoCaminhao;
  } else if (e.modal === 'ferroviario' || e.modal === 'hidroviario') {
    freteTotal = toneladas * (e.distanciaKm * p.custoTKm + p.taxaTransbordoT) * fator;
    duracaoH = e.distanciaKm / p.velocidadeKmh + p.horasTerminal;
    if (e.pernas) {
      duracaoH = p.horasTerminal + e.pernas.reduce((h, perna) =>
        h + (perna.tipo === 'rodovia' ? (perna.km / p.velocidadeCaminhaoKmh) * p.fatorTempoCaminhao : perna.km / p.velocidadeKmh), 0);
    }
  } else {
    throw new Error('Modal inválido');
  }
  const receita = e.sacas * e.precoSaca;
  const fretePorSaca = freteTotal / e.sacas;
  return {
    distanciaKm: r2(e.distanciaKm), duracaoH: r2(duracaoH), viagens,
    freteTotal: r2(freteTotal), receitaTotal: r2(receita), margem: r2(receita - freteTotal),
    fretePorSaca: r2(fretePorSaca), liquidoSaca: r2(e.precoSaca - fretePorSaca)
  };
}

module.exports = { calcularFrete, distanciaEstimadaKm, haversineKm };
