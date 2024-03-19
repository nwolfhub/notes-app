package org.nwolfhub.notes.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import org.nwolfhub.notes.Notes
import org.nwolfhub.notes.R
import org.nwolfhub.notes.model.Note

class NotesAdapter (private val dataset: Array<Note>, private val cacher: WebCacher, private val context: Context) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteText: TextView
        val noteName: TextView
        lateinit var id: String
        init {
            noteText = view.findViewById(R.id.noteTextUpdated)
            noteName = view.findViewById(R.id.noteNameUpdated)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.noteText.text=dataset[position].content
        holder.noteName.text=dataset[position].name
        holder.id = dataset[position].id
        holder.itemView.setOnLongClickListener {
            holder.noteName.text=""
            
            true
        }
    }
}