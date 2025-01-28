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
    }

    private fun loginUser(username: String, password: String) {
        val repository = UserRepository(SheetyApi.service)
        lifecycleScope.launch {
            try {
                val response = repository.getUsers()
                val user = response.users.find { it.username == username && it.password == password }
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
