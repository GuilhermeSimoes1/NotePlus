package pt.ipt.dam.noteplus.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.NoteDatabase
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
    private lateinit var playAudioButton: Button
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var imageFile: File? = null
    private var isRecording = false

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
        playAudioButton = view.findViewById(R.id.playAudioButton)

        // Configurar o FAB (Floating Action Button) para salvar as alterações
        val editNoteFab = view.findViewById<Button>(R.id.editNoteFab)
        editNoteFab.setOnClickListener {
            Log.d("EditNoteFragment", "Botão editNoteFab clicado")
            updateNote() // Chama a função para atualizar a nota
        }

        // Configurar o ImageView para tirar uma nova foto
        noteImageView.setOnClickListener {
            takePhoto()
        }

        // Configurar o botão de gravação de áudio
        view.findViewById<Button>(R.id.recordAudioButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                toggleRecording()
            }
        }

        // Configurar o botão de reprodução de áudio
        playAudioButton.setOnClickListener {
            playAudio()
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
                // Carregar áudio se existir
                note.audioPath?.let { path ->
                    audioFile = File(path)
                    playAudioButton.visibility = View.VISIBLE
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
            audioPath = audioFile?.absolutePath  // Atualize com o caminho do áudio
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

    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        audioFile = File(requireContext().externalCacheDir?.absolutePath + "/audiorecordtest.3gp")
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile?.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                isRecording = true
                Toast.makeText(requireContext(), "Gravação iniciada", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            isRecording = false
            Toast.makeText(requireContext(), "Gravação finalizada", Toast.LENGTH_SHORT).show()
            playAudioButton.visibility = View.VISIBLE
        }
        mediaRecorder = null
    }

    private fun playAudio() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFile?.absolutePath)
                prepare()
                start()
                Toast.makeText(requireContext(), "Reproduzindo áudio", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao reproduzir áudio", Toast.LENGTH_SHORT).show()
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
