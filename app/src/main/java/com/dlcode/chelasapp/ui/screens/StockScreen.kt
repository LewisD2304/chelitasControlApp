package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel
import kotlinx.coroutines.launch

@Composable
fun StockScreen(
    navController: NavHostController,
    viewModel: BusinessViewModel
) {
    val inventoryState by viewModel.inventoryState.collectAsState()
    val config by viewModel.config.collectAsState()
    val allPurchases by viewModel.allPurchases.collectAsState()
    val activeJornada by viewModel.activeJornada.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    var showPurchaseModal by remember { mutableStateOf(false) }
    var helpKey by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            if (activeJornada != null) {
                ExtendedFloatingActionButton(
                    onClick = { showPurchaseModal = true },
                    containerColor = DarkGreen,
                    contentColor = White,
                    shape = RoundedCornerShape(17.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("＋ Compra", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBg)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(18.dp)
        ) {
            // Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(21.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(text = "STOCK DISPONIBLE", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD9E8D3), fontWeight = FontWeight.Bold)
                    Text(
                        text = formatStock(inventoryState.stock, safeConfig.bottlesPerPack),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        color = White,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                    Text(text = "${inventoryState.stock} botellas en total", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6EAB2))
                }
            }

            Spacer(modifier = Modifier.height(13.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        label = "Inversión en stock",
                        value = formatMoney(inventoryState.totalValue),
                        helpKey = "stockcost",
                        onHelpClick = { helpKey = it }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        label = "Costo por botella",
                        value = formatMoney(if(inventoryState.stock > 0) (inventoryState.totalValue / inventoryState.stock) else 0L),
                        helpKey = "purchase",
                        onHelpClick = { helpKey = it }
                    )
                }
            }

            Text("Compras registradas", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 24.dp, bottom = 13.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(17.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    if (allPurchases.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(25.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sin compras", fontWeight = FontWeight.Bold, color = Ink)
                                Text("Usa el botón flotante «+ Compra».", style = MaterialTheme.typography.bodySmall, color = Muted)
                            }
                        }
                    } else {
                        allPurchases.forEachIndexed { index, purchase ->
                            PurchaseItem(
                                purchase = purchase,
                                packSize = safeConfig.bottlesPerPack,
                                showDivider = index < allPurchases.size - 1
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = "Compras pagadas desde caja. El costo de lo vendido se reconoce en tus ganancias.",
                style = MaterialTheme.typography.labelSmall,
                color = Muted,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showPurchaseModal) {
        PurchaseModal(
            viewModel = viewModel,
            onDismiss = { showPurchaseModal = false }
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
fun PurchaseItem(
    purchase: com.dlcode.chelasapp.model.Compra,
    packSize: Int,
    showDivider: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${purchase.boxes} cajas + ${purchase.bottles} botellas",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "${formatPrettyDate(purchase.date)} · ${purchase.time}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )
            }
            Text(
                text = formatMoney(purchase.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = Ink
            )
        }
        if (showDivider) HorizontalDivider(color = Line)
    }
}

@Composable
fun PurchaseModal(
    viewModel: BusinessViewModel,
    onDismiss: () -> Unit
) {
    val config by viewModel.config.collectAsState()
    val inventoryState by viewModel.inventoryState.collectAsState()
    val safeConfig = config ?: BusinessConfig()
    
    var boxes by remember { mutableIntStateOf(0) }
    var bottles by remember { mutableIntStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    
    val amount = amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
    val unitsToAdd = (boxes * safeConfig.bottlesPerPack) + bottles

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar compra", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(label = "Cajas", value = boxes, onValueChange = { boxes = it })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(label = "Botellas", value = bottles, onValueChange = { bottles = it })
                    }
                }
                
                Spacer(modifier = Modifier.height(13.dp))
                Text("Costo total · S/", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                MoneyField(
                    value = amountText,
                    onValueChange = { amountText = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ingresarán $unitsToAdd botellas. Quedarán ${formatStock(inventoryState.stock + unitsToAdd, safeConfig.bottlesPerPack)}",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        lineHeight = 22.sp
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Guardar compra",
                enabled = unitsToAdd > 0 && amount > 0,
                onClick = {
                    viewModel.registerPurchase(boxes, bottles, amount)
                    onDismiss()
                }
            )
        },
        dismissButton = {
            SecondaryButton(text = "Cancelar", onClick = onDismiss)
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(23.dp)
    )
}
