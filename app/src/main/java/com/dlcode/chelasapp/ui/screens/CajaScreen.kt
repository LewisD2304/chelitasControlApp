package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
    var helpKey by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        if (activeJornada == null) {
            AbrirCajaForm(viewModel, onHelpRequest = { helpKey = it })
        } else {
            val jornada = activeJornada!!
            val date = jornada.date
            val daySales = allSales.filter { it.date == date && it.status == "active" }
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

            Surface(
                color = Color(0xFFE3EDCE),
                shape = RoundedCornerShape(7.dp)
            ) {
                Text(
                    text = "Caja abierta · ${formatPrettyDate(date)}",
                    color = Color(0xFF476234),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(17.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow("Fondo inicial en Caja", formatMoney(jornada.openingCash))
                    SummaryRow("Cobros en efectivo", formatMoney(cashSales + cashAbonos))
                    SummaryRow("Compras", "− ${formatMoney(cashPurchases)}")
                    SummaryRow("Gastos", "− ${formatMoney(cashExpenses)}")
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp)
                            .background(Color(0xFFEDF2E4), RoundedCornerShape(11.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Fondo de caja sobrante", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { helpKey = "expected" }, modifier = Modifier.size(36.dp)) {
                                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = Muted)
                                }
                            }
                            Text(formatMoney(expectedCash), style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp), fontWeight = FontWeight.Black)
                        }
                    }
                    
                    SummaryRow("Yape", formatMoney(yapeTotal))
                    SummaryRow("Plin", formatMoney(plinTotal))
                    SummaryRow("Tarjeta", formatMoney(tarjetaTotal))
                }
            }

            Spacer(modifier = Modifier.height(13.dp))
            
            var showExpenseDialog by remember { mutableStateOf(false) }
            PrimaryButton(
                text = "＋ Registrar gasto",
                onClick = { showExpenseDialog = true },
                color = Amber,
                contentColor = Color(0xFF45391F)
            )

            if (showExpenseDialog) {
                ExpenseDialog(
                    onDismiss = { showExpenseDialog = false },
                    onConfirm = { concept, amount ->
                        viewModel.registerExpense(concept, amount)
                        showExpenseDialog = false
                    }
                )
            }

            Text("Cerrar jornada", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 22.dp, bottom = 13.dp))

            CerrarCajaForm(viewModel, expectedCash, safeConfig.bottlesPerPack, onHelpRequest = { helpKey = it })
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        SecondaryButton(
            text = "Ver cierres registrados",
            onClick = { navController.navigate(Screen.Cierres.route) },
            modifier = Modifier.fillMaxWidth()
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
fun AbrirCajaForm(viewModel: BusinessViewModel, onHelpRequest: (String) -> Unit) {
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var openingCashText by remember { mutableStateOf("") }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Abrir jornada", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(modifier = Modifier.height(13.dp))
            
            Text("Fecha", style = MaterialTheme.typography.labelMedium)
            MoneyField(value = date, onValueChange = { date = it }, placeholder = "YYYY-MM-DD")

            Spacer(modifier = Modifier.height(13.dp))
            LabelWithHelp(label = "Fondo inicial en Caja", helpKey = "opening", onHelpClick = onHelpRequest)
            MoneyField(
                value = openingCashText,
                onValueChange = { openingCashText = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
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
fun ExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

    ConfirmDialog(
        title = "Registrar gasto",
        content = {
            Column {
                Text("Concepto", style = MaterialTheme.typography.labelMedium)
                TextField(
                    value = concept,
                    onValueChange = { concept = it },
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
                    shape = RoundedCornerShape(11.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Importe · S/", style = MaterialTheme.typography.labelMedium)
                MoneyField(value = amountText, onValueChange = { amountText = it })
            }
        },
        onConfirm = {
            val amount = amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
            if (concept.isNotBlank() && amount > 0) {
                onConfirm(concept, amount)
            }
        },
        onCancel = onDismiss
    )
}

@Composable
fun CerrarCajaForm(viewModel: BusinessViewModel, expectedCash: Long, packSize: Int, onHelpRequest: (String) -> Unit) {
    var countedCashText by remember { mutableStateOf("") }
    var boxes by remember { mutableStateOf(0) }
    var units by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    val inventoryState by viewModel.inventoryState.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Efectivo contado · S/", style = MaterialTheme.typography.labelMedium)
            MoneyField(value = countedCashText, onValueChange = { countedCashText = it })

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepperField(label = "Cajas físicas", value = boxes, onValueChange = { boxes = it }, modifier = Modifier.weight(1f))
                StepperField(label = "Botellas sueltas", value = units, onValueChange = { units = it }, modifier = Modifier.weight(1f))
            }

            val countedCash = countedCashText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
            val countedStock = boxes * packSize + units
            val diffCash = countedCash - expectedCash
            val diffStock = countedStock - inventoryState.stock
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp)
            ) {
                Text(
                    text = "Diferencia de efectivo: ${formatMoney(diffCash)} · Diferencia de stock: $diffStock botellas.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    lineHeight = 22.sp
                )
            }

            Text("Observaciones", style = MaterialTheme.typography.labelMedium)
            TextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Obligatorias si existen diferencias") },
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
                shape = RoundedCornerShape(11.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            val canClose = countedCashText.isNotBlank() && ( (diffCash == 0L && diffStock == 0) || note.isNotBlank() )

            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = "Cerrar jornada",
                enabled = canClose,
                onClick = {
                    viewModel.closeJornada(countedCash, boxes, units, note)
                }
            )
        }
    }
}
