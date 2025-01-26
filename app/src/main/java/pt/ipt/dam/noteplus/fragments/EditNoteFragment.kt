package pt.ipt.dam.noteplus.fragments
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.NoteDatabase
import pt.ipt.dam.noteplus.model.Note
@Suppress("DEPRECATION")
class EditNoteFragment : Fragment(R.layout.editnote_fragment) {
    private var noteId: Int? = null
    private lateinit var noteTitle: EditText
    private lateinit var noteDesc: EditText
    private lateinit var noteImageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.editnote_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteTitle = view.findViewById(R.id.editNoteTitle)
        noteDesc = view.findViewById(R.id.editNoteDesc)
        noteImageView = view.findViewById(R.id.noteImageView)
        noteId = arguments?.getInt("noteId")
        loadNote()
    }
    private fun loadNote() {
        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            noteId?.let {
                val note = db.noteDao().getNoteById(it)
                noteTitle.setText(note.title)
                noteDesc.setText(note.description)
                // Carregar imagem e áudio se existirem
                note.imagePath?.let { path ->
                    noteImageView.setImageBitmap(BitmapFactory.decodeFile(path))
                    noteImageView.visibility = View.VISIBLE
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editnote, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveMenu -> {
                updateNote()
                true
            }
            R.id.deleteMenu -> {
                deleteNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun updateNote() {
        val title = noteTitle.text.toString()
        val description = noteDesc.text.toString()
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Título e descrição não podem estar vazios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val note = Note(
            id = noteId!!,
            title = title,
            description = description,
            imagePath = null, // Atualize com o caminho da imagem se necessário
            audioPath = null  // Atualize com o caminho do áudio se necessário
        )
        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.noteDao().update(note)
            findNavController().popBackStack()
        }
    }
    private fun deleteNote() {
        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            noteId?.let {
                db.noteDao().delete(it)
                findNavController().popBackStack()
            }
        }
    }
}
