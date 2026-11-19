# AgroAnalyst Mobile: instruções de uso e funcionalidades

Autor: Glebson Sousa

Código no GitHub: https://github.com/GlebsonSousa/App-Agroanalyst-mobile
Baixar o app (APK): https://app-agroanalyst-mobile.onrender.com/download

## Para o professor: instalar o app em um minuto

1. No celular Android, abra o link https://app-agroanalyst-mobile.onrender.com/download no navegador.
2. Baixe o arquivo e, se o Android pedir, permita "instalar apps desconhecidos" para o navegador.
3. Abra o app. Para entrar na conta, use `teste@fatec.br` / `123456` (o app também funciona sem login).

Na primeira abertura o servidor gratuito pode levar até um minuto para acordar; o app avisa e mostra os últimos dados salvos enquanto isso. O código completo, com o histórico de commits, está no link do GitHub acima.

## 1. O que o app faz e como funciona

O AgroAnalyst Mobile é um app Android nativo que reúne, em um só lugar, preços de grãos, clima, produção agrícola por estado, análise de solo por coordenada e cálculo de frete. É o braço móvel do AgroAnalyst (site de análise de solo e recomendação de culturas) e da aba AgroLogística (regras de frete e praças de escoamento).

**Como funciona, em ordem de uso**

1. **Abrir.** O app abre direto na Início, sem login. Os cartões de preço, o clima e o mapa de produção aparecem na hora com o último retrato dos dados embutido no app e são atualizados pela API assim que ela responde.
2. **Explorar o mercado.** Em *Mercado* e na Início o usuário vê preço, variação, fonte e histórico de soja, milho, café, algodão e trigo. A cultura escolhida governa o mapa de produção, o gráfico e o ranking de municípios.
3. **Ver o mapa.** Em *Mapa* há duas camadas: **Produção** (os 27 estados coloridos por faixa de produção) e **Logística** (ferrovias, hidrovias, praças e terminais). Tocando em dois pontos o app desenha o trajeto real por rodovia, ferrovia ou hidrovia e mostra a distância.
4. **Analisar um local.** Em *Análise* o usuário escolhe o ponto por busca de cidade (com sugestões enquanto digita), pelo GPS ou por latitude e longitude. O app mostra tipo de solo, pH, textura, drenagem, fertilidade, chuva de 12 meses e as culturas recomendadas.
5. **Simular frete.** Em *Frete* o usuário escolhe partida e destino (cidade, coordenadas, praça, plantio ou "meus locais"), o grão, as sacas e o modal. O app calcula o custo pela regra do AgroLogística usando a distância do trajeto real.
6. **Entrar para guardar.** A conta só é exigida para o que pertence ao usuário: plantios (com foto), fretes salvos e "meus locais". O login aparece quando necessário e devolve o usuário para onde ele estava.

**Quem fala com quem**

```
App Android (Kotlin, Compose, Material 3)
   │  Retrofit + Gson, token JWT no cabeçalho só nas rotas privadas
   ▼
API própria (Node 20 + Express, na Render)  ──►  MongoDB Atlas (usuários, plantios, fretes, locais, fotos em GridFS, cache)
   │
   ├──► CEPEA/ESALQ ........ preços (coleta a cada 6 h)
   ├──► IBGE (SIDRA, malhas, BC250) ... produção, estados, ferrovias e hidrovias
   ├──► Open-Meteo ......... clima e busca de cidades
   ├──► OSRM ............... trajeto rodoviário
   └──► meteoserver ........ solo, chuva e recomendação de culturas (resultado guardado por ponto)
```

O app fala só com a API própria. Ela concentra as fontes externas, guarda cache e aplica as regras de frete, para o celular não depender de cada serviço.

## 2. API publicada

Endereço: https://app-agroanalyst-mobile.onrender.com

O servidor gratuito da Render hiberna depois de 15 minutos sem uso: **a primeira chamada pode levar até um minuto**. O app mostra "Acordando o servidor…" depois de 5 segundos e a Início aparece na hora com os últimos dados salvos.

## 3. Usuário de teste (já cadastrado)

E-mail: `teste@fatec.br`
Senha: `123456`

## 4. Baixar ou rodar o app

- **Sem instalar nada no computador:** abra https://app-agroanalyst-mobile.onrender.com/download no navegador do celular Android, baixe o APK, permita "instalar apps desconhecidos" e instale. A rota entrega sempre o APK mais recente da última release do repositório.
- **Pelo Android Studio:** 1. abrir a pasta `app-android/`; 2. aguardar o Gradle Sync; 3. executar em emulador com API 24 ou superior (ou em um aparelho).

## 5. Como apontar para uma API local

