package com.secure.notes.feature_note.presentation.notes

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atwa.filepicker.core.FilePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.secure.notes.feature_note.domain.model.Note
import com.secure.notes.feature_note.domain.use_case.NoteUseCases
import com.secure.notes.feature_note.domain.util.Encryption
import com.secure.notes.feature_note.domain.util.NoteOrder
import com.secure.notes.feature_note.domain.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _state = mutableStateOf(NotesState())
    val state: State<NotesState> = _state

    private var recentlyDeletedNote: Note? = null

    private var getNotesJob: Job? = null

    init {
        getNotes(NoteOrder.Date(OrderType.Descending))
    }

    @SuppressLint("HardwareIds")
    fun onEvent(event: NotesEvent) {
        when (event) {
            is NotesEvent.Order -> {
                if (state.value.noteOrder::class == event.noteOrder::class &&
                    state.value.noteOrder.orderType == event.noteOrder.orderType
                ) {
                    return
                }
                getNotes(event.noteOrder)
            }
            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(event.note)
                    recentlyDeletedNote = event.note
                }
            }
            is NotesEvent.RestoreNote -> {
                viewModelScope.launch {
                    noteUseCases.addNote(recentlyDeletedNote ?: return@launch)
                    recentlyDeletedNote = null
                }
            }
            is NotesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
            is NotesEvent.BackupNotes -> {
                viewModelScope.launch {
                    val notes = noteUseCases.exportNotes()
                    val id = Settings.Secure.getString(
                        event.ctx.contentResolver, Settings.Secure.ANDROID_ID
                    ).takeLast(6).toInt(radix = 16)
                    val shift = 6969696969696969696L
                    val scale = 6969696969696969696L
                    val extendedKey = id.toBigInteger().add(shift.toBigInteger()).multiply(scale.toBigInteger()).toByteArray()


                    val backupData = Encryption.aesEncrypt(notes.toByteArray(), extendedKey.sliceArray(extendedKey.size-16 until extendedKey.size))

                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "secure-notes-backup.bkp")
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")

                    // Insert the new file into the MediaStore
                    val resolver = event.ctx.contentResolver
                    val uri = resolver . insert (MediaStore.Files.getContentUri("external"), values)

                    uri?.let {
                        resolver.openOutputStream(uri)?.use {
                            it.write(backupData)
                            Toast.makeText(event.ctx,"Saved to Internal-Storage/Download/secure-notes-backup.bkp",Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            is NotesEvent.RestoreNotes -> {
                val id = Settings.Secure.getString(
                    event.ctx.contentResolver, Settings.Secure.ANDROID_ID
                ).takeLast(6).toInt(radix = 16)
                val shift = 6969696969696969696L
                val scale = 6969696969696969696L
                val extendedKey = id.toBigInteger().add(shift.toBigInteger()).multiply(scale.toBigInteger()).toByteArray()

                try {
                    val restoredData = Encryption.aesDecrypt(event.fileBytes, extendedKey.sliceArray(extendedKey.size-16 until extendedKey.size))
                    val listType = object : TypeToken<ArrayList<Note>>(){}.type
                    val notes: List<Note> = Gson().fromJson(restoredData.decodeToString(), listType)
                    viewModelScope.launch {
                        noteUseCases.replaceAll(notes)
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(event.ctx,"Restored Notes Successfully",Toast.LENGTH_LONG).show()
                        }
                    }
                }catch (e: Exception){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(event.ctx,"Error: Invalid or corrupt backup",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun Context.getActivity(): AppCompatActivity? = when (this) {
        is AppCompatActivity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }

    private fun getNotes(noteOrder: NoteOrder) {
        getNotesJob?.cancel()
        getNotesJob = noteUseCases.getNotes(noteOrder)
            .onEach { notes ->
                _state.value = state.value.copy(
                    notes = notes,
                    noteOrder = noteOrder
                )
            }
            .launchIn(viewModelScope)
    }
}

fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}