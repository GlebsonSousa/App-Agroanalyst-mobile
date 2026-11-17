package com.example.agroanalyst

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.agroanalyst.data.Sessao
import com.example.agroanalyst.ui.*
import com.example.agroanalyst.viewmodel.*

private class Aba(val rotulo: String, val icone: ImageVector, val rota: Any, val ativa: (NavDestination?) -> Boolean)

@Composable
fun AppAgro() {
    val nav = rememberNavController()

    val auth: AuthViewModel = viewModel()
    val inicio: InicioViewModel = viewModel()
    val mercado: MercadoViewModel = viewModel()
    val mapa: MapaViewModel = viewModel()
    val analise: AnaliseViewModel = viewModel()
    val plantios: PlantiosViewModel = viewModel()
    val frete: FreteViewModel = viewModel()

    val destino = nav.currentBackStackEntryAsState().value?.destination
    val token = Sessao.token
    val noLogin = destino?.hasRoute<Login>() == true
    val loginDeMeus = noLogin && auth.origem == Plantios

    val abas = listOf(
        Aba("Início", Icons.Filled.Home, Inicio) { it?.hasRoute<Inicio>() == true },
        Aba("Mercado", Icons.Filled.Store, Mercado) { it?.hasRoute<Mercado>() == true },
        Aba("Mapa", Icons.Filled.Map, Mapa) { it?.hasRoute<Mapa>() == true },
        Aba("Análise", Icons.Filled.QueryStats, Analise) { it?.hasRoute<Analise>() == true },
        Aba("Meus", Icons.Filled.Grass, Plantios) { it?.hasRoute<Plantios>() == true || loginDeMeus }
    )
    val mostrarBarra = abas.any { it.ativa(destino) }

    fun irParaAba(aba: Aba) {
        if (aba.rota == Plantios && token == null) {
            auth.pedirLogin(Plantios)
            nav.navigate(Login) { launchSingleTop = true }
            return
        }
        nav.navigate(aba.rota) {
            popUpTo<Inicio> { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun pedirLogin(origem: Any? = null) {
        auth.pedirLogin(origem)
        nav.navigate(Login)
    }

    LaunchedEffect(token) {
        if (token == null) {
            plantios.limpar()
            auth.limpar()
            val atual = nav.currentDestination
            val privada = atual != null && (atual.hasRoute<Plantios>() || atual.hasRoute<PlantioNovo>() ||
                atual.hasRoute<PlantioForm>() || atual.hasRoute<PlantioDetalhe>())
            if (privada) nav.navigate(Inicio) { popUpTo<Inicio> { inclusive = false }; launchSingleTop = true }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (mostrarBarra) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                abas.forEach { aba ->
                    NavigationBarItem(
                        selected = aba.ativa(destino),
                        onClick = { irParaAba(aba) },
                        icon = { Icon(aba.icone, contentDescription = null) },
                        label = { Text(aba.rotulo, style = MaterialTheme.typography.labelMedium) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Inicio,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .then(if (mostrarBarra) Modifier else Modifier.navigationBarsPadding())
                .imePadding()
        ) {
            composable<Inicio> {
                InicioTela(
                    inicio, Sessao.nome,
                    aoEntrar = { pedirLogin() },
                    aoSair = auth::sair,
                    aoAbrirMapa = { cultura ->
                        mapa.selecionarCultura(cultura)
                        irParaAba(abas[2])
                    },
                    aoAnalisar = { irParaAba(abas[3]) },
                    aoFrete = { frete.preparar(null, null, null, null); nav.navigate(Frete) }
                )
            }
            composable<Mercado> { MercadoTela(mercado) }
            composable<Mapa> {
                MapaTela(mapa, aoCalcularFrete = { origem, destinoPraca ->
                    frete.preparar(origem.lat, origem.lon, destinoPraca.nome, null)
                    nav.navigate(Frete)
                })
            }
            composable<Analise> {
                AnaliseTela(analise, aoAnalisar = { lat, lon -> nav.navigate(AnaliseResultado(lat, lon)) })
            }
            composable<AnaliseResultado> { entrada ->
                val rota = entrada.toRoute<AnaliseResultado>()
                AnaliseResultadoTela(
                    analise, rota.lat, rota.lon,
                    aoVoltar = { nav.popBackStack() },
                    aoAbrirCultura = { nav.navigate(CulturaDetalhe(it)) },
                    aoSalvarNoPlantio = { cultura ->
                        if (Sessao.token == null) pedirLogin()
                        else {
                            plantios.prepararNovo(cultura, rota.lat, rota.lon)
                            nav.navigate(PlantioNovo)
                        }
                    }
                )
            }
            composable<CulturaDetalhe> { entrada ->
                CulturaDetalheTela(entrada.toRoute<CulturaDetalhe>().nome, analise, inicio) { nav.popBackStack() }
            }
            composable<Frete> {
                FreteTela(frete, plantios, logado = Sessao.token != null, aoPedirLogin = { pedirLogin() }, aoVoltar = { nav.popBackStack() })
            }
            composable<Login> {
                LoginTela(
                    auth,
                    aoEntrar = { origem ->
                        if (origem != null) nav.navigate(origem) { popUpTo<Login> { inclusive = true } }
                        else nav.popBackStack()
                    },
                    aoIrParaCadastro = { nav.navigate(Cadastro) },
                    aoVoltar = { nav.popBackStack() }
                )
            }
            composable<Cadastro> {
                CadastroUsuarioTela(auth, aoCadastrar = { nav.popBackStack() }, aoVoltar = { nav.popBackStack() })
            }
            composable<Plantios> {
                PlantiosTela(
                    plantios, Sessao.nome,
                    aoNovo = { plantios.prepararNovo(null, null, null); nav.navigate(PlantioNovo) },
                    aoAbrir = { nav.navigate(PlantioDetalhe(it)) },
                    aoSair = auth::sair
                )
            }
            composable<PlantioNovo> {
                PlantioFormTela(plantios, id = null, aoVoltar = { nav.popBackStack() }, aoSalvar = { nav.popBackStack() })
            }
            composable<PlantioForm> { entrada ->
                PlantioFormTela(plantios, id = entrada.toRoute<PlantioForm>().id, aoVoltar = { nav.popBackStack() }, aoSalvar = { nav.popBackStack() })
            }
            composable<PlantioDetalhe> { entrada ->
                val id = entrada.toRoute<PlantioDetalhe>().id
                PlantioDetalheTela(
                    id, plantios, analise,
                    aoVoltar = { nav.popBackStack() },
                    aoEditar = { nav.navigate(PlantioForm(id)) },
                    aoFrete = {
                        val p = plantios.atual
                        frete.preparar(p?.lat, p?.lon, null, id)
                        nav.navigate(Frete)
                    }
                )
            }
        }
    }
}
