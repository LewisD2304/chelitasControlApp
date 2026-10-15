package com.dlcode.chelasapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlcode.chelasapp.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
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

fun formatPrettyDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM. yyyy", Locale("es", "PE"))
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
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
                    color = Muted,
                    fontWeight = FontWeight.ExtraBold
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
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = DarkGreen)
                }
            }
        },
        actions = {
            IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = DarkGreen)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBg)
    )
}

@Composable
fun ChelasBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 16.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val items = listOf(
            Triple("inicio", "Inicio", Icons.Default.Home),
            Triple("vender", "Vender", Icons.Default.Add),
            Triple("stock", "Stock", Icons.AutoMirrored.Filled.List),
            Triple("reportes", "Reportes", Icons.Default.BarChart),
            Triple("caja", "Caja", Icons.Default.AccountBalanceWallet)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = icon, 
                        contentDescription = label,
                        modifier = Modifier.size(if (isSelected) 28.dp else 24.dp)
                    ) 
                },
                label = { 
                    Text(
                        text = label, 
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = if (isSelected) 12.sp else 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    ) 
                },
                selected = isSelected,
                onClick = { onNavigate(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = DarkGreen,
                    unselectedIconColor = Muted.copy(alpha = 0.5f),
                    unselectedTextColor = Muted.copy(alpha = 0.5f),
                    indicatorColor = DarkGreen // Color verde oscuro de la paleta solicitada
                )
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String, 
    value: String, 
    isBold: Boolean = false,
    helpKey: String? = null,
    onHelpClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(
                text = label, 
                style = MaterialTheme.typography.bodyMedium, 
                color = if (isBold) Ink else Muted, 
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium
            )
            if (helpKey != null) {
                HelpIconButton(onClick = { onHelpClick(helpKey) })
            }
        }
        Text(text = value, style = if(isBold) MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp) else MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Ink)
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    helpKey: String? = null,
    onHelpClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Line),
        shape = RoundedCornerShape(17.dp)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label, 
                    style = MaterialTheme.typography.labelMedium, 
                    color = Muted, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (helpKey != null) {
                    HelpIconButton(onClick = { onHelpClick(helpKey) })
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
                color = Ink,
                modifier = Modifier.padding(top = 0.dp),
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun HelpIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Muted
) {
    Box(
        modifier = modifier
            .size(48.dp) // Área táctil mínima recomendada
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Ayuda",
            modifier = Modifier.size(22.dp),
            tint = tint
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = DarkGreen,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = contentColor),
        enabled = enabled,
        contentPadding = PaddingValues(15.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
fun SecondaryButton(
    text: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier,
    color: Color = DarkGreen
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(49.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = BorderStroke(1.dp, Line)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StepperField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    helpKey: String? = null,
    onHelpClick: (String) -> Unit = {}
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (helpKey != null) {
                HelpIconButton(onClick = { onHelpClick(helpKey) })
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp)
                .border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp))
                .background(Color.White, RoundedCornerShape(11.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                modifier = Modifier.width(44.dp)
            ) {
                Text("−", fontSize = 24.sp, color = DarkGreen)
            }
            TextField(
                value = value.toString(),
                onValueChange = { 
                    val n = it.toIntOrNull() ?: 0
                    if (n >= min) onValueChange(n)
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Black, fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.width(44.dp)
            ) {
                Text("+", fontSize = 24.sp, color = DarkGreen)
            }
        }
    }
}

@Composable
fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00"
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(11.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun LabelWithHelp(label: String, helpKey: String, onHelpClick: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = Ink, modifier = Modifier.weight(1f))
        HelpIconButton(onClick = { onHelpClick(helpKey) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodDropdown(
    selectedMethod: String,
    onMethodChange: (String) -> Unit
) {
    val methods = listOf("Efectivo", "Yape", "Plin", "Tarjeta", "Mixto", "Fiado")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedMethod,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFD0DBC6), RoundedCornerShape(11.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(11.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            methods.forEach { m ->
                DropdownMenuItem(
                    text = { Text(m) },
                    onClick = { onMethodChange(m); expanded = false }
                )
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(27.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = Ink, fontWeight = FontWeight.Bold)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted, textAlign = TextAlign.Center, lineHeight = 24.sp)
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
        text = { Text(text = description, style = MaterialTheme.typography.bodyLarge, lineHeight = 28.sp) },
        confirmButton = {
            PrimaryButton(text = "Entendido", onClick = onDismiss)
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(23.dp)
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
            PrimaryButton(text = "Confirmar", onClick = onConfirm)
        },
        dismissButton = {
            SecondaryButton(text = "Cancelar", onClick = onCancel, modifier = Modifier.fillMaxWidth())
        },
        containerColor = LightBg,
        shape = RoundedCornerShape(23.dp)
    )
}
