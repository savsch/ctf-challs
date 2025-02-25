package com.secure.notes.feature_note.presentation.notes

import android.content.ContentResolver
import android.content.Context
import com.secure.notes.feature_note.domain.model.Note
import com.secure.notes.feature_note.domain.util.NoteOrder

sealed class NotesEvent {
    data class Order(val noteOrder: NoteOrder): NotesEvent()
    data class DeleteNote(val note: Note): NotesEvent()
    object RestoreNote: NotesEvent()
    object ToggleOrderSection: NotesEvent()
    data class RestoreNotes(val fileBytes: ByteArray, val ctx: Context): NotesEvent()
    data class BackupNotes(val ctx: Context): NotesEvent()
}
