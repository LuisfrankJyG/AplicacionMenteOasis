package com.LuisS.menteoasis.ui.features.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.LuisS.menteoasis.data.entities.ChecklistItemEntity
import com.LuisS.menteoasis.data.entities.NoteEntity
import com.LuisS.menteoasis.ui.AppViewModelProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int, // -1 for new note
    onNavigateBack: () -> Unit,
    viewModel: ChecklistViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var noteColor by remember { mutableIntStateOf(Color.White.toArgb()) } // Default white (or transparent-ish)
    var isPinned by remember { mutableStateOf(false) }
    var isArchived by remember { mutableStateOf(false) }
    
    // Checklist State
    var checklistItems by remember { mutableStateOf(listOf<ChecklistItemEntity>()) }
    var showChecklistInput by remember { mutableStateOf(false) }
    var newChecklistItemText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    // Colors Palette (Pastel)
    val colors = listOf(
        Color(0xFFFFFFFF), // White
        Color(0xFFF28B82), // Red
        Color(0xFFFBBC04), // Orange
        Color(0xFFFFF475), // Yellow
        Color(0xFFCCFF90), // Green
        Color(0xFFA7FFEB), // Teal
        Color(0xFFCBF0F8), // Blue
        Color(0xFFAECBFA), // Dark Blue
        Color(0xFFD7AEFB), // Purple
        Color(0xFFFDCFE8), // Pink
        Color(0xFFE6C9A8), // Brown
        Color(0xFFE8EAED)  // Gray
    )

    // Load Note
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            val noteWithChecklist = viewModel.getNote(noteId)
            noteWithChecklist?.let {
                title = it.note.title
                content = it.note.content
                noteColor = it.note.color
                isPinned = it.note.isPinned
                isArchived = it.note.isArchived
                checklistItems = it.checklistItems
            }
        }
    }

    val saveNote = {
        if (title.isNotBlank() || content.isNotBlank() || checklistItems.isNotEmpty()) {
            if (noteId == -1) {
                // Insert New
                // Note: using the new method to save items with the note ID
                viewModel.addNoteWithItems(title, content, noteColor, checklistItems)
            } else {
                // Update Existing
                viewModel.updateNote(
                    NoteEntity(
                        id = noteId,
                        title = title,
                        content = content,
                        color = noteColor,
                        isPinned = isPinned,
                        isArchived = isArchived
                    )
                )
                viewModel.updateChecklistItems(checklistItems, noteId)
            }
        }
    }

    Scaffold(
        containerColor = Color(noteColor),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        saveNote()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin"
                        )
                    }
                    IconButton(onClick = { isArchived = !isArchived }) {
                        Icon(Icons.Default.Inventory, contentDescription = "Archive")
                    }
                    if (noteId != -1) {
                        IconButton(onClick = {
                            // Delete logic
                             scope.launch {
                                viewModel.getNote(noteId)?.let { viewModel.deleteNote(it.note) }
                                onNavigateBack()
                             }
                        }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                actions = {
                    // Color Picker
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Simple embedded color picker for brevity
                       LazyColumn(modifier = Modifier.height(40.dp).width(200.dp)) {
                           // Actually LazyColumn isn't good horizontal. Use Row inside HorizontalScroll or just show first 5
                           // Let's just put a "Color" button that opens a dialog or cycles?
                           // Cycling for simplicity
                       }
                       IconButton(onClick = {
                           val currentIndex = colors.indexOfFirst { it.toArgb() == noteColor }
                           val nextIndex = (currentIndex + 1) % colors.size
                           noteColor = colors[nextIndex].toArgb()
                       }) {
                           Icon(
                               modifier = Modifier
                                   .size(24.dp)
                                   .clip(CircleShape)
                                   .background(Color(noteColor))
                                   .border(1.dp, Color.Black, CircleShape),
                               imageVector = Icons.Default.Add, // Placeholder icon, actually just circle
                               tint = Color.Transparent,
                               contentDescription = "Change Color"
                           )
                       }
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showChecklistInput = true }) {
                        Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add Checklist")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Título", style = MaterialTheme.typography.headlineMedium) },
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Nota...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Checklist Items List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(200.dp) // Limit height or make it scrollable part of body
            ) {
                items(checklistItems) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { isChecked ->
                                checklistItems = checklistItems.map {
                                    if (it == item) it.copy(isChecked = isChecked) else it
                                }
                            },
                             colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                        Text(
                            text = item.text,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = {
                            checklistItems = checklistItems - item
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                }
            }

            if (showChecklistInput) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newChecklistItemText,
                        onValueChange = { newChecklistItemText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nuevo elemento") }
                    )
                    IconButton(onClick = {
                        if (newChecklistItemText.isNotBlank()) {
                            val newItem = ChecklistItemEntity(
                                noteId = if(noteId == -1) 0 else noteId, // Temp ID
                                text = newChecklistItemText
                            )
                            checklistItems = checklistItems + newItem
                            newChecklistItemText = ""
                            // Keep input open
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                    IconButton(onClick = { showChecklistInput = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                }
            }
        }
    }
}