- `app-android/app/src/main/java/com/example/agroanalyst/data/ApiClient.kt`: trocar `BASE_URL` por `http://10.0.2.2:3000/`.
- `AndroidManifest.xml`: acrescentar `android:usesCleartextTraffic="true"` na tag `<application>`.
- API: `cd api`, `npm install`, criar `.env` com `MONGODB_URI` e `JWT_SECRET` (modelo em `.env.example`) e `npm start`. Testes: `npm test`.

## 6. Tela por tela


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

## 7. Funcionalidades contra o conteúdo da disciplina

| Conteúdo | Onde está no código (`app-android/.../agroanalyst/`) |
|---|---|
| `@Composable`, `Text`, `Button`, `OutlinedTextField`, `Column`, `Row`, `Modifier`, `Box` | `ui/LoginTela.kt`, `ui/CadastroUsuarioTela.kt` e todas as telas |
| `remember` com `mutableStateOf`, recomposição | Campos dos formulários (`LoginTela`, `PlantioFormTela`, `FreteTela`) |
| Lambdas em `onClick` / `onValueChange` | Todos os controles |
| Componente reutilizável com state hoisting | `CampoTexto(valor, aoMudar, rotulo)` em `ui/Componentes.kt` (não guarda estado) |
| `data class` para dados | `data/Modelos.kt` |
| `LazyColumn` com `items` e `key` estável | `ui/PlantiosTela.kt`, `ui/MercadoTela.kt`, `ui/InicioTela.kt` |
| Item componentizado (dados entram, eventos saem) | `PlantioItem(plantio, aoAbrir, aoExcluir)` em `ui/PlantiosTela.kt` |
| `NavController`, `NavHost`, grafo de destinos | `AppAgro.kt` |
| Rotas tipadas com `@Serializable` e `toRoute()` | `Rotas.kt` e `AppAgro.kt` |
| Back stack: `navigate`, `popBackStack`, `popUpTo` | `AppAgro.kt` (login que devolve ao ponto de origem, abas com `popUpTo`) |
| Telas não recebem `NavController`, só callbacks | Toda assinatura em `ui/` |
| `ViewModel` com estado e operações; fluxo unidirecional | `viewmodel/` |
| ViewModels criados acima do `NavHost` | `AppAgro.kt` (linhas iniciais) |
| `suspend` e `viewModelScope.launch` | Toda chamada de rede, a partir de `OperacaoViewModel` |
| Estado `carregando`, `try/finally`, `CancellationException` relançada | `viewmodel/OperacaoViewModel.kt` |
| `LaunchedEffect` para carregar ao entrar na tela | `PlantiosTela`, `InicioTela` e outras |
| `withContext(Dispatchers.IO)` | Leitura dos bytes da foto (`data/FotoUpload.kt`), semente e malha locais |
| `async` / `await` em chamadas independentes | `viewmodel/InicioViewModel.kt` (preços, clima e cultura em paralelo) |
| Interface Retrofit (`@GET`, `@POST`, `@PUT`, `@DELETE`, `@Path`, `@Query`, `@Body`) | `data/ApiAgro.kt` |
| `Response<T>`, `isSuccessful`, `code()`, leitura do `errorBody` | `data/Repositorio.kt` |
| `@SerializedName` do Gson | `data/Modelos.kt` (`solo_local`, `culturas_db`, `solo_preciso`) |
| `Retrofit.Builder`, `baseUrl`, `GsonConverterFactory`, interceptor lendo o token a cada chamada | `data/ApiClient.kt` |
| Cadastro, login, JWT, sair | `viewmodel/AuthViewModel.kt`, `data/Sessao.kt` |
| CRUD completo de um recurso do usuário | Plantios (`PlantiosViewModel`, `PlantioFormTela`, `PlantioDetalheTela`) |
| Upload multipart com a parte `foto` + GridFS | `data/FotoUpload.kt` e `POST /upload` na API |
| Coil exibindo imagem vinda da API | `ui/PlantioDetalheTela.kt` |
| Intent implícito `ACTION_VIEW` (app de mapas) e `ACTION_SEND` com `createChooser` (compartilhar) | `ui/Intents.kt` |
| `uses-permission` e `uses-feature` | `AndroidManifest.xml` (INTERNET, localização; GPS não obrigatório) |
| Permissão perigosa em tempo de execução | Botão "Usar minha posição" (`ui/Localizacao.kt`) |
| Consumo de Content Provider | `data/FotoUpload.kt`: o Photo Picker devolve um `Uri`; `openInputStream`, `getType` e `query` (`OpenableColumns`) leem a foto **sem pedir permissão de armazenamento** |

## 8. Guia do código

**App (`app-android/app/src/main/java/com/example/agroanalyst/`)**

