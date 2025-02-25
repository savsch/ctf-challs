package com.secure.notes.feature_note.domain.use_case

import com.google.gson.Gson
import com.secure.notes.feature_note.domain.model.Note
import com.secure.notes.feature_note.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first

class ExportNotes(
    private val repository: NoteRepository
) {

    suspend operator fun invoke(): String {
        val gson = Gson()
        val notes: List<Note> = repository.getNotes().first()
        return gson.toJson(notes)
    }
}