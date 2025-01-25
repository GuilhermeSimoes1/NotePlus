package pt.ipt.dam.noteplus.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pt.ipt.dam.noteplus.model.Note

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>
}