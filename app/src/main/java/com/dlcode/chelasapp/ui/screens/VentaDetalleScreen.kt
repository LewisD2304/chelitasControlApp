package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlcode.chelasapp.ui.components.SummaryRow
import com.dlcode.chelasapp.ui.components.formatMoney
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun VentaDetalleScreen(
    saleId: Long,
    viewModel: BusinessViewModel
) {
    val allSales by viewModel.allSales.collectAsState()
    val allAbonos by viewModel.allAbonos.collectAsState()
    
    val sale = allSales.find { it.id == saleId }
    val abonosForSale = allAbonos.filter { it.saleId == saleId }
    val abonosTotal = abonosForSale.sumOf { it.amount }

    if (sale == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
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
        Text(
            text = "${sale.date} · ${sale.time} · ${sale.client ?: "Sin nombre de cliente"}",
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
                SummaryRow("Cajas", sale.boxes.toString())
                SummaryRow("Botellas sueltas", sale.bottles.toString())
                SummaryRow("Botellas entregadas", sale.units.toString())
                SummaryRow("Botellas por caja", sale.appliedPackSize.toString())
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                SummaryRow("Cajas", formatMoney(sale.boxes * sale.appliedBoxPrice))
                val promoGroups = sale.bottles / sale.appliedPromoN
                val remainderUnits = sale.bottles % sale.appliedPromoN
                SummaryRow("$promoGroups promociones", formatMoney(promoGroups * sale.appliedPromoPrice))
                SummaryRow("$remainderUnits unidades restantes", formatMoney(remainderUnits * sale.appliedUnitPrice))
                SummaryRow("Descuento adicional", formatMoney(sale.discount))
                SummaryRow("Total", formatMoney(sale.total), isBold = true)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(text = sale.method, fontWeight = FontWeight.Bold, color = Ink)
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
                    SummaryRow("Saldo actual", formatMoney(sale.creditAmount - abonosTotal), isBold = true)
                }
            }
        }
    }
}
