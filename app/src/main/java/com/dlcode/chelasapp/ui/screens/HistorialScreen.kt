package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.navigation.Screen
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun HistorialScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val allSales by viewModel.allSales.collectAsState()
    var filterDate by remember { mutableStateOf("") }
    
    val groupedSales = allSales
        .filter { filterDate.isEmpty() || it.date == filterDate }
        .groupBy { it.date }
        .toSortedMap(compareByDescending { it })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Text("Filtrar fecha", style = MaterialTheme.typography.labelSmall)
        TextField(
            value = filterDate,
            onValueChange = { filterDate = it },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        SecondaryButton(
            text = "Todas las fechas",
            onClick = { filterDate = "" },
            modifier = Modifier.fillMaxWidth()
        )

        if (groupedSales.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(50.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Sin ventas", fontWeight = FontWeight.Bold, color = Ink)
                    Text("Aquí aparecerán agrupadas por día.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        } else {
            groupedSales.forEach { (date, sales) ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 22.dp, bottom = 11.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        SummaryRow("Total vendido", formatMoney(sales.sumOf { it.total }))
                        SummaryRow("Cajas vendidas", sales.sumOf { it.boxes }.toString())
                        SummaryRow("Botellas sueltas", sales.sumOf { it.bottles }.toString())
                        SummaryRow("Botellas entregadas", sales.sumOf { it.units }.toString())
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Line)
                        
                        sales.forEach { sale ->
                            SaleRecordItem(sale) {
                                navController.navigate(Screen.VentaDetalle.createRoute(sale.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SaleRecordItem(sale: com.dlcode.chelasapp.model.Venta, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Venta #${sale.id} · ${sale.time}", fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp)
            Text(text = formatMoney(sale.total), fontWeight = FontWeight.Bold, color = Ink, fontSize = 13.sp)
        }
        Text(
            text = "${sale.boxes} cajas + ${sale.bottles} botellas · ${sale.method} · Ver detalle",
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            modifier = Modifier.padding(top = 5.dp)
        )
        Spacer(modifier = Modifier.height(13.dp))
        HorizontalDivider(color = Line)
    }
}
