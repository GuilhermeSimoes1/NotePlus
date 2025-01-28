package pt.ipt.dam.noteplus.model

data class Note(
    val id: Int? = null,
    val userId: Int? = null,
    val title: String,
    val description: String,
    val audioPath: String? = null,
    val imagePath: String? = null,
)
