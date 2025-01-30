package pt.ipt.dam.noteplus.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ipt.dam.noteplus.R

/**
 * Fragmento para a página de informações.
 */
class InfoFragment : Fragment(R.layout.info_page) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.info_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backToLoginButton = view.findViewById<Button>(R.id.BackTologinButton)
        backToLoginButton.setOnClickListener {
            findNavController().navigate(R.id.action_infoFragment_to_loginFragment)
        }
    }
}