| Pasta ou arquivo | Papel |
|---|---|
| `MainActivity.kt`, `AppAgro.kt`, `Rotas.kt` | Ponto de entrada, grafo de navegação, barra de abas e criação dos ViewModels |
| `data/` | Rede e dados: `ApiAgro` (interface Retrofit), `ApiClient` (Retrofit, Gson, interceptor), `Repositorio` (chamadas, tentativas e mensagens de erro), `Sessao` (token em memória), `Modelos` (DTOs), `Semente` (dados embutidos para abrir sem rede), `MalhaLocal` (malha dos estados), `FotoUpload` |
| `viewmodel/` | Estado e operações de cada área: `Inicio`, `Mercado`, `Mapa`, `Analise`, `Plantios`, `Frete`, `Auth`, `Busca` (cidades com sugestões) e `OperacaoViewModel` (executa uma operação com `carregando` e tratamento de erro) |
| `ui/` | Telas e componentes Compose; `MapaOsm.kt` desenha o mapa (osmdroid), `SeletorLocal.kt` é o seletor de local reutilizado em Análise, Frete, Plantio e Início |
| `ui/theme/` | Cores, tipografia e formas (Material 3). Telas usam `MaterialTheme`, sem cor fixa |
| `res/raw/` | `semente.json`, `ufs.geojson`, `malha.json` (embutidos) |

**API (`api/`)**

| Arquivo | Papel |
|---|---|
| `server.js` | Rotas, autenticação JWT, upload GridFS, tarefas de fundo |
| `cepea.js`, `sidra.js`, `clima.js`, `cidades.js` | Preços, produção, clima, busca de cidades |
| `solo.js` | Consulta de solo ao meteoserver em segundo plano, com cache por ponto e resposta 202 enquanto não termina |
| `rotas.js` e `data/malha.json` | Trajeto rodoviário (OSRM) e grafo de ferrovias e hidrovias do IBGE (Dijkstra) |
| `frete.js`, `parametros.js` | Regra de frete e constantes (diesel, pedágio, transbordo, velocidades) |
| `seed.js`, `scripts/` | Dados iniciais (praças, semente de preços) e geradores da semente e da malha |
| `test/` | Testes automatizados (`npm test`): frete, parser do CEPEA e rotas |

**Rotas da API**

| Tipo | Rotas |
|---|---|
| Públicas | `GET /publico/precos`, `/publico/precos/:cultura/serie`, `/publico/producao`, `/publico/producao/top`, `/publico/clima`, `/publico/pracas`, `/publico/solo`, `/publico/cidades`, `/publico/rota`, `POST /publico/frete/calcular`, `GET /upload/:fileId`, `/download` |
| Conta | `POST /usuarios/registrar`, `POST /usuarios/login` |
| Privadas (Bearer JWT) | `GET/POST /plantios`, `GET/PUT/DELETE /plantios/:id`, `GET/POST /plantios/:id/fretes`, `GET/POST/DELETE /locais`, `POST /upload` |

**Regra de frete** (mesma do AgroLogística): diesel R$ 6,00, pedágio R$ 8,00 por eixo a cada 100 km, custo fixo R$ 500, fator de rota 1,3 quando a distância é estimada, transbordo e velocidade por modal. Exemplo de conferência: 3.000 sacas e 1.420 km dão R$ 47.985,60 (coberto em `api/test/frete.test.js`).

## 9. Como foi validado

- API: `npm test` (11 testes) e chamadas de ponta a ponta em produção: cadastro, login, CRUD de plantios, upload e leitura de foto, erros 400, 401 e 404, busca de cidades, rotas dos três modais, frete, locais e consulta de solo (primeira lenta, seguintes instantâneas).
- App: roteiro automatizado em emulador Android (API 34) com 42 passos, sem falha: Início e clima por cidade, Mercado, Mapa de produção, GPS, Análise por cidade, login, criar, editar e excluir plantio, frete rodoviário e ferroviário, rota no mapa, hidrovia indisponível, salvar e excluir local, e abertura do app sem rede. O roteiro está em `.github/e2e.py` e roda pelo fluxo `diagnostico.yml`.
- O APK é gerado pelo GitHub Actions (`apk.yml`) a cada alteração e publicado na release mais recente.

## 10. Fontes dos dados


- Preços: CEPEA/ESALQ (indicadores de soja Paranaguá, milho, café arábica, algodão em pluma e trigo Paraná).
- Produção agrícola: IBGE, Pesquisa Agrícola Municipal, via SIDRA.
- Malha dos estados: IBGE, API de malhas.
- Clima e busca de cidades: Open-Meteo (previsão e geocodificação).
- Rotas rodoviárias: OSRM (servidor público de demonstração, baseado no OpenStreetMap).
- Ferrovias e hidrovias: IBGE, Base Cartográfica Contínua 1:250.000 (BC250, 2025), trechos em operação, simplificados.
- Mapa base: OpenStreetMap.
- Solo, chuva e recomendação de culturas: meteoserver, consultado pela API própria, que guarda o resultado por ponto.

## 11. Limitações assumidas


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
