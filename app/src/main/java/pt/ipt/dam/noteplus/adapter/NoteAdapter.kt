package pt.ipt.dam.noteplus.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.model.Note

/**
 * Adaptador para exibir notas numa RecyclerView.
 *
 * @property notes Lista de notas a ser exibida.
 * @property onNoteClick Função a ser executada quando uma nota é clicada.
 * @property onDoneClick Função a ser executada quando o botão de conclusão é clicado.
 */
class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onDoneClick: (Int) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    /**
     * ViewHolder para a nota.
     *
     * @property noteTitle Título da nota.
     * @property noteDesc Descrição da nota.
     * @property noteImageView Imagem da nota.
     * @property doneNoteButton Botão para marcar a nota como concluída.
     */
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.noteTitle)
        val noteDesc: TextView = itemView.findViewById(R.id.noteDesc)
        val noteImageView: ImageView = itemView.findViewById(R.id.noteImageView)
        val doneNoteButton: FloatingActionButton = itemView.findViewById(R.id.doneButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note.title
        holder.noteDesc.text = note.description

        if (note.imagePath != null) {
            holder.noteImageView.visibility = View.VISIBLE
            holder.noteImageView.setImageBitmap(BitmapFactory.decodeFile(note.imagePath))
        } else {
            holder.noteImageView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }

        holder.doneNoteButton.setOnClickListener {
            note.id?.let {
                onDoneClick(it)
            }
        }
    }

    override fun getItemCount() = notes.size

    /**
     * Atualiza a lista de notas exibida pela RecyclerView.
     *
     * @param newNotes Nova lista de notas.
     */
    fun updateNotes(newNotes: List<Note>) {
        val diffCallback = NoteDiffCallback(notes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notes.clear()
        notes.addAll(newNotes)
        diffResult.dispatchUpdatesTo(this)
    }


    /**
     * Implementação de DiffUtil.Callback para calcular as diferenças entre listas de notas.
     *
     * @property oldList Lista antiga de notas.
     * @property newList Lista nova de notas.
     */
    class NoteDiffCallback(
        private val oldList: List<Note>,
        private val newList: List<Note>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
