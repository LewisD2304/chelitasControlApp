package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import com.dlcode.chelasapp.model.HelpDefinitions
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun VentaDetalleScreen(
    saleId: Long,
    viewModel: BusinessViewModel,
    navController: NavHostController
) {
    val allSales by viewModel.allSales.collectAsState()
    val allAbonos by viewModel.allAbonos.collectAsState()
    val activeJornada by viewModel.activeJornada.collectAsState()
    
    val sale = allSales.find { it.id == saleId }
    val abonosForSale = allAbonos.filter { it.saleId == saleId }
    val abonosTotal = abonosForSale.sumOf { it.amount }
    
    val isEditable = sale != null && sale.status == "active" && activeJornada?.date == sale.date
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showRevertDialog by remember { mutableStateOf<String?>(null) } // "void" or "deleted"
    var helpKey by remember { mutableStateOf<String?>(null) }

    if (sale == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Venta no encontrada")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Surface(
            color = if (sale.status == "active") Color(0xFFE3EDCE) else Color(0xFFFFE7E3),
            shape = RoundedCornerShape(7.dp)
        ) {
            val statusText = when(sale.status) {
                "active" -> if (sale.revisionCount > 0) "Corregida" else "Vigente"
                "void" -> "Anulada"
                else -> "Eliminada"
            }
            Text(
                text = statusText,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (sale.status == "active") Color(0xFF476234) else Color(0xFFA24737)
            )
        }

        Text(
            text = "${formatPrettyDate(sale.date)} · ${sale.time} · ${sale.client ?: "Sin nombre de cliente"}",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
            modifier = Modifier.padding(top = 8.dp, bottom = 13.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Cajas", sale.boxes.toString())
                SummaryRow("Botellas sueltas", sale.bottles.toString())
                SummaryRow("Total de botellas", sale.units.toString())
                SummaryRow("Promociones", (sale.bottles / sale.appliedPromoN).toString())
                SummaryRow("Descuento", formatMoney(sale.discount))
                SummaryRow("Total", formatMoney(sale.total), isBold = true)
            }
        }

        Spacer(modifier = Modifier.height(13.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Forma de pago: ${sale.method}", fontWeight = FontWeight.Black, color = Ink, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                if (sale.payEfectivo > 0) SummaryRow("Efectivo", formatMoney(sale.payEfectivo))
                if (sale.payYape > 0) SummaryRow("Yape", formatMoney(sale.payYape))
                if (sale.payPlin > 0) SummaryRow("Plin", formatMoney(sale.payPlin))
                if (sale.payTarjeta > 0) SummaryRow("Tarjeta", formatMoney(sale.payTarjeta))
                
                if (sale.method == "Efectivo") {
                    SummaryRow("Recibido", formatMoney(sale.cashReceived))
                    SummaryRow("Vuelto", formatMoney(sale.change))
                }
                
                if (sale.creditAmount > 0) {
                    SummaryRow("Fiado original", formatMoney(sale.creditAmount))
                    SummaryRow("Saldo pendiente", formatMoney(sale.creditAmount - abonosTotal), isBold = true)
                }
            }
        }

        if (isEditable) {
            Spacer(modifier = Modifier.height(13.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEAF0DC), RoundedCornerShape(13.dp))
                    .padding(horizontal = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Puedes corregir con la caja abierta.", style = MaterialTheme.typography.bodyMedium, color = Ink, modifier = Modifier.weight(1f))
                    HelpIconButton(onClick = { helpKey = "edit" })
                }
            }
            
            Spacer(modifier = Modifier.height(13.dp))
            PrimaryButton(
                text = "✎ Editar venta",
                onClick = { showEditDialog = true }
            )
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 11.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                SecondaryButton(
                    text = "Anular venta",
                    onClick = { showRevertDialog = "void" },
                    modifier = Modifier.weight(1f),
                    color = RedInfo
                )
                SecondaryButton(
                    text = "Eliminar del listado",
                    onClick = { showRevertDialog = "deleted" },
                    modifier = Modifier.weight(1f),
                    color = RedInfo
                )
            }
        } else if (sale.status == "active") {
            Spacer(modifier = Modifier.height(13.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F6EE), RoundedCornerShape(13.dp))
                    .padding(14.dp)
            ) {
                Text("Caja cerrada: cambios bloqueados.", style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showEditDialog) {
        EditSaleDialog(
            sale = sale,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onSuccess = {
                showEditDialog = false
                navController.popBackStack()
            }
        )
    }

    showRevertDialog?.let { status ->
        RevertSaleDialog(
            sale = sale,
            status = status,
            viewModel = viewModel,
            onDismiss = { showRevertDialog = null },
            onSuccess = {
                showRevertDialog = null
                navController.popBackStack()
            }
        )
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
fun EditSaleDialog(
    sale: com.dlcode.chelasapp.model.Venta,
    viewModel: BusinessViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var boxes by remember { mutableIntStateOf(sale.boxes) }
    var bottles by remember { mutableIntStateOf(sale.bottles) }
    var discountText by remember { mutableStateOf((sale.discount / 100.0).toString()) }
    var method by remember { mutableStateOf(sale.method) }
    var client by remember { mutableStateOf(sale.client ?: "") }
    var receivedText by remember { mutableStateOf((sale.cashReceived / 100.0).toString()) }
    var reason by remember { mutableStateOf("") }
    
    val discount = discountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
    val promoGroups = bottles / sale.appliedPromoN
    val remainderUnits = bottles % sale.appliedPromoN
    val subtotal = (boxes * sale.appliedBoxPrice) + (promoGroups * sale.appliedPromoPrice) + (remainderUnits * sale.appliedUnitPrice)
    val total = (subtotal - discount).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar venta #${sale.id.toString().padStart(3, '0')}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(label = "Cajas", value = boxes, onValueChange = { boxes = it })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(label = "Botellas", value = bottles, onValueChange = { bottles = it })
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow("Nuevo total", formatMoney(total), isBold = true)
                
                Spacer(modifier = Modifier.height(11.dp))
                Text("Descuento adicional", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                MoneyField(value = discountText, onValueChange = { discountText = it })
                
                Spacer(modifier = Modifier.height(11.dp))
                Text("Forma de pago", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                PaymentMethodDropdown(selectedMethod = method, onMethodChange = { method = it })
                
                Spacer(modifier = Modifier.height(11.dp))
                Text("Cliente", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                TextField(
                    value = client,
                    onValueChange = { client = it },
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
                    shape = RoundedCornerShape(11.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                if (method == "Efectivo") {
                    Spacer(modifier = Modifier.height(11.dp))
                    Text("Recibido · S/", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    MoneyField(value = receivedText, onValueChange = { receivedText = it })
                }
                
                Spacer(modifier = Modifier.height(11.dp))
                Text("Motivo de la corrección", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Obligatorio") },
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
                    shape = RoundedCornerShape(11.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Guardar cambios",
                enabled = reason.isNotBlank(),
                onClick = {
                    val draft = BusinessViewModel.VentaDraft(
                        boxes = boxes,
                        bottles = bottles,
                        discount = discount,
                        method = method,
                        client = client,
                        cashReceived = receivedText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                    )
                    viewModel.updateSale(sale.id, draft, reason) { ok, _ ->
                        if (ok) onSuccess()
                    }
                }
            )
        },
        dismissButton = {
            SecondaryButton(text = "Cancelar", onClick = onDismiss)
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(23.dp)
    )
}

@Composable
fun RevertSaleDialog(
    sale: com.dlcode.chelasapp.model.Venta,
    status: String,
    viewModel: BusinessViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (status == "void") "Anular venta #${sale.id}" else "Eliminar del listado #${sale.id}", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedBg, RoundedCornerShape(13.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Se devolverán ${sale.units} botellas y se retirará ${formatMoney(sale.total)} de los totales.",
                        color = RedInfo,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(13.dp))
                Text("Motivo obligatorio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
                    placeholder = { Text("Ej. Registro duplicado") },
                    shape = RoundedCornerShape(11.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Se conservará la auditoría del cambio.", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Confirmar",
                onClick = {
                    viewModel.revertSale(sale.id, status, reason) { ok, _ ->
                        if (ok) onSuccess()
                    }
                },
                enabled = reason.isNotBlank(),
                color = RedInfo
            )
        },
        dismissButton = {
            SecondaryButton(text = "Cancelar", onClick = onDismiss)
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(23.dp)
    )
}
