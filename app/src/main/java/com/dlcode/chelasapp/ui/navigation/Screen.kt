package com.dlcode.chelasapp.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Inicio : Screen("inicio", "Mi resumen")
    object Vender : Screen("vender", "Nueva venta")
    object Stock : Screen("stock", "Mi inventario")
    object Reportes : Screen("reportes", "Mis resultados")
    object Caja : Screen("caja", "Mi caja")
    object Apuntes : Screen("apuntes", "Mis apuntes")
    object Ajustes : Screen("ajustes", "Configuración")
    object Historial : Screen("historial", "Ventas por día")
    object Cierres : Screen("cierres", "Cierres por día")
    object Fiados : Screen("fiados", "Fiados y abonos")
    object VentaDetalle : Screen("venta_detalle/{saleId}", "Detalle de Venta") {
        fun createRoute(saleId: Long) = "venta_detalle/$saleId"
    }
    object CierreDetalle : Screen("cierre_detalle/{date}", "Detalle de Cierre") {
        fun createRoute(date: String) = "cierre_detalle/$date"
    }
}
