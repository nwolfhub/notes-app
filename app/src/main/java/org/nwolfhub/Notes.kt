package org.nwolfhub

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.nwolfhub.databinding.ActivityNotesBinding
import org.nwolfhub.model.Note
import org.nwolfhub.util.Cache
import org.nwolfhub.util.TestersApi
import org.nwolfhub.util.UpdateColors
import org.nwolfhub.util.WebCacher
import org.nwolfhub.util.WebUtils
import java.lang.Exception

class Notes : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNotesBinding
    private var onlineNotes:ArrayList<Note> = ArrayList()
    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val intentData = intent.data
        val cacher = WebCacher(Cache(this))
        UpdateColors.updateBars(this)
        PublicShared.web = getSharedPreferences("web", MODE_PRIVATE)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        val webPref = getSharedPreferences("web", MODE_PRIVATE)
        val token = webPref.getString("token", "")
        val server = webPref.getString("server", "")
        try {
            TestersApi().checkVersion(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val preferences = getSharedPreferences("notes", MODE_PRIVATE)
        PublicShared.preferences = preferences
        PublicShared.activity = this
        val notes = ArrayList<Note>()
        val welcomedPref = getSharedPreferences("main", MODE_PRIVATE)
        val welcomed = welcomedPref.getBoolean("welcomed", false)
        val serversPreferences = getSharedPreferences("servers", MODE_PRIVATE)
        if(serversPreferences.getString("servers", "").equals("")) {
            serversPreferences.edit().putString("servers", "Nwolfhub (official)").putString("Nwolfhub (official)", "https://notes.nwolfhub.org").apply()
            Log.d("servers scan", "No servers were found, added Nwolfhub to list")
        }
        if (!welcomed) {
            startActivity(Intent(this, Welcome::class.java)); finish()
        }
        notes.addAll(rebuildLocalNotesList())
        setContentView(binding.root)
        val recyclerView = findViewById<RecyclerView>(R.id.notes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notes.addAll(cacher.getCachedNotes());
        recyclerView.adapter = NotesRecyclerAdapter(notes)
        findViewById<Button>(R.id.webtimed).setOnClickListener {
            startActivity(Intent(this, WebInfo::class.java))
            finish()
        }
        binding.fab.setOnClickListener {
            preferences.edit().putString("selected", "newNote").putBoolean("isOnline", false).apply()
            startActivity(Intent(this, EditActivity::class.java))
            finish()
        }
        if(intentData==null) {
            Thread {
                cacher.runUpdateNotes(server.toString(), token.toString(), this, findViewById(R.id.fetchOnlineNotes))
                runOnUiThread {
                    val basicNotesList = rebuildLocalNotesList()
                    basicNotesList.addAll(cacher.getCachedNotes())
                    recyclerView.adapter = NotesRecyclerAdapter(basicNotesList)
                }
            }.start()
        }
    }
    private fun rebuildLocalNotesList():ArrayList<Note> {
        val preferences = getSharedPreferences("notes", MODE_PRIVATE)
        val notes = ArrayList<Note>()
        for (note in preferences.all.entries) {
            if (!note.key.toString().equals("selected") && !note.key.toString().equals("isOnline")) notes.add(
                Note(
                    note.key.toString(),
                    note.value.toString()
                )
            )
        }
        return notes
    }
    class NotesRecyclerAdapter(private val notes: List<Note>) :
        RecyclerView.Adapter<NotesRecyclerAdapter.MyViewHolder>() {
        class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), OnCreateContextMenuListener {
            init{
                itemView.setOnCreateContextMenuListener(this)
            }
            val nameText = itemView.findViewById<TextView>(R.id.noteName)
            val descriptionText = itemView.findViewById<TextView>(R.id.noteDescription)
            val local = itemView.findViewById<ImageView>(R.id.onlineStatus)
            override fun onCreateContextMenu(
                menu: ContextMenu?,
                p1: View?,
                p2: ContextMenu.ContextMenuInfo?
            ) {
                menu?.setHeaderTitle("Select action")
                menu?.add(("Delete"))?.setOnMenuItemClickListener { // TODO: Add confirmation dialog
                    AlertDialog.Builder(PublicShared.activity).setTitle("Are you sure that you want to delete this note?").setPositiveButton("yes") {_, _ ->
                        val name = nameText.text.toString()
                        if (local.tag.equals("online")) {
                            PublicShared.activity.runOnUiThread {
                                PublicShared.activity.findViewById<RecyclerView>(R.id.notes).isEnabled =
                                    false
                            }
                            Log.d("delete note", "Attempting to delete note $name online")
                            Thread {
                                try {
                                    val response = OkHttpClient().newCall(
                                        Request.Builder().url(
                                            PublicShared.web.getString(
                                                "server",
                                                ""
                                            ) + "/api/notes/delete?note=$name"
                                        ).addHeader(
                                            "token",
                                            PublicShared.web.getString("token", "").toString()
                                        ).build()
                                    ).execute()
                                    val code = response.code
                                    val body = response.body?.string()
                                    Log.d("delete note", "Received code $code")
                                    response.close()
                                    if (code == 200) {
                                        PublicShared.restart()
                                    } else {
                                        Log.d("Delete note", "Error body: $body")
                                        PublicShared.activity.runOnUiThread {
                                            Toast.makeText(
                                                PublicShared.activity,
                                                "Failed to delete note",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                } catch (_: Exception) {
                                    PublicShared.activity.runOnUiThread {
                                        PublicShared.activity.findViewById<RecyclerView>(R.id.notes).isEnabled =
                                            true
                                    }
                                }
                            }.start()
                        } else {
                            PublicShared.preferences.edit().remove(nameText.text.toString()).apply()
                            PublicShared.restart()
                        }
                    }.setNegativeButton("No") {i, _ -> i.dismiss()}.show()
                    true
                }
                if(local.tag.equals("online")) {
                    menu?.add("Download note")?.setOnMenuItemClickListener {
                        Thread {
                            if (WebUtils.checkAuth(
                                    PublicShared.web.getString("token", ""),
                                    PublicShared.web.getString("server", "")
                                )
                            ) {
                                Log.d("download note", "Downloading note " + nameText.text)
                                PublicShared.activity.runOnUiThread {
                                    PublicShared.activity.findViewById<RecyclerView>(R.id.notes).isEnabled =
                                        false
                                }
                                val name = nameText.text.toString()
                                Thread {
                                    val response = OkHttpClient().newCall(
                                        Request.Builder().url(
                                            PublicShared.web.getString(
                                                "server",
                                                ""
                                            ) + "/api/notes/get?name=$name"
                                        ).addHeader(
                                            "token",
                                            PublicShared.web.getString("token", "").toString()
                                        ).build()
                                    ).execute()
                                    val code = response.code
                                    val body = response.body?.string()
                                    response.close()
                                    Log.d("download note", "Received code $code")
                                    if (code == 200) {
                                        PublicShared.preferences.edit().putString(
                                            name,
                                            JsonParser.parseString(body).asJsonObject.get("note").asString
                                        ).apply()
                                        PublicShared.restart()
                                    } else {
                                        Log.d("download note", "Error body: $body")
                                        PublicShared.activity.runOnUiThread {
                                            Toast.makeText(
                                                PublicShared.activity,
                                                "Failed to download note",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            PublicShared.activity.findViewById<RecyclerView>(R.id.notes).isEnabled =
                                                true
                                        }
                                    }
                                }.start()
                            } else {
                                PublicShared.activity.runOnUiThread {
                                    Toast.makeText(
                                        PublicShared.activity,
                                        "You are offline",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.start()
                        true
                    }
                } else {
                    menu?.add("Upload note")?.setOnMenuItemClickListener {
                        Thread{
                            if (WebUtils.checkAuth(
                                    PublicShared.web.getString("token", ""),
                                    PublicShared.web.getString("server", "")
                                )
                            ) {
                                Log.d("upload note", "Uploading note " + nameText.text)
                                PublicShared.activity.runOnUiThread {
                                    PublicShared.activity.findViewById<RecyclerView>(R.id.notes).isEnabled =
                                        false
                                }
                                val name = nameText.text.toString()
                                Thread {
                                    val response = OkHttpClient().newCall(
                                        Request.Builder().url(
                                            PublicShared.web.getString(
                                                "server",
                                                ""
                                            ) + "/api/notes/set?name=$name"
                                        ).addHeader(
                                            "token",
                                            PublicShared.web.getString("token", "").toString()
                                        ).post(
                                            PublicShared.preferences.getString(name, "").toString().toRequestBody()
                                        ).build()
                                    ).execute()
                                    val code = response.code
                                    val body = response.body?.string()
                                    response.close()
                                    Log.d("upload note", "Received code $code")
                                    if (code == 200) {
                                        PublicShared.restart()
                                    } else {
                                        Log.d("upload note", "Error body: $body")
                                        PublicShared.activity.runOnUiThread {
                                            Toast.makeText(
                                                PublicShared.activity,
                                                "Failed to upload note",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            PublicShared.activity.findViewById<RecyclerView>(R.id.notes).isEnabled = true
                                        }
                                    }
                                }.start()
                            } else {
                                PublicShared.activity.runOnUiThread {
                                    Toast.makeText(
                                        PublicShared.activity,
                                        "You are offline",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }.start()
                        true
                    }
                }
            }
        }
        override fun getItemCount() = notes.size
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.nameText.text = notes[position].name
            holder.descriptionText.text = notes[position].description
            Log.d("note parsing", "Parsing note " + notes[position].name + ": " + notes[position].online)
            if(notes[position].online) holder.local.setImageResource(R.drawable.web) else holder.local.setBackgroundResource(R.drawable.local)
            if(notes[position].online) holder.local.tag="online" else holder.local.tag="offline"
            holder.itemView.setOnClickListener {
                PublicShared.preferences.edit().putString("selected", holder.nameText.text.toString()).apply()
                if(notes[position].online) PublicShared.preferences.edit().putBoolean("isOnline", true).apply() else PublicShared.preferences.edit().putBoolean("isOnline", false).apply()
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

    @Deprecated("Functions moved to webCache")
    private fun getOnlineNotes(preferences:SharedPreferences){
        val token = preferences.getString("token", "")
        val server = preferences.getString("server", "")
        val bar = findViewById<ProgressBar>(R.id.fetchOnlineNotes)
        Log.d("fetch notes", "Fetching notes from $server")
        bar.progress = 2
        Thread {
            val client = OkHttpClient()
            val response = client.newCall(Request.Builder().url("$server/api/notes/getAll").addHeader("token", token.toString()).build()).execute()
            val code = response.code
            val body = response.body?.string()
            response.close()
            Log.d("fetch notes", "Received code: $code")
            if(code==200) {
                runOnUiThread {
                    bar.progress = 3
                }
                Thread{
                    var r = 3
                    var g = 218
                    var b = 197
                    for (i in 1..255) { //(3,218,197) -> (14, 227, 67)
                        runOnUiThread {
                            if(r<14) {
                                r++
                            }
                            if(g<227) {
                                g++
                            }
                            if(b>67) {
                                b--
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                bar.progressDrawable.colorFilter=BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.rgb(r,g,b), BlendModeCompat.SRC_ATOP)
                            } else {
                                bar.progressTintList = ColorStateList.valueOf(Color.rgb(r,g,b))
                            }
                        }
                        Thread.sleep(20)
                    }
                }.start()
                val notes = ArrayList<Note>()
                val rootElement = JsonParser.parseString(body).asJsonObject.get("notes").asJsonArray
                for (jsonObject in rootElement) {
                    val noteObject = jsonObject.asJsonObject
                    val note = Note()
                    note.name=noteObject.get("name").asString
                    note.encryption=noteObject.get("encryption").asInt
                    note.description="Web note"
                    note.online=true
                    notes.add(note)
                }
                onlineNotes = notes
            } else {
                Log.d("fetch notes", "Error: $body")
                Thread{
                    for (i in 1..252) {
                        runOnUiThread {
                            bar.progress = 0
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //(3,218,197) -> (255, 0, 0)
                                bar.progressDrawable.colorFilter=BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.rgb( 3+i, if (i>218) 0 else 218-i, if (i>197) 0 else 197-i), BlendModeCompat.SRC_ATOP)
                            } else {
                                bar.progressTintList = ColorStateList.valueOf(Color.rgb( 3+i, if (i>218) 0 else 218-i, if (i>197) 0 else 197-i));
                            }
                        }
                        Thread.sleep(10)
                    }
                }.start()
            }
            finished = true
        }.start()
    }
}