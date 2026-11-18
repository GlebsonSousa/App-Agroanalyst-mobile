# AgroAnalyst Mobile: instruções de uso e funcionalidades

Autor: Glebson Sousa
Demais integrantes:

## 1. O que o app faz

App Android nativo que reúne, em um só lugar, preços de grãos, clima, produção agrícola por estado, análise de solo por coordenada e cálculo de frete. É o braço móvel do AgroAnalyst (site de análise de solo, que fornece o solo e a recomendação de culturas pelo meteoserver) e da aba AgroLogística (regras de frete e praças de escoamento, calculadas na API própria).

## 2. Sem conta e com conta

O app abre público: Início, Mercado, Mapa, Análise de solo e o simulador de Frete funcionam sem login. A conta só é exigida para o que pertence ao usuário: plantios, fotos de plantio e fretes salvos. O login aparece quando necessário (aba "Meus", "Salvar no plantio", "Salvar frete") e devolve o usuário para onde ele estava.

## 3. API

Endereço: https://app-agroanalyst-mobile.onrender.com

O servidor gratuito da Render hiberna depois de 15 minutos sem uso: **a primeira chamada pode levar até um minuto**. O app mostra "Acordando o servidor…" depois de 5 segundos e a Início aparece na hora com os últimos dados salvos.

## 4. Baixar o APK

https://app-agroanalyst-mobile.onrender.com/download

Abra no navegador do celular Android, baixe, permita "instalar apps desconhecidos" e instale.

## 5. Usuário de teste

E-mail: `teste@fatec.br`
Senha: `123456`

## 6. Como rodar no Android Studio

1. Abrir a pasta `app-android/` no Android Studio.
2. Aguardar o Gradle Sync.
3. Executar em um emulador com API 24 ou superior (ou em um aparelho).

## 7. Como apontar para uma API local

- `app-android/app/src/main/java/com/example/agroanalyst/data/ApiClient.kt`: trocar `BASE_URL` por `http://10.0.2.2:3000/`.
- `AndroidManifest.xml`: acrescentar `android:usesCleartextTraffic="true"` na tag `<application>`.
- API: `cd api`, `npm install`, criar `.env` com `MONGODB_URI` e `JWT_SECRET` (modelo em `.env.example`) e `npm start`. Testes: `npm test`.

## 8. Tela por tela

| Tela | O que dá para fazer |
|---|---|
| Início | Cartões de preço das 5 culturas, chips de cultura (governam o mapa, o gráfico e o ranking), clima de Mauá, SP, da sua posição ou de qualquer cidade ("Escolher cidade"), mini-mapa de produção por UF, gráfico de preço de 30 dias, atalhos para análise e frete, top 5 municípios produtores. Puxar para atualizar. |
| Mercado | Preço, unidade, variação do dia, fonte e data de soja, milho, café, algodão e trigo. Tocar abre o histórico. |
| Mapa | Camada Produção (27 UFs coloridas por faixa, com legenda e folha de detalhes por UF) e camada Logística: ferrovias e hidrovias do IBGE, praças, terminais e os seus locais. Tocar em dois pontos desenha o trajeto real do modal escolhido (rodovia, ferrovia ou hidrovia, com os trechos de acesso) e mostra a distância por rota; o que não existe para aquele par de locais não é desenhado e vem com aviso. |
| Análise | Escolher o ponto buscando a cidade (com sugestões enquanto digita), pelo GPS ou digitando latitude e longitude. O local escolhido aparece sempre num quadro com nome e coordenadas. |
| Resultado | Tipo de solo, pH, textura, drenagem, fertilidade, chuva de 12 meses e culturas recomendadas. "Salvar no plantio". |
| Cultura | Faixas ideais e requisitos, preço atual e maiores municípios produtores. |
| Frete | Simulador por modal (rodoviário, ferroviário, hidroviário). Partida e destino por busca de cidade, coordenadas, praças, plantios ou **meus locais** (salvos na conta); a distância vem do trajeto real quando existe. "Ver rota no mapa" abre o trajeto. Salvar o frete exige conta e um plantio. |
| Meus (plantios) | Lista, novo plantio com foto opcional e local por busca de cidade, editar, excluir. |
| Detalhe do plantio | Dados, foto, solo do ponto, fretes salvos, abrir no app de mapas, compartilhar. |

## 9. Fontes dos dados

- Preços: CEPEA/ESALQ (indicadores de soja Paranaguá, milho, café arábica, algodão em pluma e trigo Paraná).
- Produção agrícola: IBGE, Pesquisa Agrícola Municipal, via SIDRA.
- Malha dos estados: IBGE, API de malhas.
- Clima e busca de cidades: Open-Meteo (previsão e geocodificação).
- Rotas rodoviárias: OSRM (servidor público de demonstração, baseado no OpenStreetMap).
- Ferrovias e hidrovias: IBGE, Base Cartográfica Contínua 1:250.000 (BC250, 2025), trechos em operação, simplificados.
- Mapa base: OpenStreetMap.
- Solo, chuva e recomendação de culturas: meteoserver, consultado pela API própria, que guarda o resultado por ponto.

## 10. Limitações assumidas

- A distância do frete usa o trajeto real quando existe (rodovia pelo OSRM; ferrovia e hidrovia pela malha do IBGE, com acesso rodoviário até a malha). Sem trajeto para o par de locais, usa Haversine × 1,30 e a tela avisa que é estimada. O tempo do ferroviário e do hidroviário depende de parâmetros de velocidade e de terminal ainda a confirmar.
- A primeira consulta de solo de um ponto é lenta (o meteoserver leva de 20 s a vários minutos); a API guarda o resultado e as consultas seguintes ao mesmo ponto são instantâneas.
- O OSRM público e a geocodificação do Open-Meteo são serviços gratuitos sem garantia de disponibilidade.
- Os preços têm até 6 horas de atraso e o histórico cresce a cada coleta (o CEPEA publica poucos dias por vez).
- Soja e trigo: o CEPEA responde HTTP 403 às consultas feitas pelo servidor da Render. Enquanto isso não muda, o app mostra para essas duas culturas o último retrato embutido, com a data da coleta (17/11/2026). Milho, café e algodão atualizam normalmente.
- A produção do IBGE é anual (último ano divulgado).
- O fator de safra está em 1,0 em todos os meses.
- O JWT não é guardado no aparelho: fechar o app exige novo login.
- Sem rede, a Início mostra a semente embutida (último retrato dos dados, com a data da coleta).
- A malha ferroviária e hidroviária é a do IBGE (2025) e cobre só trechos cadastrados como em operação; um ponto a mais de 300 km da malha não tem trajeto ferroviário ou hidroviário.
