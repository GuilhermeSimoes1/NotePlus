package pt.ipt.dam.noteplus.fragments

import pt.ipt.dam.noteplus.adapter.NoteAdapter
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.NoteDatabase

@Suppress("DEPRECATION")
class HomeFragment() : Fragment(R.layout.home_fragment), Parcelable {

    private lateinit var noteAdapter: NoteAdapter

    constructor(parcel: Parcel) : this() {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true) //linha para habilitar o menu
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addNoteButton = view.findViewById<FloatingActionButton>(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }

        // Configuração do RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.homeRecyclerView)
        noteAdapter = NoteAdapter(mutableListOf()) // Definindo o adaptador
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = noteAdapter

        // Carregar notas da base de dados
        loadNotes()
    }

    private fun loadNotes() {
        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            val notes = db.noteDao().getAllNotes()
            noteAdapter.updateNotes(notes)

            // Exibir mensagem se não houver notas
            val emptyNotesText = view?.findViewById<TextView>(R.id.emptyNotesText)
            if (notes.isEmpty()) {
                emptyNotesText?.visibility = View.VISIBLE
                view?.findViewById<RecyclerView>(R.id.homeRecyclerView)?.visibility = View.GONE
            } else {
                emptyNotesText?.visibility = View.GONE
                view?.findViewById<RecyclerView>(R.id.homeRecyclerView)?.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu) // Infle o menu
        val searchItem = menu.findItem(R.id.searchMenu)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Lógica para pesquisar as notas
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Lógica para atualizar a pesquisa em tempo real
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchMenu -> {
                // Ação para encontrar as notas
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeFragment> {
        override fun createFromParcel(parcel: Parcel): HomeFragment {
            return HomeFragment(parcel)
        }

        override fun newArray(size: Int): Array<HomeFragment?> {
            return arrayOfNulls(size)
        }
    }
}