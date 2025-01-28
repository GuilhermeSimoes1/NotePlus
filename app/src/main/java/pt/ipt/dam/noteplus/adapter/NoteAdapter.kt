package pt.ipt.dam.noteplus.adapter
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam.noteplus.R
import pt.ipt.dam.noteplus.model.Note



class NoteAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
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
    fun updateNotes(newNotes: List<Note>) {
        val diffCallback = NoteDiffCallback(notes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notes = newNotes
        diffResult.dispatchUpdatesTo(this)
    }
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.noteTitle)
        val noteDesc: TextView = itemView.findViewById(R.id.noteDesc)
        val noteImageView: ImageView = itemView.findViewById(R.id.noteImageView)
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
    }
    override fun getItemCount() = notes.size
}
