package pt.ipt.dam.noteplus.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pt.ipt.dam.noteplus.R

class HomeFragment : Fragment(R.layout.home_fragment){
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        view.findViewById<FloatingActionButton>(R.id.addNoteButton).setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_addNoteFragment)
        }
    }
}