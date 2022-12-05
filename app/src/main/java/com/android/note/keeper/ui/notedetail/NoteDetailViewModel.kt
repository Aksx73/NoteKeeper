package com.android.note.keeper.ui.notedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {



    fun onSaveClick() {
        //todo
    }

    fun isNoteValid() {
        //todo check for blank note
    }

    fun onPasswordClick(){

    }

    private fun createNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
        //todo navigate back to home with task result
    }

    private fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
        //todo navigate back to home with task result
    }

    private fun updatePasswordProtection(id: Int, isProtected: Boolean) = viewModelScope.launch {
        //repository.updatePassword(id,isProtected)
        //todo
    }

}