package pt.ipt.dam.noteplus.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.NoteDatabase
import pt.ipt.dam.noteplus.model.Note
import java.io.File

@Suppress("DEPRECATION")
class EditNoteFragment : Fragment(R.layout.editnote_fragment) {
    private var noteId: Int? = null
    private lateinit var noteTitle: EditText
    private lateinit var noteDesc: EditText
    private lateinit var noteImageView: ImageView
    private var imageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.editnote_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referências às views
        noteTitle = view.findViewById(R.id.editNoteTitle)
        noteDesc = view.findViewById(R.id.editNoteDesc)
        noteImageView = view.findViewById(R.id.noteImageView)

        // Configurar o FAB (Floating Action Button) para salvar as alterações
        val editNoteFab = view.findViewById<FloatingActionButton>(R.id.editNoteFab)
        editNoteFab.setOnClickListener {
            Log.d("EditNoteFragment", "Botão editNoteFab clicado")
            updateNote() // Chama a função para atualizar a nota
        }

        // Configurar o ImageView para tirar uma nova foto
        noteImageView.setOnClickListener {
            takePhoto()
        }

        // Obter o ID da nota passada como argumento
        noteId = arguments?.getInt("noteId")
        Log.d("EditNoteFragment", "ID da nota recebida: $noteId")

        // Carregar os dados da nota para edição
        loadNote()
    }

    private fun loadNote() {
        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            noteId?.let {
                val note = db.noteDao().getNoteById(it)
                noteTitle.setText(note.title)
                noteDesc.setText(note.description)
                // Carregar imagem se existir
                note.imagePath?.let { path ->
                    imageFile = File(path)
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
                Log.d("EditNoteFragment", "Save menu item clicked")
                updateNote()
                true
            }
            R.id.deleteMenu -> {
                Log.d("EditNoteFragment", "Delete menu item clicked")
                deleteNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateNote() {
        val title = noteTitle.text.toString()
        val description = noteDesc.text.toString()

        if (noteId == null) {
            Log.e("EditNoteFragment", "ID da nota está nulo. Não é possível atualizar.")
            Toast.makeText(requireContext(), "Erro interno: Nota inválida", Toast.LENGTH_SHORT).show()
            return
        }

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
            imagePath = imageFile?.absolutePath, // Atualize com o caminho da imagem
            audioPath = null  // Atualize se necessário
        )

        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            try {
                Log.d("EditNoteFragment", "Atualizando nota: $note")
                db.noteDao().update(note)
                Toast.makeText(requireContext(), "Nota atualizada com sucesso", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Log.e("EditNoteFragment", "Erro ao atualizar a nota", e)
                Toast.makeText(requireContext(), "Erro ao atualizar a nota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Cria o arquivo de imagem
        imageFile = File(requireContext().externalCacheDir?.absolutePath + "/photo.jpg")

        // Usar FileProvider para criar um URI seguro
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(),
            "pt.ipt.dam.noteplus.fileprovider", // Nome do seu provider no AndroidManifest
            imageFile!!
        )

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        // Verifica se há uma Activity que pode lidar com a intenção
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, 3)
        } else {
            Toast.makeText(requireContext(), "Não foi possível aceder à câmera.", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == AppCompatActivity.RESULT_OK) {
            // A imagem já foi salva no arquivo imageFile
            noteImageView.visibility = View.VISIBLE
            val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
            noteImageView.setImageBitmap(bitmap)
        }
    }

    private fun deleteNote() {
        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            noteId?.let {
                db.noteDao().delete(it)
                Toast.makeText(requireContext(), "Nota apagada com sucesso", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}