package sample.app.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import sample.app.barcode.BarcodeScanScreen
import sample.app.barcode.BarcodeScanViewModel
import sample.app.main.MainScreen
import sample.app.utils.ViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavigationItem.Home) {
        composable<NavigationItem.Home> {
            MainScreen(onItemClick = {
                navController.navigate(it)
            })
        }
        composable<NavigationItem.Barcode> {
            val viewModel: BarcodeScanViewModel = remember { BarcodeScanViewModel() }
            val state by viewModel.state.collectAsStateWithLifecycle()
            BarcodeScanScreen(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onAction = viewModel::onAction,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }

}