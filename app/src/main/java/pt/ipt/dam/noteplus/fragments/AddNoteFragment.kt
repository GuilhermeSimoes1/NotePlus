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
import pt.ipt.dam.noteplus.data.NoteRepository
import pt.ipt.dam.noteplus.data.SessionManager
import pt.ipt.dam.noteplus.data.SheetyApi
import pt.ipt.dam.noteplus.model.Note
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragmento para adicionar uma nova nota.
 */

@Suppress("DEPRECATION")
class AddNoteFragment : Fragment(R.layout.addnote_fragment) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    private var imageFile: File? = null
    private lateinit var noteImageView: ImageView
    private lateinit var playAudioButton: Button
    private lateinit var saveButton: Button
    private var isRecording = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.addnote_fragment, container, false)

        noteImageView = view.findViewById(R.id.noteImageView)
        playAudioButton = view.findViewById(R.id.playAudioButton)
        saveButton = view.findViewById(R.id.saveButton)

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

        playAudioButton.setOnClickListener {
            playAudio()
        }

        saveButton.setOnClickListener{
            saveNote()
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_addnote, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveMenu -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Guarda a nota no repositório.
     */
    private fun saveNote() {
        val title = view?.findViewById<EditText>(R.id.addNoteTitle)?.text.toString()
        val description = view?.findViewById<EditText>(R.id.addNoteDesc)?.text.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "O Título e a descrição não podem estar vazios!!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager.userId ?: run {
            Toast.makeText(requireContext(), "Utilizador não logado, por favor reinicie a aplicação!!", Toast.LENGTH_SHORT).show()
            return
        }

        val note = Note(
            userId = userId,
            title = title,
            description = description,
            audioPath = audioFile?.absolutePath,
            imagePath = imageFile?.absolutePath
        )


        val repository = NoteRepository(SheetyApi.service)
        lifecycleScope.launch {
            try {
                repository.createNoteInSheety(note)
                Toast.makeText(requireContext(), "Nota criada com sucesso!!",Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao guardar a nota :(", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Alterna entre iniciar e parar a gravação de áudio.
     *
     * @param playAudioButton Botão para reprodução de áudio que será ativado quando a gravação for concluída.
     */
    private fun toggleRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    /**
     * Para a gravação de áudio e ativa o botão de reprodução.
     *
     * @param playAudioButton Botão para reprodução de áudio que será ativado.
     */
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
                Toast.makeText(requireContext(), "Gravação iniciada, clique novamente no botão para parar", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Para a gravação de áudio.
     */
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


    /**
     * Reproduz o áudio gravado.
     */
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

    /**
     * Abre a câmara para capturar uma foto.
     *
     * @param noteImageView ImageView onde a foto será exibida.
     */
    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Cria o arquivo de imagem com um nome único
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        imageFile = File(requireContext().externalCacheDir?.absolutePath + "/photo_$timeStamp.jpg")

        // Usar FileProvider para criar um URI seguro
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(),
            "pt.ipt.dam.noteplus.fileprovider",
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
        mediaPlayer?.release()
        mediaPlayer = null
    }
}