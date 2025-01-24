package pt.ipt.dam.noteplus.fragments
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import pt.ipt.dam.noteplus.R
import java.io.File
import java.io.IOException
@Suppress("DEPRECATION")
class AddNoteFragment : Fragment(R.layout.addnote_fragment) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.addnote_fragment, container, false)
        view.findViewById<Button>(R.id.recordAudioButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            } else {
                startRecording()
            }
        }
        view.findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 2)
            } else {
                takePhoto()
            }
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
        // Aqui você pode salvar a nota no banco de dados ou onde preferir
        // Por exemplo, você pode usar o Room Database para salvar a nota
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
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
        }
    }
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }
    @SuppressLint("QueryPermissionsNeeded")
    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, 3)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startRecording()
                }
            }
            2 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePhoto()
                }
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            // Aqui pode-se salvar a imagem ou fazer o que for necessário com ela
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

}
