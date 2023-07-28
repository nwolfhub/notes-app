package org.nwolfhub

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.nwolfhub.model.Note
import org.nwolfhub.util.Cache
import org.nwolfhub.util.UpdateColors
import org.nwolfhub.util.WebCacher
import java.lang.Exception


class EditActivity : AppCompatActivity() {
    private var nameModified = false
    private lateinit var noteName:TextView
    private lateinit var noteText:TextView
    private var prevName = "newNote"
    private lateinit var preferences:SharedPreferences
    private lateinit var selected:String
    private var online = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        window.statusBarColor = Color.parseColor("#000000")
        supportActionBar?.hide()
        UpdateColors.updateBars(this)
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); //I don't care if it is deprecated or not. Fuck the person who thought that replacing this with a fuckton of code is a great idea
        preferences = getSharedPreferences("notes", MODE_PRIVATE)
        val save = findViewById<Button>(R.id.save)
        selected = preferences.getString("selected", "newNote").toString()
        noteName = findViewById(R.id.noteNameEdit)
        noteText = findViewById(R.id.noteText)
        val cache = getSharedPreferences("cache", MODE_PRIVATE)
        val settings = getSharedPreferences("settings", MODE_PRIVATE)
        val autoSave = settings.getBoolean("autosave", false)
        val cacher = WebCacher(Cache(this))
        online = preferences.getBoolean("isOnline", false)
        if(online) {
            var prevNote = cacher.getCachedNote(selected)
            if(prevNote!=null) {
                noteText.text = prevNote.description
            }
            if(!selected.equals("newNote")) {
                Log.d("fetch online note", "Getting note from server")
                save.isEnabled=false
                Thread {
                    val response = OkHttpClient().newCall(
                        Request.Builder().url(
                            PublicShared.web.getString(
                                "server",
                                ""
                            ) + "/api/notes/get?name=$selected"
                        ).addHeader("token", PublicShared.web.getString("token", "").toString())
                            .build()
                    ).execute()
                    val code = response.code
                    val body = response.body?.string()
                    Log.d("fetch online note", "Received code $code")
                    response.close()
                    if(code==200) {
                        runOnUiThread {
                            noteText.text=JsonParser.parseString(body).asJsonObject.get("note").asString
                            noteText.isEnabled=true
                            save.isEnabled=true
                        }
                    } else {
                        Log.d("fetch online note",  "Error: $body")
                        runOnUiThread {
                            startActivity(Intent(this, Notes::class.java))
                            finish()
                        }
                    }
                }.start()
                prevName = selected
                noteName.text=selected
            } else {nameModified=true;noteText.isEnabled=true}
        } else {
            if(!selected.equals("newNote")) {noteName.text=selected;noteText.text=preferences.getString(selected, ""); prevName = selected} else nameModified=true
        }
        UpdateColors.updateColors(this, findViewById(R.id.mainEditLayout), save, noteName, noteText)
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
                if(noteText.equals("{ONLINECACHE}")) {
                    noteText.text="{ONLINECACHE"
                }
                if(!nameModified && autoSave) {
                    preferences.edit().putString(selected, noteText.text.toString()).apply()
                }
                cache.edit().putString(selected, noteText.text.toString()).apply()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
        save.setOnClickListener {
            save()
        }
    }

    override fun onBackPressed() {
        val cache = getSharedPreferences("cache", MODE_PRIVATE)
        AlertDialog.Builder(this).setTitle("Do you want to save note?").setMessage("Do you want to save current note?").setPositiveButton(
            "Yes"
        ){_, _ ->
            run {
                save()
            }
        }.setNegativeButton("No") { _, _ ->
            run {
                cache.edit().remove(selected).apply()
                startActivity(Intent(this, Notes::class.java).setData(Uri.parse("")))
                finish()
            }
        }.show()
    }

    private fun save() {
        if(online) {
            val btn = findViewById<Button>(R.id.save)
            val name = findViewById<EditText>(R.id.noteNameEdit)
            val text = findViewById<EditText>(R.id.noteText)
            WebCacher(Cache(this)).updateNote(Note(name.text.toString(), text.text.toString()))
            btn.isEnabled=false; name.isEnabled=false;text.isEnabled=false
            val noteName = name.text.toString()
            val noteText = text.text.toString()
            Log.d("save online note", "Saving note $noteName server " + PublicShared.web.getString("server", ""))
            Thread {
                val client = OkHttpClient()
                try {
                    val response = client.newCall(Request.Builder().url(PublicShared.web.getString("server", "") + "/api/notes/set?name=$noteName").post(noteText.toRequestBody()).addHeader("token", PublicShared.web.getString("token", "").toString()).build()).execute()
                    val code = response.code
                    val body = response.body?.string()
                    response.close()
                    Log.d("save online note", "Received code $code")
                    if(code==200) {
                        if(nameModified) {
                            Thread {
                                client.newCall(
                                    Request.Builder().url(
                                        PublicShared.web.getString(
                                            "server",
                                            ""
                                        ) + "/api/notes/delete?name=$noteName"
                                    ).addHeader(
                                        "token",
                                        PublicShared.web.getString("token", "").toString()
                                    ).build()
                                ).execute()
                            }.start()
                        }
                        runOnUiThread {
                            val cache = getSharedPreferences("cache", MODE_PRIVATE)
                            cache.edit().clear().apply()
                            startActivity(Intent(this, Notes::class.java))
                            finish()
                        }
                    } else {
                        btn.isEnabled=true; name.isEnabled=true;text.isEnabled=true
                        Log.d("save online note", "Received error: $body")
                        runOnUiThread {
                            Toast.makeText(this, "Failed to save note!", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e:Exception) {
                    Log.d("save online note", "Exception occurred: $e")
                    btn.isEnabled=true; name.isEnabled=true;text.isEnabled=true
                    Toast.makeText(this, "Failed to save note!", Toast.LENGTH_LONG).show()
                }
            }.start()
        } else {
            val cache = getSharedPreferences("cache", MODE_PRIVATE)
            Log.d("saveInfo", "Modified: $nameModified, prev: $prevName")
            if (nameModified && prevName != "newNote") preferences.edit().remove(prevName)
                .putString(noteName.text.toString(), noteText.text.toString()).apply()
            else preferences.edit().putString(noteName.text.toString(), noteText.text.toString())
                .apply()
            preferences.edit().putString("selected", "newNote").apply()
            cache.edit().remove(selected).apply()
            startActivity(Intent(this, Notes::class.java).setData(Uri.parse("")))
            finish()
        }
    }

}