package pt.ipt.dam.noteplus.data

import android.content.Context

object SessionManager {
    var userId: Int? = null
    var username: String? = null

    fun saveUserSession(context: Context) {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("USER_ID", userId ?: -1)
            putString("USERNAME", username)
            apply()
        }
    }

    fun loadUserSession(context: Context) {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1).takeIf { it != -1 }
        username = sharedPref.getString("USERNAME", null)
    }

    fun clearUserSession(context: Context) {
        userId = null
        username = null
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}