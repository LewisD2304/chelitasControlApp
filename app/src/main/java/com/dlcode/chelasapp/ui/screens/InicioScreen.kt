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

@Composable
fun InicioScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val activeJornada by viewModel.activeJornada.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val inventoryState by viewModel.inventoryState.collectAsState()
    val config by viewModel.config.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    var helpKey by remember { mutableStateOf<String?>(null) }

    val currentDate = activeJornada?.date ?: "Sin jornada"
    val daySales = allSales.filter { it.date == activeJornada?.date }
    val totalSales = daySales.sumOf { it.total }
    val boxesSold = daySales.sumOf { it.boxes }
    val bottlesSold = daySales.sumOf { it.bottles }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        SuggestionChip(
            onClick = { },
            label = { 
                Text(
                    text = "${if (activeJornada != null) "Jornada abierta" else "Caja cerrada"} · $currentDate",
                    color = Color(0xFF52683C)
                )
            },
            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFE4EDCE)),
            border = null,
            shape = RoundedCornerShape(7.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(21.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "VENTAS NETAS", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCDDFC9))
                    IconButton(onClick = { helpKey = "sales" }, modifier = Modifier.size(18.dp)) {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = "Ayuda", tint = Color(0xFFCDDFC9))
                    }
                }
                Text(
                    text = formatMoney(totalSales),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 34.sp, fontWeight = FontWeight.ExtraBold),
                    color = White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(text = "${daySales.size} ventas registradas", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6EAB2))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(label = "Cajas vendidas", value = boxesSold.toString(), helpKey = "boxes", onHelpClick = { helpKey = it })
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(label = "Botellas sueltas", value = bottlesSold.toString(), helpKey = "bottles", onHelpClick = { helpKey = it })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val buttonText = when {
            safeConfig.bottlesPerPack == 0 -> "Configurar caja"
            activeJornada == null -> "Abrir jornada"
            else -> "＋ Registrar venta"
        }
        val targetRoute = when {
            safeConfig.bottlesPerPack == 0 -> Screen.Ajustes.route
            activeJornada == null -> Screen.Caja.route
            else -> Screen.Vender.route
        }

        PrimaryButton(
            text = buttonText,
            onClick = { navController.navigate(targetRoute) },
            color = Amber,
            contentColor = Color(0xFF473A1F)
        )

        Spacer(modifier = Modifier.height(12.dp))

        MetricCard(
            label = "Stock disponible",
            value = formatStock(inventoryState.stock, safeConfig.bottlesPerPack),
            helpKey = "stock",
            onHelpClick = { helpKey = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of secondary buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(text = "Ventas por día", onClick = { navController.navigate(Screen.Historial.route) }, modifier = Modifier.weight(1f))
                SecondaryButton(text = "Cierres por día", onClick = { navController.navigate(Screen.Cierres.route) }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(text = "Fiados y abonos", onClick = { navController.navigate(Screen.Fiados.route) }, modifier = Modifier.weight(1f))
                SecondaryButton(text = "Mis apuntes", onClick = { navController.navigate(Screen.Apuntes.route) }, modifier = Modifier.weight(1f))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Guardado local en este dispositivo.",
            style = MaterialTheme.typography.labelSmall,
            color = Muted
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
