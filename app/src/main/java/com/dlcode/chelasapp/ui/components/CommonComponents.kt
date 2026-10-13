package com.dlcode.chelasapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlcode.chelasapp.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun formatMoney(amountCents: Long): String {
    val amount = amountCents / 100.0
    val symbols = DecimalFormatSymbols(Locale("es", "PE"))
    symbols.decimalSeparator = ','
    symbols.groupingSeparator = '.'
    val df = DecimalFormat("#,##0.00", symbols)
    return "S/ ${df.format(amount)}"
}

fun formatStock(totalBottles: Int, packSize: Int): String {
    return if (packSize > 0) {
        val boxes = totalBottles / packSize
        val units = totalBottles % packSize
        "$boxes cajas + $units botellas"
    } else {
        "$totalBottles botellas"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChelasTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "SAN JUAN / UNA PERSONA",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = DarkGreen)
                }
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = DarkGreen)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBg)
    )
}

@Composable
fun ChelasBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("inicio", "Inicio", Icons.Default.Home),
            Triple("vender", "Vender", Icons.Default.Add),
            Triple("stock", "Stock", Icons.AutoMirrored.Filled.List),
            Triple("reportes", "Reportes", Icons.Default.BarChart),
            Triple("caja", "Caja", Icons.Default.AccountBalanceWallet)
        )

        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkGreen,
                    selectedTextColor = DarkGreen,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = if (isBold) Ink else Muted)
        Text(text = value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, color = Ink)
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    helpKey: String? = null,
    onHelpClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Muted)
                if (helpKey != null) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Ayuda",
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onHelpClick(helpKey) },
                        tint = Muted
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                color = Ink,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DarkGreen,
    contentColor: Color = White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = contentColor),
        enabled = enabled
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(11.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen),
        border = BorderStroke(1.dp, Line)
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StepperField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    helpKey: String? = null,
    onHelpClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Ink)
            if (helpKey != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Ayuda",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onHelpClick(helpKey) },
                    tint = Muted
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .border(1.dp, Line, RoundedCornerShape(10.dp))
                .background(White, RoundedCornerShape(10.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", tint = DarkGreen)
            }
            Text(
                text = value.toString(),
                modifier = Modifier.weight(2f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Más", tint = DarkGreen)
            }
        }
    }
}

@Composable
fun HelpDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(text = description, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", color = DarkGreen, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(22.dp)
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    content: @Composable () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = content,
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, Line),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = DarkGreen)
            }
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(22.dp)
    )
}
