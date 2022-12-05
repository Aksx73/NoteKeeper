package com.android.note.keeper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.note.keeper.data.model.Note
import com.android.note.keeper.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Note::class], version = 1, exportSchema = true)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    /**
     * Just to pre populate list with some tasks
     * instead of showing empty list at start
     * **/
    class Callback @Inject constructor(
        private val database: Provider<NoteDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().noteDao()

            applicationScope.launch {
                dao.insert(
                    Note(
                        title = "Wash the dishes",
                        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam in scelerisque sem. Mauris\n" +
                                " volutpat, dolor id interdum ullamcorper, risus dolor egestas lectus, sit amet mattis purus"
                    )
                )
                dao.insert(
                    Note(
                        title = "Wash the dishes",
                        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam in scelerisque sem. Mauris\n" +
                                " volutpat, dolor id interdum ullamcorper, risus dolor egestas lectus, sit amet mattis purus",
                        isPasswordProtected = true
                    )
                )
                dao.insert(Note(title = "Wash the dishes", content = "note description"))
                dao.insert(
                    Note(
                        title = "Wash the dishes",
                        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam in scelerisque sem. Mauris\n" +
                                " volutpat, dolor id interdum ullamcorper, risus dolor egestas lectus, sit amet mattis purus"
                    )
                )
                dao.insert(
                    Note(
                        title = "Wash the dishes",
                        content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam in scelerisque sem. Mauris\n" +
                                " volutpat, dolor id interdum ullamcorper, risus dolor egestas lectus, sit amet mattis purus",
                        isPasswordProtected = true
                    )
                )
                dao.insert(Note(title = "Wash the dishes", content = ""))
                dao.insert(Note(title = "Wash the dishes", content = ""))

            }
        }
    }

}