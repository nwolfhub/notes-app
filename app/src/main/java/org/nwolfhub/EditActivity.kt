package org.nwolfhub

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {
    private var nameModified = false
    private lateinit var noteName:TextView
    private lateinit var noteText:TextView
    private var prevName = "newNote"
    private lateinit var preferences:SharedPreferences
    private lateinit var selected:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        supportActionBar?.hide()
        preferences = getSharedPreferences("notes", MODE_PRIVATE)
        val save = findViewById<Button>(R.id.save)
        selected = preferences.getString("selected", "newNote").toString()
        noteName = findViewById(R.id.noteNameEdit)
        noteText = findViewById(R.id.noteText)
        val cache = getSharedPreferences("cache", MODE_PRIVATE)
        val settings = getSharedPreferences("settings", MODE_PRIVATE)
        val autoSave = settings.getBoolean("autosave", false)
        if(!selected.equals("newNote")) {noteName.text=selected;noteText.text=preferences.getString(selected, ""); prevName = selected} else nameModified=true
        if(cache.contains(selected)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cached data found").setMessage("Cached data was found. Do you want to recover it?").setPositiveButton("Yes") { _, _ -> noteText.text = cache.getString(selected, "")}.setNegativeButton("No"){_, _ -> cache.edit().remove(selected).apply()}
            builder.show()
        }
        noteName.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(noteName.text.toString().equals("newNote")) {
                    noteName.text = "newNot"
                }
                if(noteName.text.toString().equals("selected")) {
                    noteName.text = "selecte"
                }
                nameModified=true
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
        noteText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!nameModified && autoSave) {
                    preferences.edit().putString(selected, noteText.text.toString()).apply()
                }
                cache.edit().putString(selected, noteText.text.toString()).apply()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        save.setOnClickListener {
            Log.d("saveInfo", "Modified: $nameModified, prev: $prevName")
            if (nameModified && prevName != "newNote") preferences.edit().remove(prevName).putString(noteName.text.toString(), noteText.text.toString()).apply()
            else preferences.edit().putString(noteName.text.toString(), noteText.text.toString()).apply()
            preferences.edit().putString("selected", "newNote").apply()
            cache.edit().remove(selected).apply()
            startActivity(Intent(this, Notes::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).setTitle("Do you want to save note?").setMessage("Do you want to save current note?").setPositiveButton("Yes", ){_, _ ->
            run {
                val cache = getSharedPreferences("cache", MODE_PRIVATE)
                if (nameModified && prevName != "newNote") preferences.edit().remove(prevName)
                    .putString(noteName.text.toString(), noteText.text.toString()).apply()
                else preferences.edit()
                    .putString(noteName.text.toString(), noteText.text.toString()).apply()
                preferences.edit().putString("selected", "newNote").apply()
                cache.edit().remove(selected).apply()
                startActivity(Intent(this, Notes::class.java))
                finish()
            }
        }.setNegativeButton("No") { _, _ ->
            run {
                startActivity(Intent(this, Notes::class.java))
                finish()
            }
        }.show()
    }
}