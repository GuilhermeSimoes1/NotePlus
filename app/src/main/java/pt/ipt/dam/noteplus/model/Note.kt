package pt.ipt.dam.noteplus.model

/**
 * Representação de uma nota.
 *
 * @property id Identificador único da nota.
 * @property userId Identificador do utilizador que criou a nota.
 * @property title Título da nota.
 * @property description Descrição da nota.
 * @property audioPath Caminho para o ficheiro de áudio associado à nota, se existir.
 * @property imagePath Caminho para o ficheiro de imagem associado à nota, se existir.
 */
data class Note(
    val id: Int? = null,
    val userId: Int? = null,
    val title: String,
    val description: String,
    val audioPath: String? = null,
    val imagePath: String? = null,
)

