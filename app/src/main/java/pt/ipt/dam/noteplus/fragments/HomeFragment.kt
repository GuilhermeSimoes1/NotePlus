package pt.ipt.dam.noteplus.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.adapter.NoteAdapter
import pt.ipt.dam.noteplus.data.NoteRepository
import pt.ipt.dam.noteplus.data.SessionManager
import pt.ipt.dam.noteplus.data.SheetyApi
import pt.ipt.dam.noteplus.model.Note

/**
 * Fragmento da homepage que exibe a lista de notas.
 */
@Suppress("DEPRECATION")
class HomeFragment : Fragment(R.layout.home_fragment) {

    private lateinit var noteAdapter: NoteAdapter
    private var allNotes: List<Note> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addNoteButton = view.findViewById<FloatingActionButton>(R.id.addNoteButton)
        addNoteButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.homeRecyclerView)
        noteAdapter = NoteAdapter(mutableListOf(), { note ->
            findNavController().navigate(
                R.id.action_homeFragment_to_editNoteFragment,
                bundleOf("noteId" to note.id)
            )
        }, { noteId ->
            deleteNote(noteId)
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = noteAdapter

        loadNotes()

        // Intercepta o botão de voltar para exibir um alerta de confirmação
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })
    }


    /**
     * Exibe um diálogo de confirmação ao pressionar "voltar" na homepage.
     */
    private fun showExitConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sair da aplicação")
            .setMessage("Quer fechar a aplicação?")
            .setPositiveButton("Sim") { _, _ ->
                requireActivity().finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    /**
     * Carrega as notas do utilizador.
     */
    private fun loadNotes() {
        val repository = NoteRepository(SheetyApi.service)
        val userId = SessionManager.userId ?: run {
            Toast.makeText(requireContext(), "Utilizador não logado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val userNotes = repository.getNotesForUser(userId)
                allNotes = userNotes
                updateUI(userNotes)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao carregar notas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Atualiza a interface do utilizador com as notas fornecidas.
     */
    private fun updateUI(notes: List<Note>) {
        noteAdapter.updateNotes(notes)

        val emptyNotesText = view?.findViewById<TextView>(R.id.emptyNotesText)
        val recyclerView = view?.findViewById<RecyclerView>(R.id.homeRecyclerView)
        if (notes.isEmpty()) {
            emptyNotesText?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            emptyNotesText?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }

    /**
     * Remove a nota localmente, atualizando a lista exibida ao utilizador.
     */
    private fun deleteNoteLocal(noteId: Int) {
        val noteIndex = allNotes.indexOfFirst { it.id == noteId }
        if (noteIndex != -1) {
            noteAdapter.removeNoteAt(noteIndex)
            allNotes = allNotes.filter { it.id != noteId }
        }
        filterNotes(currentSearchQuery)
    }

    /**
     * Remove a nota do servidor e atualiza a lista localmente após a exclusão bem-sucedida.
     */
    private fun deleteNote(noteId: Int) {
        lifecycleScope.launch {
            try {
                SheetyApi.service.deleteNote(noteId)
                Toast.makeText(requireContext(), "Parabéns por concluir esta nota!!", Toast.LENGTH_SHORT).show()
                deleteNoteLocal(noteId) // Atualizar localmente após exclusão bem-sucedida
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao apagar a nota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var searchJob: Job? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        val searchItem = menu.findItem(R.id.searchMenu)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    kotlinx.coroutines.delay(300) // Debounce delay
                    filterNotes(newText)
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    private var currentSearchQuery: String? = null

    /**
     * Filtra as notas com base na consulta de pesquisa fornecida.
     */
    private fun filterNotes(query: String?) {

        currentSearchQuery = query
        val filteredNotes = if (query.isNullOrEmpty()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
        updateUI(filteredNotes)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchMenu -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
