# AgroAnalyst Mobile — instruções de uso e funcionalidades

Autor: Glebson Sousa
Demais integrantes: ______________________

## 1. O que o app faz
App Android nativo (Kotlin + Jetpack Compose) que reúne duas frentes: **análise de solo** de um ponto (lado AgroAnalyst, consumindo o meteoserver) e **plantios, frete e vendas** (lado AgroLogística, regras de frete portadas para a API própria). O usuário cadastra-se, analisa um ponto por GPS ou coordenada, salva plantios com foto, calcula o frete até uma praça e registra a venda.

## 2. API publicada
`https://agroanalyst-api.onrender.com/` _(ajuste se o nome do serviço no Render for outro e atualize `BASE_URL` em `ApiClient.kt`)_.
**Atenção:** o servidor gratuito do Render hiberna. **A primeira chamada pode levar até um minuto.** O app mostra "Acordando o servidor…" após 5 s e aguarda; abrir a tela de login já acorda os dois serviços em paralelo.

## 3. Usuário de teste
Cadastre antes de entregar e preencha aqui: e-mail `____` · senha `____`.

## 4. Como rodar
1. Android Studio (versão recente) → *Open* → pasta `app-android/`.
2. Aguardar o *Gradle Sync*. 3. Executar em emulador com **API 24 ou superior**.

## 5. Apontar para uma API local
- `ApiClient.kt`: trocar `BASE_URL` por `http://10.0.2.2:3000/`.
- `AndroidManifest.xml`: acrescentar `android:usesCleartextTraffic="true"` na tag `<application>` (há um comentário no lugar).
- API: ver `api/.env.example`; `cd api && npm install && npm start`. Teste do frete: `npm test`.

## 6. Tela por tela
| Tela | O que dá para fazer |
|---|---|
| Login | Entrar; ir para o cadastro. Aquece os servidores ao abrir. |
| Cadastro | Nome, e-mail, senha e confirmação; mensagem da API se o e-mail já existe. |
| Análise | "Usar minha posição" (pede permissão em tempo de execução) ou digitar latitude/longitude. |
| Resultado | Solo, pH, textura, drenagem, fertilidade, gráfico de chuva de 12 meses, culturas recomendadas; "Salvar no plantio". |
| Detalhe da cultura | Faixas e requisitos vindos do meteoserver. |
| Plantios | Lista, excluir (com confirmação), novo plantio. |
| Formulário | Criar/editar com foto (Photo Picker) e rascunho preservado na rotação. |
| Detalhe do plantio | Dados, foto, solo do ponto, calcular frete, ver no mapa, editar. |
| Frete | Plantio, destino, modal, sacas, preço; resultado do cálculo; registrar venda. |
| Vendas / detalhe | Lista, números da venda, mudar status, "Ver rota no mapa", "Compartilhar resumo". |

**Foto sem permissão:** o Photo Picker roda fora do processo e devolve um `content://` com leitura só daquele item; o app o consome via `ContentResolver` (`openInputStream`, `getType`, `query`) sem pedir `READ_EXTERNAL_STORAGE` nem `READ_MEDIA_IMAGES`.

## 7. Funcionalidades × conteúdo da disciplina
| Conteúdo | Onde |
|---|---|
| Composables, estado, `remember`, state hoisting | `Componentes.kt` (`CampoTexto`), telas de login/cadastro |
| `LazyColumn` com `key`, item componentizado | `PlantiosTela.kt` (`PlantioItem`), `VendasTela.kt` |
| Navegação tipada, back stack, telas sem `NavController` | `Rotas.kt`, `AppAgro.kt` |
| ViewModel, fluxo unidirecional, VMs acima do `NavHost` | `viewmodel/`, `AppAgro.kt` |
| Corrotinas, `try/finally`, `CancellationException`, `async/await`, `Dispatchers.IO` | `OperacaoViewModel.kt`, `LogisticaViewModel.kt`, `Repositorio.kt`, `FotoUpload.kt` |
| Retrofit, Gson, `Response<T>`, `errorBody`, interceptor de token | `ApiAgro.kt`, `ApiMeteo.kt`, `ApiClient.kt`, `Repositorio.kt` |
| Intents `ACTION_VIEW` e `ACTION_SEND`, permissão de localização, Content Provider | `Intents.kt`, `AnaliseMapaTela.kt`, `FotoUpload.kt`, `AndroidManifest.xml` |
| Login/JWT, CRUD, upload multipart `foto` (GridFS), Coil | `AuthViewModel.kt`, `PlantiosViewModel.kt`, `api/server.js`, `PlantioDetalheTela.kt` |

## 8. Limitações assumidas
- Distância **estimada** (Haversine × 1,3), não rota real; "Ver rota no mapa" abre o app de mapas.
- O JWT **não é persistido**: fechar o app exige novo login (proposital).
- Sem modo offline. Só tema claro.
- Parâmetros de frete ferroviário/hidroviário, fator de safra mensal e o preço de referência dos destinos são valores de referência (ver `api/frete.js` e o seed em `api/server.js`); o rodoviário bate com o caso de teste (R$ 47.985,60).
- A geocodificação reversa (Nominatim) não foi implementada; o contexto do resultado usa a cidade devolvida pelo meteoserver.
