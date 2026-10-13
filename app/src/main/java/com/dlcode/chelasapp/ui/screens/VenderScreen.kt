package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dlcode.chelasapp.model.BusinessConfig
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VenderScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val config by viewModel.config.collectAsState()
    val activeJornada by viewModel.activeJornada.collectAsState()
    val inventoryState by viewModel.inventoryState.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (safeConfig.bottlesPerPack == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Configura tu caja primero")
        }
        return
    }

    var boxes by remember { mutableStateOf(0) }
    var bottles by remember { mutableStateOf(0) }
    var discountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Efectivo") }
    var client by remember { mutableStateOf("") }
    var receivedText by remember { mutableStateOf("") }
    
    var mixEfectivo by remember { mutableStateOf("") }
    var mixYape by remember { mutableStateOf("") }
    var mixPlin by remember { mutableStateOf("") }
    var mixTarjeta by remember { mutableStateOf("") }

    val discount = discountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
    
    val promoGroups = bottles / safeConfig.promoN
    val remainderUnits = bottles % safeConfig.promoN
    val boxTotal = boxes * safeConfig.boxPrice
    val promoTotal = promoGroups * safeConfig.promoPrice
    val unitTotal = remainderUnits * safeConfig.unitPrice
    val subtotal = boxTotal + promoTotal + unitTotal
    val total = (subtotal - discount).coerceAtLeast(0)
    val unitsToSell = (boxes * safeConfig.bottlesPerPack) + bottles

    val canSave = unitsToSell > 0 && unitsToSell <= inventoryState.stock && (method != "Fiado" || client.isNotBlank())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(18.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(13.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "${safeConfig.promoN} por ${formatMoney(safeConfig.promoPrice)} · Unidad ${formatMoney(safeConfig.unitPrice)}\nCaja helada ${formatMoney(safeConfig.boxPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink
                    )
                }
            }

            Text(
                text = "Disponible: ${formatStock(inventoryState.stock, safeConfig.bottlesPerPack)}",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                modifier = Modifier.padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            StepperField(label = "Cajas heladas", value = boxes, onValueChange = { boxes = it })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            StepperField(label = "Botellas sueltas", value = bottles, onValueChange = { bottles = it })
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow("Cajas", formatMoney(boxTotal))
                    SummaryRow("$promoGroups promociones", formatMoney(promoTotal))
                    SummaryRow("$remainderUnits unidades restantes", formatMoney(unitTotal))
                    
                    if (unitsToSell > inventoryState.stock) {
                        Text("Stock insuficiente", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                    } else {
                        Text("Quedarán ${formatStock(inventoryState.stock - unitsToSell, safeConfig.bottlesPerPack)}", color = Muted, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Text("Detalle del cobro", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Text("Descuento adicional · S/", style = MaterialTheme.typography.labelSmall)
                    TextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Forma de pago", style = MaterialTheme.typography.labelSmall)
                    val methods = listOf("Efectivo", "Yape", "Plin", "Tarjeta", "Mixto", "Fiado")
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = method,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                            colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            methods.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = { method = m; expanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cliente", style = MaterialTheme.typography.labelSmall)
                    TextField(
                        value = client,
                        onValueChange = { client = it },
                        placeholder = { Text("Obligatorio para fiado") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    when (method) {
                        "Efectivo" -> {
                            Text("Efectivo recibido · S/", style = MaterialTheme.typography.labelSmall)
                            TextField(
                                value = receivedText,
                                onValueChange = { receivedText = it },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                            )
                        }
                        "Mixto" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Efectivo S/", style = MaterialTheme.typography.labelSmall)
                                    TextField(value = mixEfectivo, onValueChange = { mixEfectivo = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Yape S/", style = MaterialTheme.typography.labelSmall)
                                    TextField(value = mixYape, onValueChange = { mixYape = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Plin S/", style = MaterialTheme.typography.labelSmall)
                                    TextField(value = mixPlin, onValueChange = { mixPlin = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tarjeta S/", style = MaterialTheme.typography.labelSmall)
                                    TextField(value = mixTarjeta, onValueChange = { mixTarjeta = it }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryRow("Total a cobrar", formatMoney(total), isBold = true)
                    
                    val hint = when (method) {
                        "Efectivo" -> {
                            val r = receivedText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                            if (receivedText.isBlank()) "Ingresa el efectivo recibido."
                            else if (r >= total) "Vuelto: ${formatMoney(r - total)}"
                            else "Falta: ${formatMoney(total - r)}"
                        }
                        "Mixto" -> {
                            val sum = (mixEfectivo.toDoubleOrNull() ?: 0.0) + (mixYape.toDoubleOrNull() ?: 0.0) + (mixPlin.toDoubleOrNull() ?: 0.0) + (mixTarjeta.toDoubleOrNull() ?: 0.0)
                            val sumCents = (sum * 100).toLong()
                            "Asignado: ${formatMoney(sumCents)} · Diferencia: ${formatMoney(sumCents - total)}"
                        }
                        "Fiado" -> "Pendiente: ${formatMoney(total)}"
                        else -> "Cobro por $method: ${formatMoney(total)}"
                    }
                    Text(text = hint, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(
                text = "Registrar venta",
                enabled = canSave,
                onClick = {
                    val pay = mutableMapOf<String, Long>()
                    if (method == "Mixto") {
                        pay["Efectivo"] = (mixEfectivo.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
                        pay["Yape"] = (mixYape.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
                        pay["Plin"] = (mixPlin.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
                        pay["Tarjeta"] = (mixTarjeta.toDoubleOrNull() ?: 0.0).let { (it * 100).toLong() }
                    } else if (method != "Fiado") {
                        pay[method] = total
                    }
                    
                    val received = receivedText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                    
                    viewModel.registerSale(boxes, bottles, discount, method, client, pay, received)
                    scope.launch {
                        snackbarHostState.showSnackbar("Venta registrada")
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
