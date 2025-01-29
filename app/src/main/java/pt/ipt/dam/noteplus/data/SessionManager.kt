package pt.ipt.dam.noteplus.data

import android.content.Context

/**
 * Gerenciador de sessão para armazenar e carregar dados do utilizador.
 */
object SessionManager {
    var userId: Int? = null
    var username: String? = null

    /**
     * Guarda a sessão do utilizador nas SharedPreferences.
     *
     * @param context Contexto da aplicação.
     */
    fun saveUserSession(context: Context) {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("USER_ID", userId ?: -1)
            putString("USERNAME", username)
            apply()
        }
    }

    /**
     * Carrega a sessão do utilizador das SharedPreferences.
     *
     * @param context Contexto da aplicação.
     */
    fun loadUserSession(context: Context) {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1).takeIf { it != -1 }
        username = sharedPref.getString("USERNAME", null)
    }
}
