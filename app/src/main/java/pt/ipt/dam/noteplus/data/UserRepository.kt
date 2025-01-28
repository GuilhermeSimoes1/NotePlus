package pt.ipt.dam.noteplus.data

import pt.ipt.dam.noteplus.model.User

class UserRepository(private val sheetyService: SheetyService) {

    suspend fun getUsers(): SheetyService.UserResponse {
        return sheetyService.getUsers()
    }


    suspend fun createUser(user: User) {
        val userRequest = SheetyService.UserRequest(user)
        sheetyService.createUser(userRequest)
    }
}