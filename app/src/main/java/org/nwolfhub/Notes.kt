package org.nwolfhub

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.nwolfhub.databinding.ActivityNotesBinding
import java.lang.Exception

class Notes : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide()
        binding = ActivityNotesBinding.inflate(layoutInflater)
        try {
            TestersApi().checkVersion(this)
        } catch (_:Exception) {}
        val preferences = getSharedPreferences("notes", MODE_PRIVATE)
        PublicShared.preferences = preferences
        PublicShared.activity = this
        val notes = ArrayList<Note>()
        val welcomedPref = getSharedPreferences("main", MODE_PRIVATE)
        val welcomed = welcomedPref.getBoolean("welcomed", false)
        if(!welcomed) {startActivity(Intent(this, Welcome::class.java)); finish()}
        for(note in preferences.all.entries) {
            if(!note.key.toString().equals("selected")) notes.add(Note(note.key.toString(), note.value.toString()))
        }
        setContentView(binding.root)
        val recyclerView = findViewById<RecyclerView>(R.id.notes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NotesRecyclerAdapter(notes)
        findViewById<Button>(R.id.webtimed).setOnClickListener{
            startActivity(Intent(this, WebLogin::class.java))
        }
        binding.fab.setOnClickListener { view ->
            preferences.edit().putString("selected", "newNote").apply()
            startActivity(Intent(this, EditActivity::class.java))
            finish()
        }
    }
    class NotesRecyclerAdapter(private val notes: List<Note>) :
        RecyclerView.Adapter<NotesRecyclerAdapter.MyViewHolder>() {
        class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), OnCreateContextMenuListener {
            init{
                itemView.setOnCreateContextMenuListener(this)
            }
            val nameText = itemView.findViewById<TextView>(R.id.noteName)
            val descriptionText = itemView.findViewById<TextView>(R.id.noteDescription)

            override fun onCreateContextMenu(
                menu: ContextMenu?,
                p1: View?,
                p2: ContextMenu.ContextMenuInfo?
            ) {
                menu?.setHeaderTitle("Select action")
                menu?.add(("Delete"))?.setOnMenuItemClickListener {
                    PublicShared.preferences.edit().remove(nameText.text.toString()).apply()
                    PublicShared.activity.startActivity(Intent(PublicShared.activity, Notes::class.java))
                    PublicShared.restart()
                    true
                }
            }
        }
        override fun getItemCount() = notes.size
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.nameText.text = notes[position].name
            holder.descriptionText.text = notes[position].description
            holder.itemView.setOnClickListener {
                PublicShared.preferences.edit().putString("selected", holder.nameText.text.toString()).apply()
                PublicShared.activity.startActivity(Intent(PublicShared.activity, EditActivity::class.java))
                PublicShared.activity.finish()
            }
            holder.itemView.setOnLongClickListener {
                holder.itemView.showContextMenu()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.note, parent, false)
            return MyViewHolder(itemView)
        }
    }
}