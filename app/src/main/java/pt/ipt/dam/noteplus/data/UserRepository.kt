package pt.ipt.dam.noteplus.data

import pt.ipt.dam.noteplus.model.User

/**
 * Repositório para gerir as operações dos utilizadores com o uso do SheetyService.
 *
 * @property sheetyService Serviço utilizado para interagir com a API do Sheety.
 */
class UserRepository(private val sheetyService: SheetyService) {

    /**
     * Obtém a lista de utilizadores.
     *
     * @return Resposta contendo a lista de utilizadores.
     */
    suspend fun getUsers(): SheetyService.UserResponse {
        return sheetyService.getUsers()
    }

    /**
     * Cria um novo utilizador no Sheety.
     *
     * @param user Utilizador a ser criado.
     */
    suspend fun createUser(user: User) {
        val userRequest = SheetyService.UserRequest(user)
        sheetyService.createUser(userRequest)
    }
}
