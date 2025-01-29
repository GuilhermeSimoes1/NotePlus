package pt.ipt.dam.noteplus.model

/**
 * Representação de um utilizador.
 *
 * @property id Identificador único do utilizador.
 * @property username Nome de utilizador.
 * @property password Senha do utilizador.
 */
data class User(
    val id: Int? = null,
    val username: String,
    val password: String
)
