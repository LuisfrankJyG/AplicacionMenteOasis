package com.LuisS.menteoasis.ui.features.checklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.LuisS.menteoasis.data.entities.NoteWithChecklist

@Composable
fun NoteCard(
    noteWithChecklist: NoteWithChecklist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(noteWithChecklist.note.color).copy(alpha = 1f) // Ensure full opacity
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (noteWithChecklist.note.title.isNotBlank()) {
                Text(
                    text = noteWithChecklist.note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (noteWithChecklist.note.content.isNotBlank()) {
                Text(
                    text = noteWithChecklist.note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Preview Checklist Items
            if (noteWithChecklist.checklistItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val uncheckedCount = noteWithChecklist.checklistItems.count { !it.isChecked }
                val totalCount = noteWithChecklist.checklistItems.size
                
                Text(
                    text = if (uncheckedCount == 0) "Completada ($totalCount items)" else "$uncheckedCount pendientes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
