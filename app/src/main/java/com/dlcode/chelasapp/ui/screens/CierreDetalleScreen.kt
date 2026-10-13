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
import com.dlcode.chelasapp.ui.components.SummaryRow
import com.dlcode.chelasapp.ui.components.formatMoney
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun CierreDetalleScreen(
    date: String,
    viewModel: BusinessViewModel
) {
    val allJornadas by viewModel.allJornadas.collectAsState()
    val jornada = allJornadas.find { it.date == date }

    if (jornada == null || !jornada.isClosed) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
            text = "Apertura ${jornada.openingTime} · Cierre ${jornada.closingTime}",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Ventas", fontWeight = FontWeight.Bold, color = Ink)
                SummaryRow("Ventas netas", formatMoney(jornada.snapshotSalesTotal), isBold = true)
                SummaryRow("Fiados del día", formatMoney(dayFiados(viewModel, date)))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Caja", fontWeight = FontWeight.Bold, color = Ink)
                SummaryRow("Fondo inicial", formatMoney(jornada.openingCash))
                SummaryRow("Esperado", formatMoney(jornada.snapshotSalesTotal + jornada.openingCash - jornada.snapshotPurchasesTotal - jornada.snapshotExpensesTotal))
                SummaryRow("Contado", formatMoney(jornada.closingCashCounted ?: 0), isBold = true)
                val diff = (jornada.closingCashCounted ?: 0) - (jornada.snapshotSalesTotal + jornada.openingCash - jornada.snapshotPurchasesTotal - jornada.snapshotExpensesTotal)
                SummaryRow("Diferencia", formatMoney(diff))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Inventario y resultado", fontWeight = FontWeight.Bold, color = Ink)
                SummaryRow("Stock contado", "${jornada.closingStockBoxes} cajas + ${jornada.closingStockUnits} botellas")
                SummaryRow("Utilidad bruta", formatMoney(jornada.snapshotSalesTotal - jornada.snapshotCostTotal))
                SummaryRow("Resultado estimado", formatMoney(jornada.snapshotSalesTotal - jornada.snapshotCostTotal - jornada.snapshotExpensesTotal), isBold = true)
            }
        }

        if (!jornada.closingNote.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = jornada.closingNote,
                    modifier = Modifier.padding(13.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink
                )
            }
        }
    }
}

@Composable
fun dayFiados(viewModel: BusinessViewModel, date: String): Long {
    val sales by viewModel.allSales.collectAsState()
    return sales.filter { it.date == date }.sumOf { it.creditAmount }
}
