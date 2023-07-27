package org.nwolfhub.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.ProgressBar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.nwolfhub.Notes
import org.nwolfhub.R
import org.nwolfhub.model.Note

class WebCacher(val cache: Cache) {

    fun getCachedNotes():List<Note> {
        synchronized(cache) {
            return cache.cachedNotes
        }
    }

    fun getCachedNote(name:String):Note? {
        synchronized(cache) {
            return cache.getCachedNote(name)
        }
    }

    fun runUpdateNotes(server:String, token:String, context: Notes, bar:ProgressBar) {
        context.runOnUiThread {
            bar.progress=2
        }
        val client = OkHttpClient()
        val response = client.newCall(
            Request.Builder().url("$server/api/notes/getAll").addHeader("token", token).build()
        ).execute()
        val code = response.code
        val body = response.body?.string()
        response.close()
        if (code != 200) {
            Log.d("Notes caching", "Failed to obtain online notes: $body")
            return
        } else {
            context.runOnUiThread {
                bar.progress = 3
            }
            Thread {
                var r = 3
                var g = 218
                var b = 197
                for (i in 1..255) { //(3,218,197) -> (14, 227, 67)
                    context.runOnUiThread {
                        if (r < 14) {
                            r++
                        }
                        if (g < 227) {
                            g++
                        }
                        if (b > 67) {
                            b--
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            bar.progressDrawable.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    Color.rgb(
                                        r,
                                        g,
                                        b
                                    ), BlendModeCompat.SRC_ATOP
                                )
                        } else {
                            bar.progressTintList = ColorStateList.valueOf(Color.rgb(r, g, b))
                        }
                    }
                    Thread.sleep(20)
                }
            }.start()
            val notes = JsonParser.parseString(body).asJsonObject.get("notes").asJsonArray
            val notesList = ArrayList<Note>()
            for (note in notes) {
                val finalNote = Note(note.asJsonObject.get("name").asString, "{ONLINECACHE}")
                finalNote.online = true
                notesList.add(finalNote)
            }
            cache.cacheOnlineNotes(notesList)
        }
    }

    fun updateNote(note:Note) {
        cache.cacheNote(note)
    }
}