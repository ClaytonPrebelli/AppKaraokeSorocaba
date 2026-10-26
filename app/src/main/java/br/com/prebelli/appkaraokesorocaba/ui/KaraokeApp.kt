package br.com.prebelli.appkaraokesorocaba.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.prebelli.appkaraokesorocaba.data.MusicasRepository
import br.com.prebelli.appkaraokesorocaba.ui.screens.BuscaScreen
import br.com.prebelli.appkaraokesorocaba.ui.screens.CategoriaScreen
import br.com.prebelli.appkaraokesorocaba.ui.screens.ListaScreen
import br.com.prebelli.appkaraokesorocaba.update.UpdateHost
import br.com.prebelli.appkaraokesorocaba.update.UpdateViewModel

@Composable
fun KaraokeApp() {
    val context = LocalContext.current
    val repository = remember {
        MusicasRepository(context.applicationContext)
    }
    val navController = rememberNavController()
    val updateViewModel: UpdateViewModel = viewModel()

    LaunchedEffect(Unit) {
        updateViewModel.verificarAoAbrir()
    }

    UpdateHost(viewModel = updateViewModel)

    NavHost(
        navController = navController,
        startDestination = "lista"
    ) {
        composable("lista") {
            ListaScreen(
                onCategoria = { categoria ->
                    navController.navigate("categoria/$categoria")
                },
                onBuscar = { termo ->
                    navController.navigate("busca?termo=${Uri.encode(termo)}")
                },
                onVerificarAtualizacao = {
                    updateViewModel.verificar(manual = true)
                }
            )
        }

        composable(
            route = "categoria/{cat}",
            arguments = listOf(
                navArgument("cat") { type = NavType.StringType }
            )
        ) { entrada ->
            val categoria = entrada.arguments?.getString("cat") ?: "BRA"
            CategoriaScreen(
                catKey = categoria,
                repository = repository,
                onBack = { navController.popBackStack() },
                onHome = {
                    if (!navController.popBackStack("lista", inclusive = false)) {
                        navController.navigate("lista") {
                            launchSingleTop = true
                        }
                    }
                },
                onVerificarAtualizacao = {
                    updateViewModel.verificar(manual = true)
                }
            )
        }

        composable(
            route = "busca?termo={termo}",
            arguments = listOf(
                navArgument("termo") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entrada ->
            val termo = entrada.arguments?.getString("termo").orEmpty()
            BuscaScreen(
                initialTermo = termo,
                repository = repository,
                onBack = { navController.popBackStack() },
                onHome = {
                    if (!navController.popBackStack("lista", inclusive = false)) {
                        navController.navigate("lista") {
                            launchSingleTop = true
                        }
                    }
                },
                onVerificarAtualizacao = {
                    updateViewModel.verificar(manual = true)
                }
            )
        }
    }
}
