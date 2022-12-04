package com.android.note.keeper.ui.notelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.android.note.keeper.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val noteFlow = repository.getNotes()
    val notes = noteFlow.asLiveData()

}