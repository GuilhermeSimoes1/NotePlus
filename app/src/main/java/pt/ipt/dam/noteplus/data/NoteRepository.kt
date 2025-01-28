package pt.ipt.dam.noteplus.data

import pt.ipt.dam.noteplus.model.Note

class NoteRepository(private val sheetyService: SheetyService) {

    suspend fun getNotesForUser(userId: Int): List<Note> {
        return try {
            val response = sheetyService.getNotes()
            val notes = response.notes.filter { it.userId == userId }
            notes
        } catch (e: Exception) {
            // Em caso de erro, retornar uma lista vazia ou propagar a exceção
            emptyList()
        }
    }

    suspend fun createNoteInSheety(note: Note) {
        val noteRequest = SheetyService.NoteRequest(note)
        sheetyService.createNote(noteRequest)
    }

    suspend fun updateNoteInSheety(note: Note) {
        val noteRequest = SheetyService.NoteRequest(note)
        sheetyService.updateNote(note.id, noteRequest)
    }
}