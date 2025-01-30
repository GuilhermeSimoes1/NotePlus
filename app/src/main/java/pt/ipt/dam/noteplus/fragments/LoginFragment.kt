package pt.ipt.dam.noteplus.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.SessionManager
import pt.ipt.dam.noteplus.data.SheetyApi
import pt.ipt.dam.noteplus.data.UserRepository
import java.security.MessageDigest

/**
 * Fragmento para a tela de login.
 */
class LoginFragment : Fragment(R.layout.login) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = view.findViewById<TextView>(R.id.loginNome).text.toString()
            val password = view.findViewById<TextView>(R.id.loginPassword).text.toString()
            loginUser(username, password)
        }

        val registerLink = view.findViewById<TextView>(R.id.registerLink)
        registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        val infoLink = view.findViewById<TextView>(R.id.infoLink)
        infoLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_InfoFragment)
        }
    }

    /**
     * Gera um hash SHA-256 a partir da senha fornecida.
     *
     * Esta função utiliza o algoritmo SHA-256 para gerar um hash a partir da senha fornecida.
     * O hash gerado é retornado como uma string hexadecimal.
     *
     * @param password A senha a ser convertida em hash.
     * @return A senha convertida num hash SHA-256 representado como uma string hexadecimal.
     */

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Realiza o login do utilizador com base no nome de utilizador e senha fornecidos.
     * Armazena as informações do utilizador no SessionManager e navega para o HomeFragment em caso de sucesso.
     *
     * @param username Nome de utilizador fornecido.
     * @param password Senha fornecida.
     */
    private fun loginUser(username: String, password: String) {
        val repository = UserRepository(SheetyApi.service)
        lifecycleScope.launch {
            try {
                val response = repository.getUsers()
                val hashedPassword = hashPassword(password)
                val user = response.users.find { it.username == username && it.password == hashedPassword }
                if (user != null) {
                    // Armazenar informações dos utilizadores no SessionManager
                    SessionManager.userId = user.id
                    SessionManager.username = user.username
                    SessionManager.saveUserSession(requireContext())
                    Toast.makeText(requireContext(), "Login bem sucedido!!", Toast.LENGTH_SHORT).show()
                    // Navegar para o HomeFragment
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Toast.makeText(requireContext(), "Utilizador ou senha incorretos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao fazer login", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
