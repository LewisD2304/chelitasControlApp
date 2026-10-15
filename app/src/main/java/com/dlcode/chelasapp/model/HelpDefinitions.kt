package com.dlcode.chelasapp.model

data class HelpInfo(val title: String, val description: String)

object HelpDefinitions {
    val definitions = mapOf(
        "sales" to HelpInfo("Ventas netas", "Es toda la platita de las ventas de hoy que no has anulado. Aquí sumamos todo: lo que te pagaron en efectivo, por Yape, Plin y hasta lo que has dejado fiado."),
        "boxes" to HelpInfo("Cajas vendidas", "Son las cajas cerraditas de 12 botellas que ya entregaste. Las botellas que vendiste sueltas no se cuentan aquí."),
        "bottles" to HelpInfo("Botellas sueltas", "Son las botellas que vendiste una por una. También cuentan las que vendiste en oferta (como las de 3 por 20 soles)."),
        "units" to HelpInfo("Botellas entregadas", "Es el total de botellas que salieron de tu local hoy. Sumamos las que iban en cajas y las unidades sueltas."),
        "result" to HelpInfo("Ganancia estimada", "Es lo que te queda 'limpio' en el bolsillo. Agarramos tus ventas, le quitamos lo que te costó la cerveza y también restamos tus gastos (como el hielo o los pasajes)."),
        "gross" to HelpInfo("Utilidad bruta", "Es lo que ganas solo por vender la cerveza, sin contar todavía los gastos extra."),
        "expenses" to HelpInfo("Gastos registrados", "Pagos que hiciste hoy para que el negocio funcione, como comprar hielo, pagar transporte o limpieza."),
        "stock" to HelpInfo("Stock disponible", "Es la cantidad de cerveza que tienes guardada ahorita mismo para vender, contada en cajas y botellas."),
        "stockcost" to HelpInfo("Inversión en stock", "Es cuánta plata tienes 'amarrada' en la cerveza que todavía no vendes."),
        "opening" to HelpInfo("Fondo inicial en Caja", "Es el sencillo con el que empezaste el día para poder dar vuelto. No es una ganancia, es tu base."),
        "expected" to HelpInfo("Fondo de caja sobrante", "Es la plata que debería haber en tu cajón ahorita: lo que había al inicio + ventas y abonos en efectivo - compras y gastos pagados en efectivo."),
        "promo" to HelpInfo("Ofertas", "Son los precios especiales que el sistema pone solito cuando vendes varias botellas juntas para que salgan más rápido."),
        "discount" to HelpInfo("Descuento extra", "Es una rebajita adicional que tú decides hacerle al cliente al momento de cobrarle."),
        "purchase" to HelpInfo("Costo de compra", "Es lo que te cobraron por el lote de cerveza que estás metiendo a tu almacén. Se paga con la plata de la caja."),
        "quantity" to HelpInfo("Cantidades", "Usa los botones + y - para marcar cuántas cajas o botellas estás moviendo. Solo números enteros."),
        "edit" to HelpInfo("Correcciones", "Si te equivocaste al marcar, puedes arreglarlo siempre que la caja siga abierta. Guardamos un registro de los cambios por seguridad."),
        "credit" to HelpInfo("Fiados", "Cerveza que ya entregaste pero que el cliente todavía te debe. Cuando te den un adelanto, regístralo como 'Abono' para bajar su deuda."),
        "note" to HelpInfo("Mis apuntes", "Son recordatorios virtuales para que no se te olvide nada importante. No afectan tus cuentas de dinero ni tu inventario.")
    )
}
