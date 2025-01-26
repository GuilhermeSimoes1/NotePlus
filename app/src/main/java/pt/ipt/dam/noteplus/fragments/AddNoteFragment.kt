package pt.ipt.dam.noteplus.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.gson.Gson
import kotlinx.coroutines.launch
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.data.NoteDatabase
import pt.ipt.dam.noteplus.model.Note
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
class AddNoteFragment : Fragment(R.layout.addnote_fragment) {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var imageFile: File? = null
    private lateinit var noteImageView: ImageView
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.addnote_fragment, container, false)

        noteImageView = view.findViewById(R.id.noteImageView)

        view.findViewById<Button>(R.id.recordAudioButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                toggleRecording()
            }
        }

        view.findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 2)
            } else {
                takePhoto()
            }
        }

        noteImageView.setOnClickListener {
            takePhoto()
        }

        setHasOptionsMenu(true) // Adicione esta linha para habilitar o menu

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_addnote, menu) // Infle o menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveMenu -> {
                saveNote() // Chame o método para salvar a nota
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        val title = view?.findViewById<EditText>(R.id.addNoteTitle)?.text.toString()
        val description = view?.findViewById<EditText>(R.id.addNoteDesc)?.text.toString()
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Título e descrição não podem estar vazios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val note = Note(
            title = title,
            description = description,
            audioPath = audioFile?.absolutePath,
            imagePath = imageFile?.absolutePath
        )

        val gson = Gson()
        val noteJson = gson.toJson(note) // Converte o objeto Note para JSON

        val db = NoteDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            try {
                db.noteDao().insert(note)  // Insere a nota na base de dados
                val navController = findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "new_note_json",
                    noteJson
                ) // Armazena como String JSON
                navController.popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()  // Imprime o erro no Logcat
                Toast.makeText(requireContext(), "Erro ao salvar nota", Toast.LENGTH_SHORT).show()
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
        }
        mediaRecorder = null
    }

    @SuppressLint("QueryPermissionsNeeded")
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

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}