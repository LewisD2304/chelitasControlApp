package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dlcode.chelasapp.model.BusinessConfig
import com.dlcode.chelasapp.model.HelpDefinitions
import com.dlcode.chelasapp.ui.components.*
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@Composable
fun AjustesScreen(viewModel: BusinessViewModel) {
    val config by viewModel.config.collectAsState()
    val allSales by viewModel.allSales.collectAsState()
    val allPurchases by viewModel.allPurchases.collectAsState()
    
    val safeConfig = config ?: BusinessConfig()
    
    var packSize by remember(safeConfig) { mutableIntStateOf(safeConfig.bottlesPerPack) }
    var boxPriceText by remember(safeConfig) { mutableStateOf((safeConfig.boxPrice / 100.0).toString()) }
    var unitPriceText by remember(safeConfig) { mutableStateOf((safeConfig.unitPrice / 100.0).toString()) }
    var promoN by remember(safeConfig) { mutableIntStateOf(safeConfig.promoN) }
    var promoPriceText by remember(safeConfig) { mutableStateOf((safeConfig.promoPrice / 100.0).toString()) }
    
    var showConfirm by remember { mutableStateOf(false) }
    var helpKey by remember { mutableStateOf<String?>(null) }
    
    val hasMovements = allSales.isNotEmpty() || allPurchases.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                StepperField(
                    label = "Botellas por caja",
                    value = packSize,
                    onValueChange = { if (!hasMovements) packSize = it },
                    min = 1,
                    helpKey = "pack",
                    onHelpClick = { helpKey = it }
                )
                if (hasMovements) {
                    Text(
                        "No se puede cambiar con movimientos registrados",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Caja helada · S/", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = boxPriceText,
                    onValueChange = { boxPriceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Botella suelta · S/", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                )

                Spacer(modifier = Modifier.height(12.dp))
                StepperField(
                    label = "Botellas por promoción",
                    value = promoN,
                    onValueChange = { promoN = it },
                    min = 1,
                    helpKey = "promo",
                    onHelpClick = { helpKey = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Precio promoción · S/", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = promoPriceText,
                    onValueChange = { promoPriceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                )

                Spacer(modifier = Modifier.height(15.dp))
                PrimaryButton(
                    text = "Guardar configuración",
                    onClick = { showConfirm = true }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Los precios solo cambian para ventas futuras. La cantidad por caja se bloquea al registrar movimientos.",
            style = MaterialTheme.typography.labelSmall,
            color = Muted
        )
    }

    if (showConfirm) {
        val newBoxPrice = boxPriceText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
        val newUnitPrice = unitPriceText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
        val newPromoPrice = promoPriceText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
        
        ConfirmDialog(
            title = "¿Guardar configuración?",
            content = {
                Column {
                    SummaryRow("Botellas por caja", packSize.toString())
                    SummaryRow("Caja helada", formatMoney(newBoxPrice))
                    SummaryRow("Botella suelta", formatMoney(newUnitPrice))
                    SummaryRow("$promoN botellas en promoción", formatMoney(newPromoPrice))
                }
            },
            onConfirm = {
                viewModel.saveConfig(
                    BusinessConfig(
                        bottlesPerPack = packSize,
                        boxPrice = newBoxPrice,
                        unitPrice = newUnitPrice,
                        promoN = promoN,
                        promoPrice = newPromoPrice
                    )
                )
                showConfirm = false
            },
            onCancel = { showConfirm = false }
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
