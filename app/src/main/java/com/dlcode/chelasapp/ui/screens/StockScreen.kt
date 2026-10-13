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
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(21.dp)) {
                    Text(text = "STOCK DISPONIBLE", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCDDFC9))
                    Text(
                        text = formatStock(inventoryState.stock, safeConfig.bottlesPerPack),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 27.sp, fontWeight = FontWeight.ExtraBold),
                        color = White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(text = "${inventoryState.stock} botellas en total", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6EAB2))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        label = "Valor al costo",
                        value = formatMoney((inventoryState.stock * inventoryState.averageCost).toLong()),
                        helpKey = "stockcost"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    MetricCard(
                        label = "Costo por botella",
                        value = formatMoney(inventoryState.averageCost.toLong()),
                        helpKey = "average"
                    )
                }
            }

            Text("Compras registradas", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 22.dp, horizontal = 0.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(16.dp)
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
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Compras pagadas desde caja, sin depósitos de envases.",
                style = MaterialTheme.typography.labelSmall,
                color = Muted
            )
        }
    }

    if (showPurchaseModal) {
        PurchaseModal(
            viewModel = viewModel,
            onDismiss = { showPurchaseModal = false }
        )
    }
}

@Composable
fun PurchaseItem(
    purchase: com.dlcode.chelasapp.model.Compra,
    packSize: Int,
    showDivider: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${purchase.boxes} cajas + ${purchase.bottles} botellas",
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "${purchase.date} · ${purchase.time} · Efectivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )
            }
            Text(
                text = formatMoney(purchase.amount),
                fontWeight = FontWeight.Bold,
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
    
    var boxes by remember { mutableStateOf(0) }
    var bottles by remember { mutableStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    
    val amount = amountText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
    val unitsToAdd = (boxes * safeConfig.bottlesPerPack) + bottles

    ConfirmDialog(
        title = "Registrar compra",
        content = {
            Column {
                Text(
                    text = "San Juan · ${safeConfig.bottlesPerPack} botellas por caja",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(label = "Cajas", value = boxes, onValueChange = { boxes = it })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StepperField(label = "Botellas sueltas", value = bottles, onValueChange = { bottles = it })
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Costo total · S/", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF0DC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ingresarán $unitsToAdd botellas. Quedarán ${formatStock(inventoryState.stock + unitsToAdd, safeConfig.bottlesPerPack)}",
                        modifier = Modifier.padding(13.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink
                    )
                }
            }
        },
        onConfirm = {
            if (unitsToAdd > 0 && amount > 0) {
                viewModel.registerPurchase(boxes, bottles, amount)
                onDismiss()
            }
        },
        onCancel = onDismiss
    )
}
