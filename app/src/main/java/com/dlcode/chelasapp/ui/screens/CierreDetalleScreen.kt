package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dlcode.chelasapp.ui.components.SummaryRow
import com.dlcode.chelasapp.ui.components.formatMoney
import com.dlcode.chelasapp.ui.components.formatPrettyDate
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun CierreDetalleScreen(
    date: String,
    viewModel: BusinessViewModel,
    navController: NavHostController
) {
    val allJornadas by viewModel.allJornadas.collectAsState()
    val jornada = allJornadas.find { it.date == date }

    if (jornada == null || !jornada.isClosed) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cierre no encontrado")
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
        Text(
            text = "${formatPrettyDate(jornada.date)} · Apertura ${jornada.openingTime} · Cierre ${jornada.closingTime}",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ventas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
                SummaryRow("Ventas netas", formatMoney(jornada.snapshotSalesTotal), isBold = true)
                SummaryRow("Fiados del día", formatMoney(dayFiados(viewModel, date)))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Caja", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
                SummaryRow("Fondo inicial en Caja", formatMoney(jornada.openingCash))
                val expected = jornada.snapshotSalesTotal + jornada.openingCash - jornada.snapshotPurchasesTotal - jornada.snapshotExpensesTotal
                SummaryRow("Fondo de caja sobrante", formatMoney(expected))
                SummaryRow("Efectivo contado", formatMoney(jornada.closingCashCounted ?: 0), isBold = true)
                val diff = (jornada.closingCashCounted ?: 0) - expected
                SummaryRow("Diferencia", formatMoney(diff))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(17.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Inventario y resultado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink)
                SummaryRow("Stock contado", "${jornada.closingStockBoxes} cajas + ${jornada.closingStockUnits} botellas")
                SummaryRow("Utilidad bruta", formatMoney(jornada.snapshotSalesTotal - jornada.snapshotCostTotal))
                SummaryRow("Ganancia estimada", formatMoney(jornada.snapshotSalesTotal - jornada.snapshotCostTotal - jornada.snapshotExpensesTotal), isBold = true)
            }
        }

        if (!jornada.closingNote.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = jornada.closingNote,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun dayFiados(viewModel: BusinessViewModel, date: String): Long {
    val sales by viewModel.allSales.collectAsState()
    return sales.filter { it.date == date && it.status == "active" }.sumOf { it.creditAmount }
}
