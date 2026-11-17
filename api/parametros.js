// velocidadeCaminhaoKmh e horasTerminal: confirmar no frete.php
const PARAMETROS_PADRAO = {
  diesel: 6,
  pedagioEixo100km: 8,
  custoFixoViagem: 500,
  fatorRota: 1.3,
  custoTKm: 0.12,
  taxaTransbordoT: 18,
  velocidadeKmh: 25,
  fatorTempoCaminhao: 1.25,
  velocidadeCaminhaoKmh: 60,
  horasTerminal: 0,
  fatorSafra: Array(12).fill(1)
};

module.exports = { PARAMETROS_PADRAO };
