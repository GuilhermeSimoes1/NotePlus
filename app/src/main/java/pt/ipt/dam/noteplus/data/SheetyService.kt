package pt.ipt.dam.noteplus.data

import pt.ipt.dam.noteplus.model.Note
import pt.ipt.dam.noteplus.model.User
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Serviço Sheety para interagir com a API de notas e utilizadores.
 */
interface SheetyService {

    //::::::::::::EndPoints das notas:::::::::::://

    /**
     * Classe de resposta para GET de notas.
     *
     * @property notes Lista de notas.
     */
    data class NotesResponse(
        val notes: List<Note>
    )

    /**
     * Classe de requisição para POST e PUT de notas.
     *
     * @property note Nota a ser enviada na requisição.
     */
    data class NoteRequest(
        val note: Note
    )

    /**
     * Obtém todas as notas.
     *
     * @return Resposta contendo a lista de notas.
     */
    @GET("notes")
    suspend fun getNotes(): NotesResponse

    /**
     * Cria uma nova nota.
     *
     * @param noteRequest Requisição que contém a nota a ser criada.
     * @return Nota criada.
     */
    @POST("notes")
    suspend fun createNote(@Body noteRequest: NoteRequest): Note

    /**
     * Atualiza uma nota existente.
     *
     * @param id ID da nota a ser atualizada.
     * @param noteRequest Requisição que contém a nota a ser atualizada.
     * @return Nota atualizada.
     */
    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: Int, @Body noteRequest: NoteRequest): Note

    /**
     * Apaga uma nota existente.
     *
     * @param id ID da nota a ser apagada.
     */
    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): retrofit2.Response<Unit>

    //::::::::::::EndPoints dos utilizadores:::::::::::://

    /**
     * Classe de resposta para GET de utilizadores.
     *
     * @property users Lista de utilizadores.
     */
    data class UserResponse(
        val users: List<User>
    )

    /**
     * Classe de requisição para POST de utilizadores.
     *
     * @property user Utilizador a ser enviado na requisição.
     */
    data class UserRequest(
        val user: User
    )

    /**
     * Obtém todos os utilizadores.
     *
     * @return Resposta que contém a lista de utilizadores.
     */
    @GET("users/")
    suspend fun getUsers(): UserResponse

    /**
     * Cria um novo utilizador.
     *
     * @param userRequest Requisição que contém o utilizador a ser criado.
     * @return Utilizador criado.
     */
    @POST("users/")
    suspend fun createUser(@Body userRequest: UserRequest): User
}

/**
 * Objeto para configurar e fornecer o serviço Sheety.
 */
object SheetyApi {
    private const val BASE_URL = "https://api.sheety.co/13402998b9223ddac6b380b566ef9312/notePlus/"

    /**
     * Instância do serviço Sheety.
     */
    val service: SheetyService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SheetyService::class.java)
    }
}
