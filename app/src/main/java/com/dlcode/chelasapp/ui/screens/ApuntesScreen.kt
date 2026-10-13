package com.dlcode.chelasapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlcode.chelasapp.ui.components.ConfirmDialog
import com.dlcode.chelasapp.ui.components.PrimaryButton
import com.dlcode.chelasapp.ui.theme.*
import com.dlcode.chelasapp.viewmodel.BusinessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApuntesScreen(viewModel: BusinessViewModel) {
    val allNotes by viewModel.allNotes.collectAsState()
    var category by remember { mutableStateOf("operacion") }
    var noteText by remember { mutableStateOf("") }
    var showConfirm by remember { mutableStateOf(false) }

    val categories = mapOf(
        "operacion" to "Operación",
        "compra" to "Compra pendiente",
        "cliente" to "Cliente",
        "recordatorio" to "Recordatorio"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBg)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            categories.forEach { (key, label) ->
                val colors = getNoteColors(key)
                Text(
                    text = label,
                    modifier = Modifier
                        .border(1.dp, colors.border, RoundedCornerShape(7.dp))
                        .background(colors.bg, RoundedCornerShape(7.dp))
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = colors.text)
                )
            }
        }

        if (allNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(25.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Sin apuntes todavía", fontWeight = FontWeight.Bold, color = Ink)
                    Text("Elige una categoría y guarda tu primer pendiente.", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
        } else {
            allNotes.forEach { note ->
                NoteCard(note)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Text("Nuevo apunte", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 22.dp, bottom = 11.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text("Categoría", style = MaterialTheme.typography.labelSmall)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = categories[category] ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { category = key; expanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("¿Qué necesitas recordar?", style = MaterialTheme.typography.labelSmall)
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Escribe aquí…") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = White, unfocusedContainerColor = White)
                )

                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Guardar apunte",
                    onClick = { if (noteText.isNotBlank()) showConfirm = true }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Los apuntes no cambian ventas ni caja. Los importes deben registrarse como movimientos para entrar en los cálculos.",
            style = MaterialTheme.typography.labelSmall,
            color = Muted
        )
    }

    if (showConfirm) {
        ConfirmDialog(
            title = "¿Guardar este apunte?",
            content = {
                NoteCard(com.dlcode.chelasapp.model.Apunte(date = "Hoy", category = category, text = noteText))
            },
            onConfirm = {
                viewModel.registerNote(category, noteText)
                noteText = ""
                showConfirm = false
            },
            onCancel = { showConfirm = false }
        )
    }
}

@Composable
fun NoteCard(note: com.dlcode.chelasapp.model.Apunte) {
    val colors = getNoteColors(note.category)
    val categories = mapOf(
        "operacion" to "Operación",
        "compra" to "Compra pendiente",
        "cliente" to "Cliente",
        "recordatorio" to "Recordatorio"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = (categories[note.category] ?: "OPERACIÓN").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp, color = colors.text)
                )
                Text(
                    text = note.date,
                    style = MaterialTheme.typography.labelSmall.copy(color = colors.text.copy(alpha = 0.8f))
                )
            }
            Text(
                text = note.text,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 1.7.sp, color = colors.text)
            )
        }
    }
}

data class NoteColors(val bg: Color, val border: Color, val text: Color)

@Composable
fun getNoteColors(category: String): NoteColors {
    return when (category) {
        "compra" -> NoteColors(CompraBg, CompraBorder, CompraText)
        "cliente" -> NoteColors(ClienteBg, ClienteBorder, ClienteText)
        "recordatorio" -> NoteColors(RecordatorioBg, RecordatorioBorder, RecordatorioText)
        else -> NoteColors(OperacionBg, OperacionBorder, OperacionText)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}
