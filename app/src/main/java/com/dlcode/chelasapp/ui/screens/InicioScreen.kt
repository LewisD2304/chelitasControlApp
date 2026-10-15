package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val dayExpenses by viewModel.allExpenses.collectAsState()
    val inventoryState by viewModel.inventoryState.collectAsState()
    val config by viewModel.config.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    var helpKey by remember { mutableStateOf<String?>(null) }

    val currentDate = activeJornada?.date ?: "Sin jornada"
    val daySales = allSales.filter { it.date == activeJornada?.date && it.status == "active" }
    val currentDayExpenses = dayExpenses.filter { it.date == activeJornada?.date }
    
    val totalSales = daySales.sumOf { it.total }
    val boxesSold = daySales.sumOf { it.boxes }
    val bottlesSold = daySales.sumOf { it.bottles }
    val totalCost = daySales.sumOf { it.cost }
    val totalExp = currentDayExpenses.sumOf { it.amount }
    val netProfit = totalSales - totalCost - totalExp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Surface(
            color = Color(0xFFE3EDCE),
            shape = RoundedCornerShape(7.dp)
        ) {
            Text(
                text = "${if (activeJornada != null) "Caja abierta" else "Sin caja abierta"} · ${formatPrettyDate(currentDate)}",
                color = Color(0xFF476234),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkGreen),
            shape = RoundedCornerShape(21.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "TOTAL DE VENTAS REALIZADAS", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD9E8D3), fontWeight = FontWeight.Bold)
                    HelpIconButton(onClick = { helpKey = "sales" }, tint = Color(0xFFD9E8D3))
                }
                Text(
                    text = formatMoney(totalSales),
                    style = MaterialTheme.typography.displayLarge,
                    color = White,
                    modifier = Modifier.padding(vertical = 7.dp)
                )
                Text(text = "${daySales.size} ventas vigentes", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6EAB2))
            }
        }

        if (daySales.isNotEmpty() || currentDayExpenses.isNotEmpty()) {
            Spacer(modifier = Modifier.height(13.dp))
            ProfitHighlightCard(
                title = "Ganancia estimada del día",
                amount = netProfit,
                onHelpClick = { helpKey = "result" }
            )
        }

        Spacer(modifier = Modifier.height(13.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(label = "Cajas vendidas", value = boxesSold.toString(), helpKey = "boxes", onHelpClick = { helpKey = it })
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard(label = "Botellas sueltas", value = bottlesSold.toString(), helpKey = "bottles", onHelpClick = { helpKey = it })
            }
        }

        Spacer(modifier = Modifier.height(13.dp))

        val buttonText = when {
            safeConfig.bottlesPerPack == 0 -> "Configurar mi caja"
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
            contentColor = Color(0xFF45391F)
        )

        Spacer(modifier = Modifier.height(13.dp))

        MetricCard(
            label = "Stock disponible",
            value = formatStock(inventoryState.stock, safeConfig.bottlesPerPack),
            helpKey = "stock",
            onHelpClick = { helpKey = it }
        )

        Text("Control de tu negocio", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 24.dp, bottom = 13.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            QuickAccessCard(
                title = "Ventas por día",
                subtitle = "Consulta y corrige",
                icon = Icons.Default.History,
                onClick = { navController.navigate(Screen.Vender.route + "?tab=history") },
                modifier = Modifier.weight(1f)
            )
            QuickAccessCard(
                title = "Cierres por día",
                subtitle = "Revisa tus jornadas",
                icon = Icons.Default.Close,
                onClick = { navController.navigate(Screen.Cierres.route) },
                colorClass = "blue",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(11.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            QuickAccessCard(
                title = "Fiados y abonos",
                subtitle = "Controla lo pendiente",
                icon = Icons.Default.CreditCard,
                onClick = { navController.navigate(Screen.Fiados.route) },
                colorClass = "purple",
                modifier = Modifier.weight(1f)
            )
            QuickAccessCard(
                title = "Mis apuntes",
                subtitle = "Organiza pendientes",
                icon = Icons.Default.NoteAlt,
                onClick = { navController.navigate(Screen.Apuntes.route) },
                colorClass = "gold",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(28.dp))
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
fun ProfitHighlightCard(
    title: String,
    amount: Long,
    onHelpClick: () -> Unit
) {
    val isNegative = amount < 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isNegative) Color(0xFFFFF0E4) else Color(0xFFE0EDC9)
        ),
        border = BorderStroke(1.dp, if (isNegative) Color(0xFFE5BB9A) else Color(0xFFB3CC88)),
        shape = RoundedCornerShape(21.dp)
    ) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 4.dp, top = 8.dp, bottom = 20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = Ink, fontWeight = FontWeight.Bold)
                HelpIconButton(onClick = onHelpClick)
            }
            Text(
                text = formatMoney(amount),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                color = if (isNegative) Color(0xFF964925) else Color(0xFF204B2C),
                modifier = Modifier.padding(vertical = 0.dp)
            )
            Text(
                text = "Solo ventas vigentes. Se actualiza al corregir.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
                modifier = Modifier.padding(start = 0.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorClass: String = ""
) {
    val bgColor = when(colorClass) {
        "blue" -> Color(0xFFF0F6FB)
        "purple" -> Color(0xFFF7F3FC)
        "gold" -> Color(0xFFFFF9EA)
        else -> Color.White
    }
    val borderColor = when(colorClass) {
        "blue" -> Color(0xFFC6D9E7)
        "purple" -> Color(0xFFD8CEE7)
        "gold" -> Color(0xFFE8D6A6)
        else -> Color(0xFFCEDBC1)
    }

    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(17.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.size(43.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = Color(0xFFE5EFDA)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = DarkGreen
                    )
                }
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, color = Ink, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF607354))
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).size(20.dp),
                tint = Color(0xFF597344)
            )
        }
    }
}
