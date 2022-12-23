@file:OptIn(ExperimentalCoroutinesApi::class)

package com.android.note.keeper.ui.notelist

import androidx.lifecycle.*
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.data.repository.NoteRepository
import com.android.note.keeper.ui.notedetail.NoteDetailViewModel
import com.android.note.keeper.util.Constants.NOTE_ADDED_RESULT_OK
import com.android.note.keeper.util.Constants.NOTE_DELETE_RESULT_OK
import com.android.note.keeper.util.Constants.NOTE_UPDATED_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: NoteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val searchQuery = savedStateHandle.getLiveData("searchQuery", "")

    private val _tasksEvent = MutableSharedFlow<TasksEvent>()
    val tasksEvent: SharedFlow<TasksEvent> = _tasksEvent

    private val noteFlow = searchQuery.asFlow().flatMapLatest { query ->
        repository.getNotes(query)
    }

    private val archiveNoteFlow = searchQuery.asFlow().flatMapLatest { query ->
        repository.getArchiveNotes(query)
    }

    //private val noteFlow = repository.getNotes()
    val notes = noteFlow.asLiveData()
    val archiveNotes = archiveNoteFlow.asLiveData()

    //here both are needed
    val masterPasswordFlow = preferenceManager.masterPasswordFlow
    val masterPasswordLiveData = preferenceManager.masterPasswordFlow.asLiveData()

    var isMultiColumnView: Boolean = false

    val viewModeFlow = preferenceManager.viewModeFlow
    val viewModeLiveData = preferenceManager.viewModeFlow.asLiveData()

    fun setMasterPassword(password: String) = viewModelScope.launch {
        preferenceManager.setMasterPassword(password)
    }

    fun onUpdateClick(note: Note) {
        updateNote(note)
    }

    fun onDeleteClick(note: Note) {
        deleteNote(note)
    }

    fun onUndoDeleteClick(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun onUndoArchiveClick(note: Note) = viewModelScope.launch {
        repository.update(note.copy(archived = false))
    }

    fun onViewModeChanged(mode: Int) = viewModelScope.launch {
        preferenceManager.setViewMode(mode)
    }

    private fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    private fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
        _tasksEvent.emit(TasksEvent.ShowUndoDeleteNoteMessage(note))
    }

    sealed class TasksEvent {
        data class ShowUndoDeleteNoteMessage(val note: Note) : TasksEvent()
    }

}