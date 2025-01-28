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
import pt.ipt.dam.noteplus.data.SheetyApi
import pt.ipt.dam.noteplus.data.UserRepository
import pt.ipt.dam.noteplus.model.User

class RegistoFragment : Fragment(R.layout.registo) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.registo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerButton = view.findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            val username = view.findViewById<TextView>(R.id.registerNome).text.toString()
            val password = view.findViewById<TextView>(R.id.registerPassword).text.toString()
            registerUser(username, password)
        }

        val registerLink = view.findViewById<TextView>(R.id.registerLink)
        registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun registerUser(username: String, password: String) {
        val user = User(username = username, password = password)

        val repository = UserRepository(SheetyApi.service)
        lifecycleScope.launch {
            try {
                repository.createUser(user)
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao registrar o utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
