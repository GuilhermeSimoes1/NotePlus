package pt.ipt.dam.noteplus.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.NoteRepository
import pt.ipt.dam.noteplus.data.SessionManager
import pt.ipt.dam.noteplus.data.SheetyApi
import pt.ipt.dam.noteplus.model.Note
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class EditNoteFragment : Fragment(R.layout.editnote_fragment) {
    private var noteId: Int? = null
    private lateinit var noteTitle: EditText
    private lateinit var noteDesc: EditText
    private lateinit var noteImageView: ImageView
    private var imageFile: File? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playAudioButton: Button
    private lateinit var backToHomeButton: Button
    private var audioFilePath: String? = null
    private var originalTitle: String? = null
    private var originalDescription: String? = null
    private var originalImagePath: String? = null

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
        playAudioButton = view.findViewById(R.id.playAudioButton) // Inicialize o botão de reprodução de áudio
        backToHomeButton = view.findViewById(R.id.backToHomeButton)
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

        // Configurar o botão de reprodução de áudio
        playAudioButton.setOnClickListener {
            playAudio()
        }

        // Configurar o botão de voltar à homepage
        backToHomeButton.setOnClickListener {
            if (hasChanges()) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigate(R.id.editNoteFragment_to_homeFragment)
            }


        }

        // Obter o ID da nota passada como argumento
        noteId = arguments?.getInt("noteId")
        Log.d("EditNoteFragment", "ID da nota recebida: $noteId")

        // Carregar os dados da nota para edição
        loadNote()
    }


    private val repository by lazy {
        NoteRepository(SheetyApi.service)
    }

    private fun loadNote() {
        lifecycleScope.launch {
            noteId?.let { id ->
                try {
                    val notes = repository.getNotesForUser(SessionManager.userId ?: 0)
                    val note = notes.find { it.id == id }
                    if (note != null) {
                        noteTitle.setText(note.title)
                        noteDesc.setText(note.description)
                        originalTitle = note.title
                        originalDescription = note.description
                        originalImagePath = note.imagePath

                        // Carregar imagem se existir
                        note.imagePath?.let { path ->
                            imageFile = File(path)
                            noteImageView.setImageBitmap(BitmapFactory.decodeFile(path))
                            noteImageView.visibility = View.VISIBLE
                        }

                        // Configurar a visibilidade do botão de reprodução de áudio
                        if (!note.audioPath.isNullOrEmpty()) {
                            audioFilePath = note.audioPath
                            playAudioButton.visibility = View.VISIBLE
                        } else {
                            playAudioButton.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(requireContext(), "Nota não encontrada", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("EditNoteFragment", "Erro ao carregar nota", e)
                    Toast.makeText(requireContext(), "Erro ao carregar nota", Toast.LENGTH_SHORT).show()
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
            userId = SessionManager.userId ?: 0,
            title = title,
            description = description,
            imagePath = imageFile?.absolutePath,
            audioPath = audioFilePath
        )

        lifecycleScope.launch {
            try {
                Log.d("EditNoteFragment", "A atualizar a nota via NoteRepository: $note")
                repository.updateNoteInSheety(note)
                Toast.makeText(requireContext(), "Nota atualizada com sucesso!!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Log.e("EditNoteFragment", "Erro ao atualizar a nota", e)
                Toast.makeText(requireContext(), "Erro ao atualizar a nota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Cria o arquivo de imagem com um nome único
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        imageFile = File(requireContext().externalCacheDir?.absolutePath + "/photo_$timeStamp.jpg")

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

    private fun playAudio() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                Toast.makeText(requireContext(), "A reproduzir o áudio", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao reproduzir áudio", Toast.LENGTH_SHORT).show()
            }
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

        lifecycleScope.launch {
            try {
                noteId?.let {
                    SheetyApi.service.deleteNote(it)
                    Toast.makeText(requireContext(), "Nota apagada com sucesso", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                Log.e("EditNoteFragment", "Erro ao apagar a nota", e)
                Toast.makeText(requireContext(), "Erro ao apagar a nota", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.editNoteFragment_to_homeFragment)
            }
        }
    }

    private fun hasChanges(): Boolean {
        val currentTitle = noteTitle.text.toString()
        val currentDescription = noteDesc.text.toString()
        val currentImagePath = imageFile?.absolutePath
        return currentTitle != originalTitle || currentDescription != originalDescription || currentImagePath != originalImagePath
    }


    private fun showDiscardChangesDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Descartar alterações?")
        builder.setMessage("Você tem alterações que não foram guardadas. Deseja descartá-las e voltar à página inicial?")
        builder.setPositiveButton("Descartar") { _, _ ->
            findNavController().navigate(R.id.editNoteFragment_to_homeFragment)
        }
        builder.setNegativeButton("Continuar a editar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}