package org.nwolfhub.util

import android.util.Log
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
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

    fun runUpdateNotes(server:String, token:String) {
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
        }
        val notes = JsonParser.parseString(body).asJsonObject.get("notes").asJsonArray
        val notesList = ArrayList<Note>()
        for (note in notes) {
            val finalNote = Note(note.asJsonObject.get("name").asString, "{ONLINECACHE}")
            finalNote.online=true
            notesList.add(finalNote)
        }
        cache.cacheOnlineNotes(notesList)
    }

    fun updateNote(note:Note) {
        cache.cacheNote(note)
    }
}