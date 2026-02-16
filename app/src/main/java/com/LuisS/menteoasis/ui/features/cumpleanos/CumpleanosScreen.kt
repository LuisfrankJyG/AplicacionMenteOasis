package com.LuisS.menteoasis.ui.features.cumpleanos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LuisS.menteoasis.data.entities.BirthdayEntity
import com.LuisS.menteoasis.ui.AppViewModelProvider
import java.util.Calendar

@Composable
fun CumpleanosScreen(
    modifier: Modifier = Modifier,
    viewModel: CumpleanosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Birthday")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Próximos Cumpleaños",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.birthdays.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay cumpleaños registrados", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.birthdays) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            onDelete = { viewModel.deleteBirthday(birthday) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBirthdayDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, day, month, year ->
                viewModel.addBirthday(name, day, month, year)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BirthdayCard(
    birthday: BirthdayEntity,
    onDelete: () -> Unit
) {
    val today = Calendar.getInstance()
    val nextBirthday = Calendar.getInstance().apply {
        set(Calendar.MONTH, birthday.month - 1)
        set(Calendar.DAY_OF_MONTH, birthday.day)
        if (before(today)) {
            add(Calendar.YEAR, 1)
        }
    }
    val diff = nextBirthday.timeInMillis - today.timeInMillis
    val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cake,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = birthday.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${birthday.day}/${birthday.month}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (daysLeft == 0) {
                    Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
                } else {
                    Text(
                        "Faltan $daysLeft días",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AddBirthdayDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    // Year optional

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Cumpleaños") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = { if(it.length <= 2) day = it },
                        label = { Text("Día") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { if(it.length <= 2) month = it },
                        label = { Text("Mes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val d = day.toIntOrNull()
                    val m = month.toIntOrNull()
                    if (name.isNotBlank() && d != null && m != null && d in 1..31 && m in 1..12) {
                        onConfirm(name, d, m, null)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
