package pt.ipt.dam.noteplus.data

import pt.ipt.dam.noteplus.model.Note
import pt.ipt.dam.noteplus.model.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SheetyService {


    //::::::::::::EndPoints das notas::::::::::://

    // Classe de resposta para GET de notas
    data class NotesResponse(
        val notes: List<Note>
    )

    // Classe de requisição para POST e PUT de notas
    data class NoteRequest(
        val note: Note
    )

    @GET("notes")
    suspend fun getNotes(): NotesResponse

    @POST("notes")
    suspend fun createNote(@Body noteRequest: NoteRequest): Note

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: Int, @Body noteRequest: NoteRequest): Note

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int)


    //::::::::::::EndPoints dos utilizadores::::::::::://

    // Classe de resposta para GET de utilizadores
    data class UserResponse(
        val users: List<User>
    )

    // Classe de requisição para POST de utilizadores
    data class UserRequest(
        val user: User
    )

    @GET("users/")
    suspend fun getUsers(): UserResponse

    @POST("users/")
    suspend fun createUser(@Body userRequest: UserRequest): User
}

object SheetyApi {
    private const val BASE_URL = "https://api.sheety.co/56ba2a79dee0c3ec8ff723f94e7d5f51/notePlus/"

    val service: SheetyService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SheetyService::class.java)
    }


}



