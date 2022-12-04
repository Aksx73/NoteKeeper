package com.android.note.keeper.data.repository

import com.android.note.keeper.data.local.NoteDao
import com.android.note.keeper.data.model.Note
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 *  Implementation of [NoteRepository]. Single entry point for managing tasks data.
 */
class NoteRepositoryImpl @Inject constructor(private val dao: NoteDao) : NoteRepository {
    override fun getNotes(): Flow<List<Note>> {
        return dao.getAllNotes()
    }

    override suspend fun insert(note: Note) = dao.insert(note)

    override suspend fun update(note: Note) = dao.update(note)

    override suspend fun delete(note: Note) = dao.delete(note)

    override suspend fun updatePassword(id: Int, password: String) =
        dao.updatePassword(id, password)

}