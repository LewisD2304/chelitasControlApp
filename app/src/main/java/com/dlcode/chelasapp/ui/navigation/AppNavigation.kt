package com.dlcode.chelasapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.dlcode.chelasapp.ui.screens.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel
import com.dlcode.chelasapp.ui.components.ChelasBottomBar
import com.dlcode.chelasapp.ui.components.ChelasTopBar

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Splash.route) {
                val title = when {
                    currentRoute == Screen.Inicio.route -> Screen.Inicio.title
                    currentRoute?.startsWith(Screen.Vender.route) == true -> Screen.Vender.title
                    currentRoute == Screen.Stock.route -> Screen.Stock.title
                    currentRoute == Screen.Reportes.route -> Screen.Reportes.title
                    currentRoute == Screen.Caja.route -> Screen.Caja.title
                    currentRoute == Screen.Apuntes.route -> Screen.Apuntes.title
                    currentRoute == Screen.Ajustes.route -> Screen.Ajustes.title
                    currentRoute == Screen.Historial.route -> Screen.Historial.title
                    currentRoute == Screen.Cierres.route -> Screen.Cierres.title
                    currentRoute == Screen.Fiados.route -> Screen.Fiados.title
                    currentRoute?.startsWith("venta_detalle") == true -> Screen.VentaDetalle.title
                    currentRoute?.startsWith("cierre_detalle") == true -> Screen.CierreDetalle.title
                    else -> "San Juan"
                }
                ChelasTopBar(
                    title = title,
                    showBack = navController.previousBackStackEntry != null && 
                               navController.previousBackStackEntry?.destination?.route != Screen.Splash.route,
                    onBack = { navController.popBackStack() },
                    onSettings = { navController.navigate(Screen.Ajustes.route) }
                )
            }
        },
        bottomBar = {
            val baseRoute = currentRoute?.split("?")?.first()
            if (baseRoute in listOf(
                    Screen.Inicio.route,
                    Screen.Vender.route,
                    Screen.Stock.route,
                    Screen.Reportes.route,
                    Screen.Caja.route
                )
            ) {
                ChelasBottomBar(
                    currentRoute = baseRoute ?: Screen.Inicio.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(if (currentRoute == Screen.Splash.route) PaddingValues(0.dp) else innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Inicio.route) { InicioScreen(navController, viewModel) }
            
            composable(
                route = Screen.Vender.route + "?tab={tab}",
                arguments = listOf(navArgument("tab") { defaultValue = "new" })
            ) { backStackEntry ->
                val initialTab = backStackEntry.arguments?.getString("tab") ?: "new"
                VenderScreen(navController, viewModel, initialTab)
            }
            
            composable(Screen.Stock.route) { StockScreen(navController, viewModel) }
            composable(Screen.Reportes.route) { ReportesScreen(navController, viewModel) }
            composable(Screen.Caja.route) { CajaScreen(navController, viewModel) }
            composable(Screen.Apuntes.route) { ApuntesScreen(viewModel) }
            composable(Screen.Ajustes.route) { AjustesScreen(viewModel) }
            composable(Screen.Historial.route) { 
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Vender.route + "?tab=history") {
                        popUpTo(Screen.Inicio.route)
                    }
                }
            }
            composable(Screen.Cierres.route) { CierresScreen(navController, viewModel) }
            composable(Screen.Fiados.route) { FiadosScreen(navController, viewModel) }
            
            composable(
                route = Screen.VentaDetalle.route,
                arguments = listOf(navArgument("saleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getLong("saleId") ?: 0L
                VentaDetalleScreen(saleId, viewModel, navController)
            }
            
            composable(
                route = Screen.CierreDetalle.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: ""
                CierreDetalleScreen(date, viewModel, navController)
            }
        }
    }
}
