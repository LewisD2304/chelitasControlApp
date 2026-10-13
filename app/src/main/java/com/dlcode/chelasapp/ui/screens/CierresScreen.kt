package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.dlcode.chelasapp.ui.components.formatMoney
import com.dlcode.chelasapp.ui.navigation.Screen
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun CierresScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val allJornadas by viewModel.allJornadas.collectAsState()
    val closedJornadas = allJornadas.filter { it.isClosed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        if (closedJornadas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(50.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin cierres", fontWeight = FontWeight.Bold, color = Ink)
                    Text("Aquí se conservarán tus cierres por día.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        } else {
            closedJornadas.forEach { jornada ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            navController.navigate(Screen.CierreDetalle.createRoute(jornada.date))
                        },
                    colors = CardDefaults.cardColors(containerColor = White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = jornada.date, fontWeight = FontWeight.Bold, color = Ink, fontSize = 15.sp)
                            SuggestionChip(
                                onClick = { },
                                label = { Text("Cerrado", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFFE4EDCE),
                                    labelColor = Color(0xFF52683C)
                                ),
                                border = null,
                                shape = RoundedCornerShape(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ventas: ${formatMoney(jornada.snapshotSalesTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                        Text(
                            text = "Efectivo contado: ${formatMoney(jornada.closingCashCounted ?: 0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ver detalle completo →",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
