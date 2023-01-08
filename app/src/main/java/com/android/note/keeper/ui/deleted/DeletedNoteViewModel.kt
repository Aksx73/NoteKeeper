package com.android.note.keeper.ui.deleted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.android.note.keeper.data.repository.DeletedNoteRepository
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class DeletedNoteViewModel @Inject constructor(private val repository: DeletedNoteRepository) : ViewModel() {

    val deletedNotes = repository.getDeletedNotes().asLiveData()


}