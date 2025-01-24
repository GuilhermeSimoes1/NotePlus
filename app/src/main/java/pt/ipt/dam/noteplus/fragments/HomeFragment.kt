package pt.ipt.dam.noteplus.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import pt.ipt.dam.noteplus.R

@Suppress("DEPRECATION")
class HomeFragment : Fragment(R.layout.home_fragment) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) // Adicione esta linha para habilitar o menu
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu) // Infle o menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchMenu -> {
                // Ação para buscar notas
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}