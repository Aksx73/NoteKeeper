package com.android.note.keeper.ui.deleted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.android.note.keeper.data.model.DeletedNote
import com.android.note.keeper.data.repository.DeletedNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class DeletedNoteViewModel @Inject constructor(
    private val repository: DeletedNoteRepository
) : ViewModel() {

    val deletedNotes = repository.getDeletedNotes().asLiveData()

    fun onRestoreClicked(note: DeletedNote) {

    }

}