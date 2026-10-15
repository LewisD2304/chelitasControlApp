package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
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
fun ReportesScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val allSales by viewModel.allSales.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val config by viewModel.config.collectAsState()
    val activeJornada by viewModel.activeJornada.collectAsState()
    val inventoryState by viewModel.inventoryState.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    
    var period by remember { mutableIntStateOf(0) } // 0: Día, 1: 7 días, 2: Mes
    var helpKey by remember { mutableStateOf<String?>(null) }
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = activeJornada?.date ?: sdf.format(Date())
    
    val filteredSales = remember(allSales, period, today) {
        val startCalendar = Calendar.getInstance()
        val endDate = today
        
        val list = when (period) {
            0 -> allSales.filter { it.date == today }
            1 -> {
                startCalendar.add(Calendar.DAY_OF_YEAR, -6)
                val startDate = sdf.format(startCalendar.time)
                allSales.filter { it.date >= startDate && it.date <= endDate }
            }
            2 -> {
                val startDate = today.substring(0, 8) + "01"
                allSales.filter { it.date >= startDate && it.date <= endDate }
            }
            else -> emptyList()
        }
        list.filter { it.status == "active" }
    }
    
    val filteredExpenses = remember(allExpenses, period, today) {
        val startCalendar = Calendar.getInstance()
        val endDate = today
        when (period) {
            0 -> allExpenses.filter { it.date == today }
            1 -> {
                startCalendar.add(Calendar.DAY_OF_YEAR, -6)
                val startDate = sdf.format(startCalendar.time)
                allExpenses.filter { it.date >= startDate && it.date <= endDate }
            }
            2 -> {
                val startDate = today.substring(0, 8) + "01"
                allExpenses.filter { it.date >= startDate && it.date <= endDate }
            }
            else -> emptyList()
        }
    }

    val totalVendido = filteredSales.sumOf { it.total }
    val totalCost = filteredSales.sumOf { it.cost }
    val totalExpenses = filteredExpenses.sumOf { it.amount }
    val boxesSold = filteredSales.sumOf { it.boxes }
    val bottlesSold = filteredSales.sumOf { it.bottles }
    val totalUnits = filteredSales.sumOf { it.units }
    val resultadoEstimado = totalVendido - totalCost - totalExpenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE5ECD9), RoundedCornerShape(11.dp))
                .padding(4.dp)
        ) {
            listOf("Día", "7 días", "Mes").forEachIndexed { index, label ->
                Button(
                    onClick = { period = index },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (period == index) White else Color.Transparent,
                        contentColor = DarkGreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    elevation = null
                ) {
                    Text(text = label, fontWeight = if (period == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredSales.isEmpty() && filteredExpenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin datos vigentes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Ink)
                    Text("Anuladas y eliminadas no suman.", style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            // Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(21.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val title = if (period == 0) "GANANCIA ESTIMADA DEL DÍA" else if (period == 1) "GANANCIA ESTIMADA (7 DÍAS)" else "GANANCIA ESTIMADA DEL MES"
                        Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color(0xFFD9E8D3), fontWeight = FontWeight.Bold)
                        HelpIconButton(onClick = { helpKey = "result" }, tint = Color(0xFFD9E8D3))
                    }
                    Text(
                        text = formatMoney(resultadoEstimado),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                        color = if (resultadoEstimado < 0) Color(0xFFF3CE78) else White,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                    Text(text = "Ventas - Costos - Gastos", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6EAB2))
                }
            }

            Spacer(modifier = Modifier.height(13.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(label = "Total de ventas realizadas", value = formatMoney(totalVendido), helpKey = "sales", onHelpClick = { helpKey = it })
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(label = "Costo de cervezas vendidas", value = formatMoney(totalCost), helpKey = "gross", onHelpClick = { helpKey = it })
                }
            }
            Spacer(modifier = Modifier.height(11.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(label = "Cajas vendidas", value = boxesSold.toString(), helpKey = "boxes", onHelpClick = { helpKey = it })
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(label = "Botellas sueltas", value = bottlesSold.toString(), helpKey = "bottles", onHelpClick = { helpKey = it })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(17.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow("Botellas entregadas", totalUnits.toString(), helpKey = "units", onHelpClick = { helpKey = it })
                    SummaryRow("Gastos registrados", formatMoney(totalExpenses), helpKey = "expenses", onHelpClick = { helpKey = it })
                    SummaryRow("Inversión en stock actual", formatMoney(inventoryState.totalValue), helpKey = "stockcost", onHelpClick = { helpKey = it })
                }
            }

            // Simple Chart
            if (period > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    shape = RoundedCornerShape(17.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ventas por fecha · S/", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        SalesChart(sales = filteredSales)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        SecondaryButton(
            text = "Ver ventas por día",
            onClick = { navController.navigate(Screen.Vender.route + "?tab=history") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(40.dp))
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
fun SalesChart(sales: List<com.dlcode.chelasapp.model.Venta>) {
    val salesByDate = sales.groupBy { it.date }.mapValues { entry -> entry.value.sumOf { it.total } }.toSortedMap()
    if (salesByDate.isEmpty()) return
    
    val maxVal = salesByDate.values.maxOrNull()?.toDouble() ?: 1.0
    
    Row(
        modifier = Modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        salesByDate.forEach { (date, amount) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((amount / maxVal * 80).dp)
                        .background(Color(0xFFADC783), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Text(text = date.takeLast(2), style = MaterialTheme.typography.labelSmall, color = Muted, fontSize = 9.sp)
            }
        }
    }
}
