package pt.ipt.dam.noteplus.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ipt.dam.noteplus.R

class RegistoFragment : Fragment(R.layout.registo){

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
            // Lógica de registro aqui
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        val registerLink = view.findViewById<Button>(R.id.registerLink)
        registerLink.setOnClickListener {
            // Lógica de registro aqui
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}