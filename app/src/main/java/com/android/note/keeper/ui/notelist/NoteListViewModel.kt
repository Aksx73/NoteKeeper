package com.android.note.keeper.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.note.keeper.data.PreferenceManager
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

    val masterPasswordFlow = preferenceManager.masterPasswordFlow

    fun setMasterPassword(password: String) = viewModelScope.launch {
        preferenceManager.setMasterPassword(password)
    }

}