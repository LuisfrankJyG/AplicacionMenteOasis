package com.LuisS.menteoasis.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithChecklist(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val checklistItems: List<ChecklistItemEntity>
)
