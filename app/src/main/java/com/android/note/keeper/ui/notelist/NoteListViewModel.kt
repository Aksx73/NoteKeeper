package com.android.note.keeper.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: NoteRepository
) : ViewModel() {

    private val noteFlow = repository.getNotes()
    val notes = noteFlow.asLiveData()

    //here both are needed
    val masterPasswordFlow = preferenceManager.masterPasswordFlow
    val masterPasswordLiveData = preferenceManager.masterPasswordFlow.asLiveData()

    fun setMasterPassword(password: String) = viewModelScope.launch {
        preferenceManager.setMasterPassword(password)
    }

    fun onUpdateClick(note: Note) {
        updateNote(note)
    }

    fun onDeleteClick(note: Note) {
        deleteNote(note)
    }

    private fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
        //todo navigate back to home with task result
    }

    private fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
        //todo navigate back to home with task result
    }


}