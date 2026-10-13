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
import com.dlcode.chelasapp.model.Venta
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun FiadosScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val allSales by viewModel.allSales.collectAsState()
    val allAbonos by viewModel.allAbonos.collectAsState()
    
    val pendingFiados = allSales.filter { sale ->
        val abonosTotal = allAbonos.filter { it.saleId == sale.id }.sumOf { it.amount }
        sale.creditAmount > abonosTotal
    }
    
    val totalPending = pendingFiados.sumOf { sale ->
        val abonosTotal = allAbonos.filter { it.saleId == sale.id }.sumOf { it.amount }
        sale.creditAmount - abonosTotal
    }

    var selectedSaleForAbono by remember { mutableStateOf<Venta?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(21.dp)) {
                Text(text = "PENDIENTE DE COBRO", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCDDFC9))
                Text(
                    text = formatMoney(totalPending),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold),
                    color = White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pendingFiados.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Sin fiados pendientes", fontWeight = FontWeight.Bold, color = Ink)
                    Text("Los abonos no duplican las ventas.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        } else {
            pendingFiados.forEach { sale ->
                val abonosTotal = allAbonos.filter { it.saleId == sale.id }.sumOf { it.amount }
                val saldo = sale.creditAmount - abonosTotal
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text(text = sale.client ?: "Cliente desconocido", fontWeight = FontWeight.Bold, color = Ink)
                        Text(text = "Venta #${sale.id} · ${sale.date}", style = MaterialTheme.typography.labelSmall, color = Muted)
                        SummaryRow("Saldo", formatMoney(saldo), isBold = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        SecondaryButton(
                            text = "Registrar abono",
                            onClick = { selectedSaleForAbono = sale },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (selectedSaleForAbono != null) {
        val sale = selectedSaleForAbono!!
        val abonosTotal = allAbonos.filter { it.saleId == sale.id }.sumOf { it.amount }
        val saldo = sale.creditAmount - abonosTotal
        
        AbonoModal(
            sale = sale,
            saldo = saldo,
            onDismiss = { selectedSaleForAbono = null },
            onConfirm = { amount, method ->
                viewModel.registerAbono(sale.id, amount, method)
                selectedSaleForAbono = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbonoModal(
    sale: Venta,
    saldo: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Efectivo") }
    
    ConfirmDialog(
        title = "Registrar abono",
        content = {
            Column {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "${sale.client}\nSaldo ${formatMoney(saldo)}",
                        modifier = Modifier.padding(13.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink
                    )
                }
                
                Text("Importe · S/", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Medio de pago", style = MaterialTheme.typography.labelSmall)
                val methods = listOf("Efectivo", "Yape", "Plin", "Tarjeta")
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        methods.forEach { m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { method = m; expanded = false })
                        }
                    }
                }
            }
        },
        onConfirm = {
            val amount = amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
            if (amount > 0 && amount <= saldo) {
                onConfirm(amount, method)
            }
        },
        onCancel = onDismiss
    )
}
