package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dlcode.chelasapp.model.BusinessConfig
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.navigation.Screen
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CajaScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val activeJornada by viewModel.activeJornada.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val allPurchases by viewModel.allPurchases.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allAbonos by viewModel.allAbonos.collectAsState()
    val config by viewModel.config.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        if (activeJornada == null) {
            AbrirCajaForm(viewModel)
        } else {
            val jornada = activeJornada!!
            val date = jornada.date
            val daySales = allSales.filter { it.date == date }
            val dayPurchases = allPurchases.filter { it.date == date }
            val dayExpenses = allExpenses.filter { it.date == date }
            val dayAbonos = allAbonos.filter { it.date == date }

            val cashSales = daySales.sumOf { it.payEfectivo }
            val cashAbonos = dayAbonos.filter { it.method == "Efectivo" }.sumOf { it.amount }
            val cashPurchases = dayPurchases.sumOf { it.amount }
            val cashExpenses = dayExpenses.sumOf { it.amount }
            
            val expectedCash = jornada.openingCash + cashSales + cashAbonos - cashPurchases - cashExpenses
            
            val yapeTotal = daySales.sumOf { it.payYape } + dayAbonos.filter { it.method == "Yape" }.sumOf { it.amount }
            val plinTotal = daySales.sumOf { it.payPlin } + dayAbonos.filter { it.method == "Plin" }.sumOf { it.amount }
            val tarjetaTotal = daySales.sumOf { it.payTarjeta } + dayAbonos.filter { it.method == "Tarjeta" }.sumOf { it.amount }

            SuggestionChip(
                onClick = { },
                label = { Text(text = "Jornada abierta · $date", color = Color(0xFF52683C)) },
                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFE4EDCE)),
                border = null,
                shape = RoundedCornerShape(7.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    SummaryRow("Fondo inicial", formatMoney(jornada.openingCash))
                    SummaryRow("Ingresos en efectivo", formatMoney(cashSales + cashAbonos))
                    SummaryRow("Compras", "− ${formatMoney(cashPurchases)}")
                    SummaryRow("Gastos", "− ${formatMoney(cashExpenses)}")
                    SummaryRow("Efectivo esperado", formatMoney(expectedCash), isBold = true)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Text("COBROS DIGITALES", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow("Yape", formatMoney(yapeTotal))
                    SummaryRow("Plin", formatMoney(plinTotal))
                    SummaryRow("Tarjeta", formatMoney(tarjetaTotal))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            var showExpenseModal by remember { mutableStateOf(false) }
            SecondaryButton(
                text = "＋ Registrar gasto",
                onClick = { showExpenseModal = true },
                modifier = Modifier.fillMaxWidth()
            )

            if (showExpenseModal) {
                ExpenseModal(viewModel = viewModel, onDismiss = { showExpenseModal = false })
            }

            Text("Cerrar jornada", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 22.dp, horizontal = 0.dp))

            CerrarCajaForm(viewModel, expectedCash, safeConfig.bottlesPerPack)
        }

        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(
            text = "Cierres registrados",
            onClick = { navController.navigate(Screen.Cierres.route) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AbrirCajaForm(viewModel: BusinessViewModel) {
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var openingCashText by remember { mutableStateOf("") }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text("Abrir jornada", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Fecha", style = MaterialTheme.typography.labelSmall)
            TextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Efectivo inicial · S/", style = MaterialTheme.typography.labelSmall)
            TextField(
                value = openingCashText,
                onValueChange = { openingCashText = it },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
            )

            Spacer(modifier = Modifier.height(15.dp))
            PrimaryButton(
                text = "Abrir caja",
                onClick = {
                    val cash = openingCashText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
                    viewModel.openJornada(date, cash)
                }
            )
        }
    }
}

@Composable
fun ExpenseModal(viewModel: BusinessViewModel, onDismiss: () -> Unit) {
    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    
    ConfirmDialog(
        title = "Registrar gasto",
        content = {
            Column {
                Text("Concepto", style = MaterialTheme.typography.labelSmall)
                TextField(value = concept, onValueChange = { concept = it }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Importe en efectivo · S/", style = MaterialTheme.typography.labelSmall)
                TextField(value = amountText, onValueChange = { amountText = it }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        onConfirm = {
            val amount = amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
            if (concept.isNotBlank() && amount > 0) {
                viewModel.registerExpense(concept, amount)
                onDismiss()
            }
        },
        onCancel = onDismiss
    )
}

@Composable
fun CerrarCajaForm(viewModel: BusinessViewModel, expectedCash: Long, packSize: Int) {
    var countedCashText by remember { mutableStateOf("") }
    var boxes by remember { mutableStateOf(0) }
    var units by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    val inventoryState by viewModel.inventoryState.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text("Efectivo contado · S/", style = MaterialTheme.typography.labelSmall)
            TextField(
                value = countedCashText,
                onValueChange = { countedCashText = it },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    StepperField(label = "Cajas físicas", value = boxes, onValueChange = { boxes = it })
                }
                Box(modifier = Modifier.weight(1f)) {
                    StepperField(label = "Botellas sueltas", value = units, onValueChange = { units = it })
                }
            }

            val countedCash = countedCashText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
            val countedStock = boxes * packSize + units
            val diffCash = countedCash - expectedCash
            val diffStock = countedStock - inventoryState.stock
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Diferencia de efectivo: ${formatMoney(diffCash)} · Diferencia de stock: $diffStock botellas.",
                    modifier = Modifier.padding(13.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink
                )
            }

            Text("Observaciones", style = MaterialTheme.typography.labelSmall)
            TextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Obligatorias si existen diferencias") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
            )

            val canClose = countedCashText.isNotBlank() && ( (diffCash == 0L && diffStock == 0) || note.isNotBlank() )

            Spacer(modifier = Modifier.height(15.dp))
            PrimaryButton(
                text = "Revisar cierre",
                enabled = canClose,
                onClick = {
                    viewModel.closeJornada(countedCash, boxes, units, note)
                }
            )
        }
    }
}
