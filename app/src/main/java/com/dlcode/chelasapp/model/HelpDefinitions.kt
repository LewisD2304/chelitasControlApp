package com.dlcode.chelasapp.model

data class HelpInfo(val title: String, val description: String)

object HelpDefinitions {
    val definitions = mapOf(
        "sales" to HelpInfo("Ventas netas", "Total vendido después de promociones y descuentos. Incluye fiados; no equivale a dinero cobrado."),
        "boxes" to HelpInfo("Cajas vendidas", "Cajas completas registradas. Las botellas sueltas no se convierten en cajas en este indicador."),
        "bottles" to HelpInfo("Botellas sueltas", "Botellas vendidas fuera de las cajas, incluidas las que participan en promociones."),
        "units" to HelpInfo("Total de botellas", "Cajas × botellas por caja + botellas sueltas. Incluye todo lo entregado."),
        "gross" to HelpInfo("Utilidad bruta", "Ventas netas − costo de la cerveza vendida. Todavía no descuenta gastos."),
        "result" to HelpInfo("Resultado estimado", "Ventas netas − costo vendido − gastos registrados. Es distinto al efectivo disponible."),
        "expenses" to HelpInfo("Gastos", "Pagos de operación como hielo o transporte. Comprar cerveza se registra como compra de inventario."),
        "stock" to HelpInfo("Stock disponible", "Botellas disponibles agrupadas en cajas y sueltas. Es una equivalencia; no identifica cajas selladas ni temperatura."),
        "stockcost" to HelpInfo("Valor al costo", "Stock disponible × costo promedio por botella. No es el precio de venta."),
        "average" to HelpInfo("Costo promedio", "(Valor del stock anterior + costo de compra) ÷ botellas disponibles después de la compra."),
        "opening" to HelpInfo("Efectivo inicial", "Dinero físico al abrir la jornada. No cuenta como venta ni ganancia."),
        "expected" to HelpInfo("Efectivo esperado", "Fondo inicial + cobros en efectivo − compras en efectivo − gastos. No incluye pagos digitales."),
        "digital" to HelpInfo("Cobros digitales", "Cobros recibidos por Yape, Plin o tarjeta, incluidos abonos. No se suman al efectivo físico."),
        "credit" to HelpInfo("Fiados", "Ventas pendientes de cobro. Restan stock pero no generan dinero hasta registrar abonos."),
        "collection" to HelpInfo("Abonos", "Cobros de deudas: reducen el saldo y aumentan los cobros sin generar otra venta."),
        "promo" to HelpInfo("Promoción", "Cada grupo completo de botellas sueltas usa la promoción; las restantes usan precio unitario. Las cajas tienen precio independiente."),
        "discount" to HelpInfo("Descuento adicional", "Rebaja extra después de la promoción automática. No registres de nuevo el ahorro de la promoción."),
        "received" to HelpInfo("Efectivo recibido", "Dinero entregado por el cliente. Vuelto = recibido − total. La caja aumenta solo por el total de la venta."),
        "purchase" to HelpInfo("Costo de compra", "Importe pagado por toda la cerveza ingresada, sin depósitos de envases. Aumenta inventario y reduce efectivo."),
        "pack" to HelpInfo("Botellas por caja", "Cantidad real que contiene una caja de tu presentación. Se usa para convertir cajas en botellas.")
    )
}
