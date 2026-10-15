package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dlcode.chelasapp.model.BusinessConfig
import com.dlcode.chelasapp.model.HelpDefinitions
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.navigation.Screen
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel
import kotlinx.coroutines.launch

@Composable
fun VenderScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel,
    initialTab: String = "new"
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val draft by viewModel.saleDraft.collectAsState()
    val config by viewModel.config.collectAsState()
    val inventoryState by viewModel.inventoryState.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    var helpKey by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(LightBg).padding(horizontal = 18.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3EBD6), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        text = "Nueva venta",
                        icon = Icons.Default.Add,
                        isSelected = selectedTab == "new",
                        onClick = { selectedTab = "new" },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "Historial de venta",
                        icon = Icons.Default.History,
                        isSelected = selectedTab == "history",
                        onClick = { selectedTab = "history" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .padding(padding)
        ) {
            if (selectedTab == "new") {
                NuevaVentaContent(
                    viewModel = viewModel,
                    draft = draft,
                    safeConfig = safeConfig,
                    inventoryStock = inventoryState.stock,
                    onHelpRequest = { helpKey = it },
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Venta registrada")
                        }
                    }
                )
            } else {
                HistorialVentaContent(
                    allSales = allSales,
                    navController = navController,
                    safeConfig = safeConfig
                )
            }
        }
    }

    helpKey?.let { key ->
        HelpDefinitions.definitions[key]?.let { info ->
            HelpDialog(
                title = info.title,
                description = info.description,
                onDismiss = { helpKey = null }
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) DarkGreen else Color.White
    val contentColor = if (isSelected) Color.White else Color(0xFF536B48)
    val borderColor = if (isSelected) DarkGreen else Color(0xFFCEDBC1)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(19.dp))
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun NuevaVentaContent(
    viewModel: BusinessViewModel,
    draft: BusinessViewModel.VentaDraft,
    safeConfig: BusinessConfig,
    inventoryStock: Int,
    onHelpRequest: (String) -> Unit,
    onSuccess: () -> Unit
) {
    if (safeConfig.bottlesPerPack == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Configura tu caja en Ajustes", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Price Notice
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${safeConfig.promoN} por ${formatMoney(safeConfig.promoPrice)} · Unidad ${formatMoney(safeConfig.unitPrice)}\nCaja helada ${formatMoney(safeConfig.boxPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quantity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(
                            label = "Cajas heladas",
                            value = draft.boxes,
                            onValueChange = { newValue -> viewModel.updateDraft { d -> d.copy(boxes = newValue) } },
                            helpKey = "quantity",
                            onHelpClick = onHelpRequest
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(
                            label = "Botellas sueltas",
                            value = draft.bottles,
                            onValueChange = { newValue -> viewModel.updateDraft { d -> d.copy(bottles = newValue) } },
                            helpKey = "quantity",
                            onHelpClick = onHelpRequest
                        )
                    }
                }

                val promoGroups = draft.bottles / safeConfig.promoN
                val remainderUnits = draft.bottles % safeConfig.promoN
                val boxTotal = draft.boxes * safeConfig.boxPrice
                val promoTotal = promoGroups * safeConfig.promoPrice
                val unitTotal = remainderUnits * safeConfig.unitPrice
                val subtotal = boxTotal + promoTotal + unitTotal
                val unitsToSell = (draft.boxes * safeConfig.bottlesPerPack) + draft.bottles

                Spacer(modifier = Modifier.height(13.dp))
                SummaryRow("Cajas", formatMoney(boxTotal))
                SummaryRow("$promoGroups promociones", formatMoney(promoTotal))
                SummaryRow("$remainderUnits unidades", formatMoney(unitTotal))

                val stockAfter = inventoryStock - unitsToSell
                Text(
                    text = if (unitsToSell > inventoryStock) "Stock insuficiente" else "Stock después: ${formatStock(stockAfter, safeConfig.bottlesPerPack)}",
                    color = if (unitsToSell > inventoryStock) Color.Red else Muted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Text(
            text = "Detalle del cobro",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 13.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LabelWithHelp(label = "Descuento adicional", helpKey = "discount", onHelpClick = onHelpRequest)
                MoneyField(
                    value = if (draft.discount == 0L) "" else (draft.discount / 100.0).toString(),
                    onValueChange = { text ->
                        val d = text.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                        viewModel.updateDraft { d2 -> d2.copy(discount = d) }
                    }
                )

                Spacer(modifier = Modifier.height(13.dp))
                Text("Forma de pago", style = MaterialTheme.typography.labelMedium)
                PaymentMethodDropdown(
                    selectedMethod = draft.method,
                    onMethodChange = { newMethod -> viewModel.updateDraft { d -> d.copy(method = newMethod) } }
                )

                Spacer(modifier = Modifier.height(13.dp))
                Text("Cliente", style = MaterialTheme.typography.labelMedium)
                TextField(
                    value = draft.client,
                    onValueChange = { newClient -> viewModel.updateDraft { d -> d.copy(client = newClient) } },
                    placeholder = { Text("Obligatorio si es fiado") },
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
                    shape = RoundedCornerShape(11.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                if (draft.method == "Efectivo") {
                    Spacer(modifier = Modifier.height(13.dp))
                    LabelWithHelp(label = "Efectivo recibido", helpKey = "received", onHelpClick = onHelpRequest)
                    MoneyField(
                        value = if (draft.cashReceived == 0L) "" else (draft.cashReceived / 100.0).toString(),
                        onValueChange = { text ->
                            val r = text.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                            viewModel.updateDraft { d -> d.copy(cashReceived = r) }
                        }
                    )
                } else if (draft.method == "Mixto") {
                    Spacer(modifier = Modifier.height(13.dp))
                    Text("Distribución de pagos mixtos", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.padding(top = 7.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Efectivo", style = MaterialTheme.typography.labelSmall)
                            MoneyField(value = if(draft.payEfectivo==0L) "" else (draft.payEfectivo/100.0).toString(), onValueChange = { text -> viewModel.updateDraft { d -> d.copy(payEfectivo = text.toDoubleOrNull()?.let{(it*100).toLong()}?:0L) } })
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Yape", style = MaterialTheme.typography.labelSmall)
                            MoneyField(value = if(draft.payYape==0L) "" else (draft.payYape/100.0).toString(), onValueChange = { text -> viewModel.updateDraft { d -> d.copy(payYape = text.toDoubleOrNull()?.let{(it*100).toLong()}?:0L) } })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.padding(top = 11.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Plin", style = MaterialTheme.typography.labelSmall)
                            MoneyField(value = if(draft.payPlin==0L) "" else (draft.payPlin/100.0).toString(), onValueChange = { text -> viewModel.updateDraft { d -> d.copy(payPlin = text.toDoubleOrNull()?.let{(it*100).toLong()}?:0L) } })
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tarjeta", style = MaterialTheme.typography.labelSmall)
                            MoneyField(value = if(draft.payTarjeta==0L) "" else (draft.payTarjeta/100.0).toString(), onValueChange = { text -> viewModel.updateDraft { d -> d.copy(payTarjeta = text.toDoubleOrNull()?.let{(it*100).toLong()}?:0L) } })
                        }
                    }
                }

                val promoGroups2 = draft.bottles / safeConfig.promoN
                val remainderUnits2 = draft.bottles % safeConfig.promoN
                val subtotal = (draft.boxes * safeConfig.boxPrice) + (promoGroups2 * safeConfig.promoPrice) + (remainderUnits2 * safeConfig.unitPrice)
                val total = (subtotal - draft.discount).coerceAtLeast(0)

                Spacer(modifier = Modifier.height(13.dp))
                SummaryRow("Total a cobrar", formatMoney(total), isBold = true)
                
                val hint = when (draft.method) {
                    "Efectivo" -> {
                        if (draft.cashReceived == 0L) "Ingresa lo recibido."
                        else if (draft.cashReceived >= total) "Vuelto: ${formatMoney(draft.cashReceived - total)}"
                        else "Falta: ${formatMoney(total - draft.cashReceived)}"
                    }
                    "Mixto" -> {
                        val sum = draft.payEfectivo + draft.payYape + draft.payPlin + draft.payTarjeta
                        "Asignado ${formatMoney(sum)} · Diferencia ${formatMoney(sum - total)}"
                    }
                    "Fiado" -> "Pendiente ${formatMoney(total)}"
                    else -> "${draft.method}: ${formatMoney(total)}"
                }
                Text(text = hint, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        val unitsToSell = (draft.boxes * safeConfig.bottlesPerPack) + draft.bottles
        val canSave = unitsToSell > 0 && unitsToSell <= inventoryStock && (draft.method != "Fiado" || draft.client.isNotBlank())
        
        PrimaryButton(
            text = "Registrar venta",
            enabled = canSave,
            onClick = {
                viewModel.registerSale(draft)
                onSuccess()
            }
        )
    }
}

@Composable
fun HistorialVentaContent(
    allSales: List<com.dlcode.chelasapp.model.Venta>,
    navController: NavHostController,
    safeConfig: BusinessConfig
) {
    var filterDate by remember { mutableStateOf("") }
    var showDeleted by remember { mutableStateOf(false) }

    val filteredSales = allSales.filter { 
        (filterDate.isEmpty() || it.date == filterDate) && (showDeleted || it.status != "deleted")
    }

    val groupedSales = filteredSales
        .groupBy { it.date }
        .toSortedMap(compareByDescending { it })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Text("Filtrar por fecha", style = MaterialTheme.typography.labelMedium)
        MoneyField(
            value = filterDate,
            onValueChange = { filterDate = it },
            placeholder = "YYYY-MM-DD"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryButton(
            text = "Ver todas las fechas",
            onClick = { filterDate = "" },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clickable { showDeleted = !showDeleted },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = showDeleted, onCheckedChange = { showDeleted = it })
            Text("Mostrar eliminadas / auditoría", style = MaterialTheme.typography.bodyMedium)
        }

        if (groupedSales.isEmpty()) {
            EmptyState(title = "Sin ventas registradas", subtitle = "Registra una venta en la pestaña Nueva venta.")
        } else {
            groupedSales.forEach { (date, sales) ->
                val liveSales = sales.filter { it.status == "active" }
                
                DailyGroupHeader(
                    date = date,
                    liveCount = liveSales.size
                )
                
                DailySummaryCard(liveSales)
                
                Text(
                    text = "OPERACIONES DE ESTA FECHA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Muted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                sales.forEach { sale ->
                    SaleCard(sale) {
                        navController.navigate(Screen.VentaDetalle.createRoute(sale.id))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyGroupHeader(date: String, liveCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp, bottom = 10.dp)
            .background(DarkGreen, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(41.dp)
                    .background(Color(0x2EFFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = White)
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column {
                Text("FECHA DE VENTAS", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCFE0C8), fontWeight = FontWeight.ExtraBold)
                Text(formatPrettyDate(date), style = MaterialTheme.typography.headlineMedium, color = White)
            }
        }
        
        Box(
            modifier = Modifier
                .background(Color(0xFFDCECB5), RoundedCornerShape(10.dp))
                .padding(9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$liveCount\n${if (liveCount == 1) "venta vigente" else "ventas vigentes"}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF284E25),
                fontWeight = FontWeight.ExtraBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun DailySummaryCard(sales: List<com.dlcode.chelasapp.model.Venta>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2E4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD9E3CC)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            SummaryRow("Total vendido", formatMoney(sales.sumOf { it.total }))
            SummaryRow("Cajas vendidas", sales.sumOf { it.boxes }.toString())
            SummaryRow("Botellas sueltas", sales.sumOf { it.bottles }.toString())
            SummaryRow("Botellas entregadas", sales.sumOf { it.units }.toString())
        }
    }
}

@Composable
fun SaleCard(sale: com.dlcode.chelasapp.model.Venta, onClick: () -> Unit) {
    val isActive = sale.status == "active"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (isActive) White else Color(0xFFFAF8F5)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) Color(0xFFD4DFCA) else Color(0xFFE6D9D0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(if (isActive) Color(0xFFE4EDDA) else Color(0xFFF5E7DF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "VENTA #${sale.id.toString().padStart(3, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isActive) Color(0xFF214B32) else Color(0xFF995943)
                    )
                }
                Text(
                    text = formatMoney(sale.total),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    color = Ink
                )
            }
            
            Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp), tint = Muted)
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "${sale.time} · ${sale.method}", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            
            Text(
                text = "${sale.boxes} cajas + ${sale.bottles} botellas sueltas",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4D6345),
                modifier = Modifier.padding(bottom = 9.dp)
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val statusText = when(sale.status) {
                    "active" -> if (sale.revisionCount > 0) "Corregida" else "Vigente"
                    "void" -> "Anulada · No suma"
                    else -> "Eliminada · No suma"
                }
                
                Surface(
                    color = if (isActive) Color(0xFFE3EDCE) else Color(0xFFFFE7E3),
                    shape = RoundedCornerShape(7.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) Color(0xFF476234) else Color(0xFFA24737)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ver detalle", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = DarkGreen)
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkGreen)
                }
            }
        }
    }
}
