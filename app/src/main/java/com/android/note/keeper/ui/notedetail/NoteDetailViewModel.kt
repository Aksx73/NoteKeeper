package com.android.note.keeper.ui.notedetail

import androidx.lifecycle.*
import com.android.note.keeper.data.PreferenceManager
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.data.repository.DeletedNoteRepository
import com.android.note.keeper.data.repository.NoteRepository
import com.android.note.keeper.ui.notelist.NoteListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val deletedNotesRepository: DeletedNoteRepository,
    private val repository: NoteRepository
) : ViewModel() {

    val selectedColor = MutableLiveData<Int>()
    var enableSaveOnDestroy = true

    private val _tasksEvent = MutableSharedFlow<TasksEvent>()
    val tasksEvent: SharedFlow<TasksEvent> = _tasksEvent

    private val mutableNote = MutableLiveData<Note?>()
    val currentNote: LiveData<Note?> get() = mutableNote

    private val mutableDeletedNote = MutableLiveData<DeletedNote?>()
    val currentDeletedNote: LiveData<DeletedNote?> get() = mutableDeletedNote

    private val mutableTempNote = MutableLiveData<Note>(Note(title = "", content = ""))
    val tempNote: LiveData<Note> get() = mutableTempNote

    private val mutableEditMode = MutableLiveData(true)
    val editMode: LiveData<Boolean> get() = mutableEditMode

    private val mutablePinValue = MutableLiveData(false)
    val pinValue: LiveData<Boolean> get() = mutablePinValue

    private val mutableArchiveValue = MutableLiveData(false)
    val archiveValue: LiveData<Boolean> get() = mutableArchiveValue

    //here both are needed
    val masterPasswordFlow = preferenceManager.masterPasswordFlow
    val masterPasswordLiveData = preferenceManager.masterPasswordFlow.asLiveData()

    /*fun getSelectedColor() :Int {
        if (selectedColor.value == null)
            selectedColor.value = 0
        return selectedColor.value!!
    }*/

    fun setSelectedColor(color: Int?) {
        if (color == null)
            selectedColor.value = 0
        else
            selectedColor.value = color!!
    }

    fun setEditMode(isEditable: Boolean) {
        mutableEditMode.value = isEditable
    }

    fun setPinValue(isPinned: Boolean) {
        mutablePinValue.value = isPinned
    }

    fun setArchiveValue(isArchived: Boolean) {
        mutableArchiveValue.value = isArchived
    }

    fun setTempNote(note: Note) {
        mutableTempNote.value = note
    }

    fun setCurrentNote(note: Note?) {
        mutableNote.value = note
    }

    fun setCurrentDeletedNote(deletedNote: DeletedNote?) {
        mutableDeletedNote.value = deletedNote
    }

    fun onSaveClick(note: Note) {
        createNote(note)
    }

    suspend fun onSaveClickAndReturnID(note: Note): Long {
        val id = viewModelScope.async { createNoteAndReturnID(note) }
        return id.await()
    }

    fun onUpdateClick(note: Note) {
        updateNote(note)
    }

    fun onDeleteClick(note: Note) {
        deleteNote(note)
        insertNoteInBin(note)
    }

    fun onDeleteForeverClicked(note: DeletedNote) {
        deleteNoteFromBinForever(note)
    }

    private fun createNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
        _tasksEvent.emit(TasksEvent.OnNoteUpdatedConfirmationMessage("Note added"))
    }

    suspend fun getNoteById(id: Long): Note {
        val deferred: Deferred<Note> = viewModelScope.async {
            repository.getNoteById(id)
        }
        return deferred.await()
    }

    private suspend fun createNoteAndReturnID(note: Note): Long {
        val id = repository.insertAndGetID(note)
        _tasksEvent.emit(TasksEvent.OnNoteUpdatedConfirmationMessage("Note added"))
        return id
    }

    private fun updateNote(note: Note) = viewModelScope.launch {
        //repository.update(note.copy(created = System.currentTimeMillis())) //created time updated to latest in case of updating note as well
        repository.update(note)
        _tasksEvent.emit(TasksEvent.OnNoteUpdatedConfirmationMessage("Note updated"))
    }

    private fun deleteNote(note: Note) = viewModelScope.launch {
        repository.delete(note)
        _tasksEvent.emit(TasksEvent.ShowUndoDeleteNoteMessage(note))
    }

    private fun insertNoteInBin(note: Note) = viewModelScope.launch {
        deletedNotesRepository.insert(
            DeletedNote(
                _id = note._id,
                title = note.title,
                content = note.content
            )
        )
    }

    private fun deleteNoteFromBinForever(note: DeletedNote) = viewModelScope.launch {
        deletedNotesRepository.delete(note)
    }

    fun setMasterPassword(password: String) = viewModelScope.launch {
        preferenceManager.setMasterPassword(password)
    }

    private fun updatePasswordProtection(id: Int, isProtected: Boolean) = viewModelScope.launch {
        //repository.updatePassword(id,isProtected)
        //todo
    }

    sealed class TasksEvent {
        data class ShowUndoDeleteNoteMessage(val note: Note) : TasksEvent()
        data class OnNewNoteSavedConfirmationMessage(val msg: String) : TasksEvent()
        data class OnNoteUpdatedConfirmationMessage(val msg: String) : TasksEvent()
    }


}