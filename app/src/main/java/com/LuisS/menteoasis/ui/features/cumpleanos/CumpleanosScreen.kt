package com.LuisS.menteoasis.ui.features.cumpleanos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LuisS.menteoasis.data.entities.BirthdayEntity
import com.LuisS.menteoasis.ui.AppViewModelProvider
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CumpleanosScreen(
    modifier: Modifier = Modifier,
    viewModel: CumpleanosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var nameForNewBirthday by remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) }
    var dayForNewBirthday by remember { mutableStateOf("") }
    var monthForNewBirthday by remember { mutableStateOf("") }
    var showDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNameDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Cake, contentDescription = "Añadir Cumpleaños")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Celebraciones", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    ) 
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp) // Responsive width
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Próximos Cumpleaños",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.birthdays.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Text("No hay cumpleaños registrados", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.birthdays) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            daysUntil = viewModel.getDaysUntilBirthday(birthday),
                            onDelete = { viewModel.deleteBirthday(birthday) }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Nombre del cumpleañero") },
            text = {
                OutlinedTextField(
                    value = nameForNewBirthday,
                    onValueChange = { nameForNewBirthday = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameForNewBirthday.isNotBlank()) {
                        showNameDialog = false
                        showDateDialog = true
                    }
                }) {
                    Text("Siguiente")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDateDialog) {
        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = { Text("Fecha de Cumpleaños") },
            text = {
                Column {
                    Text("Ingresa el día y el mes (DD/MM)", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = dayForNewBirthday,
                            onValueChange = { if (it.length <= 2) dayForNewBirthday = it.filter { c -> c.isDigit() } },
                            label = { Text("Día") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = monthForNewBirthday,
                            onValueChange = { if (it.length <= 2) monthForNewBirthday = it.filter { c -> c.isDigit() } },
                            label = { Text("Mes") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val d = dayForNewBirthday.toIntOrNull()
                    val m = monthForNewBirthday.toIntOrNull()
                    if (d != null && m != null && d in 1..31 && m in 1..12) {
                        viewModel.addBirthday(
                            name = nameForNewBirthday,
                            day = d,
                            month = m,
                            year = Calendar.getInstance().get(Calendar.YEAR) // Default to current year
                        )
                        nameForNewBirthday = ""
                        dayForNewBirthday = ""
                        monthForNewBirthday = ""
                        showDateDialog = false
                    }
                }) {
                    Text("Guardar (DD/MM)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("Atrás")
                }
            }
        )
    }
    }
}

@Composable
fun BirthdayCard(
    birthday: BirthdayEntity,
    daysUntil: Int,
    onDelete: () -> Unit
) {
    val isToday = daysUntil == 0

    // Dynamic Gradient based on proximity
    val gradient = if (isToday) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF5252), Color(0xFFFF4081)),
            tileMode = TileMode.Clamp
        )
    } else {
        Brush.linearGradient(
            colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer),
            tileMode = TileMode.Clamp
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isToday) 6.dp else 1.dp)
    ) {
        Box(modifier = Modifier.background(gradient)) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar/Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isToday) Icons.Default.Celebration else Icons.Default.Cake,
                        contentDescription = null,
                        tint = if (isToday) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = birthday.name, 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format("%02d/%02d", birthday.day, birthday.month),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isToday) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Days Left Badge
                Column(horizontalAlignment = Alignment.End) {
                    if (isToday) {
                        Surface(
                            color = Color(0xFFFFEB3B), // Success Yellow
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "¡HOY!", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = Color.Black
                            )
                        }
                    } else {
                        Text(
                            "Faltan", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$daysUntil días", 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.DeleteOutline, 
                            contentDescription = "Eliminar",
                            tint = if (isToday) Color.White else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}



