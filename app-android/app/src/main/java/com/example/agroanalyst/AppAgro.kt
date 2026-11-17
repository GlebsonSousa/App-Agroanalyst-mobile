package com.example.agroanalyst

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.agroanalyst.data.Sessao
import com.example.agroanalyst.ui.*
import com.example.agroanalyst.viewmodel.*

private class Aba(val rotulo: String, val icone: ImageVector, val rota: Any, val ativa: (androidx.navigation.NavDestination?) -> Boolean)

private val ABAS = listOf(
    Aba("Análise", Icons.Filled.QueryStats, Analise) { it?.hasRoute<Analise>() == true },
    Aba("Plantios", Icons.Filled.Place, Plantios) { it?.hasRoute<Plantios>() == true },
    Aba("Vendas", Icons.Filled.LocalShipping, Vendas) { it?.hasRoute<Vendas>() == true }
)

@Composable
fun AppAgro() {
    val nav = rememberNavController()

    // Criados ACIMA do NavHost: lista e detalhe enxergam o mesmo estado.
    val auth: AuthViewModel = viewModel()
    val analise: AnaliseViewModel = viewModel()
    val plantios: PlantiosViewModel = viewModel()
    val logistica: LogisticaViewModel = viewModel()

    val destino = nav.currentBackStackEntryAsState().value?.destination
    val mostrarBarra = ABAS.any { it.ativa(destino) }

    // Sair da conta e 401/403 limpam o token; aqui o app reage: zera os dados e volta ao login.
    val token = Sessao.token
    LaunchedEffect(token) {
        if (token == null) {
            analise.limpar(); plantios.limpar(); logistica.limpar()
            val atual = nav.currentDestination
            if (atual != null && !atual.hasRoute<Login>() && !atual.hasRoute<Cadastro>()) {
                nav.navigate(Login) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (mostrarBarra) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                ABAS.forEach { aba ->
                    NavigationBarItem(
                        selected = aba.ativa(destino),
                        onClick = {
                            nav.navigate(aba.rota) {
                                popUpTo<Analise> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
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
            startDestination = Login,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .then(if (mostrarBarra) Modifier else Modifier.navigationBarsPadding())
                .imePadding()
        ) {
            composable<Login> {
                LoginTela(auth,
                    aoEntrar = { nav.navigate(Analise) { popUpTo(Login) { inclusive = true } } },
                    aoIrParaCadastro = { nav.navigate(Cadastro) })
            }
            composable<Cadastro> {
                CadastroUsuarioTela(auth, aoCadastrar = { nav.popBackStack() }, aoVoltar = { nav.popBackStack() })
            }
            composable<Analise> {
                AnaliseMapaTela(analise, aoAnalisar = { lat, lon -> nav.navigate(AnaliseResultado(lat, lon)) }, aoSair = auth::sair)
            }
            composable<AnaliseResultado> { e ->
                val rota = e.toRoute<AnaliseResultado>()
                AnaliseResultadoTela(analise, rota.lat, rota.lon,
                    aoVoltar = { nav.popBackStack() },
                    aoAbrirCultura = { nav.navigate(CulturaDetalhe(it)) },
                    aoSalvarNoPlantio = { cultura ->
                        plantios.prepararNovo(cultura, rota.lat, rota.lon)
                        nav.navigate(PlantioNovo)
                    })
            }
            composable<CulturaDetalhe> { e ->
                CulturaDetalheTela(e.toRoute<CulturaDetalhe>().nome, analise) { nav.popBackStack() }
            }
            composable<Plantios> {
                PlantiosTela(plantios,
                    aoNovo = { plantios.prepararNovo(null, null, null); nav.navigate(PlantioNovo) },
                    aoAbrir = { nav.navigate(PlantioDetalhe(it)) },
                    aoSair = auth::sair)
            }
            composable<PlantioNovo> {
                PlantioFormTela(plantios, id = null, aoVoltar = { nav.popBackStack() }, aoSalvar = { nav.popBackStack() })
            }
            composable<PlantioForm> { e ->
                PlantioFormTela(plantios, id = e.toRoute<PlantioForm>().id, aoVoltar = { nav.popBackStack() }, aoSalvar = { nav.popBackStack() })
            }
            composable<PlantioDetalhe> { e ->
                val id = e.toRoute<PlantioDetalhe>().id
                PlantioDetalheTela(id, plantios, analise,
                    aoVoltar = { nav.popBackStack() },
                    aoEditar = { nav.navigate(PlantioForm(id)) },
                    aoFrete = { logistica.plantioPreSelecionado = id; nav.navigate(Frete) })
            }
            composable<Frete> {
                FreteTela(logistica, plantios, aoVoltar = { nav.popBackStack() },
                    aoVenda = { nav.navigate(Vendas) { popUpTo<Analise> { saveState = false }; launchSingleTop = true } })
            }
            composable<Vendas> {
                VendasTela(logistica,
                    aoNova = { logistica.plantioPreSelecionado = null; nav.navigate(Frete) },
                    aoAbrir = { nav.navigate(VendaDetalhe(it)) },
                    aoSair = auth::sair)
            }
            composable<VendaDetalhe> { e ->
                VendaDetalheTela(e.toRoute<VendaDetalhe>().id, logistica) { nav.popBackStack() }
            }
        }
    }
}
