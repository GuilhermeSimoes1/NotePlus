package pt.ipt.dam.noteplus.data

import pt.ipt.dam.noteplus.model.Note

/**
 * Repositório para gerir as operações de notas usando o SheetyService.
 *
 * @property sheetyService Serviço utilizado para interagir com a API do Sheety.
 */
class NoteRepository(private val sheetyService: SheetyService) {

    /**
     * Obtém as notas de um utilizador específico.
     *
     * @param userId ID do utilizador cujas notas devem ser obtidas.
     * @return Lista de notas do utilizador, ou uma lista vazia em caso de erro.
     */
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

    /**
     * Cria uma nova nota no Sheety.
     *
     * @param note Nota a ser criada.
     */
    suspend fun createNoteInSheety(note: Note) {
        val noteRequest = SheetyService.NoteRequest(note)
        sheetyService.createNote(noteRequest)
    }

    /**
     * Atualiza uma nota existente no Sheety.
     *
     * @param note Nota a ser atualizada.
     */
    suspend fun updateNoteInSheety(note: Note) {
        val noteRequest = SheetyService.NoteRequest(note)
        note.id?.let { sheetyService.updateNote(it, noteRequest) }
    }



    suspend fun deleteNoteInSheety(noteId: Int) {
        val response = sheetyService.deleteNote(noteId)

        if (!response.isSuccessful) {
            throw Exception("Erro ao apagar a nota. Código HTTP: ${response.code()}")
        }
    }
}
