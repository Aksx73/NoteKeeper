package com.android.note.keeper.ui.notedetail

import androidx.lifecycle.*
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val repository: NoteRepository
) : ViewModel() {

    val selectedColor = MutableLiveData<Int>()

    private val mutableNote = MutableLiveData<Note?>()
    val currentNote: LiveData<Note?> get() = mutableNote

    private val mutableEditMode = MutableLiveData(true)
    val editMode: LiveData<Boolean> get() = mutableEditMode

    val masterPasswordFlow = preferenceManager.masterPasswordFlow

    fun getSelectedColor() :Int {
        if (selectedColor.value == null)
            selectedColor.value = 0
        return selectedColor.value!!
    }

    fun setSelectedColor(color: Int?){
        if (color == null)
            selectedColor.value = 0
        else
            selectedColor.value = color!!
    }

    fun setEditMode(isEditable: Boolean) {
        mutableEditMode.value = isEditable
    }

    fun setCurrentNote(note: Note?) {
        mutableNote.value = note
    }

    fun onSaveClick(note: Note) {
        createNote(note)
    }

    fun onUpdateClick(note: Note) {
        updateNote(note)
    }

    fun onDeleteClick(note: Note) {
        deleteNote(note)
    }

    fun isNoteValid() {
        //todo check for blank note
    }

    fun onPasswordClick() {

    }

    private fun createNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
        //todo navigate back to home with task result
    }

    private fun updateNote(note: Note) = viewModelScope.launch {
        repository.update(note)
        //todo navigate back to home with task result
    }

    private fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
        //todo navigate back to home with task result
    }


    fun setMasterPassword(password: String) = viewModelScope.launch {
        preferenceManager.setMasterPassword(password)
    }

    private fun updatePasswordProtection(id: Int, isProtected: Boolean) = viewModelScope.launch {
        //repository.updatePassword(id,isProtected)
        //todo
    }

}