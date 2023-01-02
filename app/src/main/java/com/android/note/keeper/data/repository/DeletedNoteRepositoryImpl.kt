package com.android.note.keeper.data.repository

import com.android.note.keeper.data.local.DeletedNoteDao
import com.android.note.keeper.data.model.DeletedNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeletedNoteRepositoryImpl @Inject constructor(
    private val dao: DeletedNoteDao
) : DeletedNoteRepository {
    override fun getDeletedNotes(): Flow<List<DeletedNote>> {
        return dao.getAllDeletedNotes()
    }

    override suspend fun insert(note: DeletedNote) = dao.insert(note)

    override suspend fun deleteAll() = dao.deleteAllNotes()

    override suspend fun delete(note: DeletedNote) = dao.delete(note)
}