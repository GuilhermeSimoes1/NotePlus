package pt.ipt.dam.noteplus.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import pt.ipt.dam.noteplus.R

@Suppress("DEPRECATION")
class EditNoteFragment : Fragment(R.layout.editnote_fragment) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.editnote_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editnote, menu) // Infle o menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveMenu -> {
                // Ação para salvar a edição da nota
                true
            }
            R.id.deleteMenu -> {
                // Ação para deletar a nota
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}