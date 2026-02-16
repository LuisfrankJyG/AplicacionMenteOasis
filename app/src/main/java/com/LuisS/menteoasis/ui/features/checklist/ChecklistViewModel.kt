package com.LuisS.menteoasis.ui.features.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LuisS.menteoasis.data.MenteOasisRepository
import com.LuisS.menteoasis.data.entities.ChecklistItemEntity
import com.LuisS.menteoasis.data.entities.NoteEntity
import com.LuisS.menteoasis.data.entities.NoteWithChecklist
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChecklistUiState(
    val notes: List<NoteWithChecklist> = emptyList()
)

class ChecklistViewModel(
    private val repository: MenteOasisRepository
) : ViewModel() {

    val uiState: StateFlow<ChecklistUiState> = repository.allNotes
        .map { ChecklistUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChecklistUiState()
        )

    fun addNote(title: String, content: String, color: Int) {
        viewModelScope.launch {
            repository.insertNote(
                NoteEntity(
                    title = title,
                    content = content,
                    color = color
                )
            )
        }
    }

    fun addNoteWithItems(title: String, content: String, color: Int, items: List<ChecklistItemEntity>) {
        viewModelScope.launch {
            val noteId = repository.insertNote(
                NoteEntity(
                    title = title,
                    content = content,
                    color = color
                )
            )
            items.forEach { item ->
                repository.insertChecklistItem(item.copy(noteId = noteId.toInt()))
            }
        }
    }
    
    // Simplified: get from current list instead of DB query for now
    // In a real app with large data, use repository.getNote(id)
    suspend fun getNote(id: Int): NoteWithChecklist? {
        return uiState.value.notes.find { it.note.id == id }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    fun updateChecklistItems(items: List<ChecklistItemEntity>, noteId: Int) {
        viewModelScope.launch {
            // Simple strategy: delete all for this note and re-insert
            // A better way is DiffUtil-like logic but for now this ensures consistency
            repository.deleteChecklistItemsForNote(noteId)
            items.forEach { item ->
                repository.insertChecklistItem(item.copy(noteId = noteId))
            }
        }
    }
}