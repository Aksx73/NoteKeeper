package com.android.note.keeper.ui.notedetail

import androidx.lifecycle.ViewModel
import com.android.note.keeper.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {



}