package com.android.note.keeper.ui.deleted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.data.repository.DeletedNoteRepository
import com.android.note.keeper.ui.notelist.NoteListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletedNoteViewModel @Inject constructor(
    private val repository: DeletedNoteRepository
) : ViewModel() {

    val deletedNotes = repository.getDeletedNotes().asLiveData()

    fun onRestoreClicked(note: DeletedNote) {

    }

    fun onDeleteClick(note: DeletedNote) {
        deleteNote(note)
    }

    fun onDeleteAllClick() {
        deleteAllNotes()
    }

    private fun deleteNote(note: DeletedNote) = viewModelScope.launch {
        repository.delete(note)
       // _tasksEvent.emit(NoteListViewModel.TasksEvent.ShowUndoDeleteNoteMessage(note))
    }

    private fun deleteAllNotes() = viewModelScope.launch {
        repository.deleteAll()
    }





}