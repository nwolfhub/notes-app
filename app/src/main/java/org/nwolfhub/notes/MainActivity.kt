package org.nwolfhub.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import org.nwolfhub.notes.model.Note
import org.nwolfhub.notes.util.NotesAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dataset = arrayOf<Note>()
        val adapter = NotesAdapter(dataset)
        val recyclerView:RecyclerView = findViewById(R.id.notesList)
        recyclerView.adapter=adapter
        startActivity(Intent(this, LoginActivity::class.java))
    }
}