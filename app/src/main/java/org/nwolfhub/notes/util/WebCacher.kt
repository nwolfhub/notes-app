package org.nwolfhub.notes.util

import android.util.Log
import okhttp3.internal.cacheGet
import org.nwolfhub.notes.model.Note
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.model.User

class WebCacher(private val serverStorage:ServerStorage, private val storage: NotesStorage,
                private val me: User, private val server:ServerInfo) {
    private val worker = WebWorker()
    fun fetchNotes() {
        try {
            Log.d("Fetch notes", "Begin fetch")
            val rawNotes = worker.getNotes(server,
                serverStorage.getToken(server.address).toString()
            )
            Log.d("Fetch notes", "Got list of notes with length ${rawNotes.size}")
            val rebuilt = mutableListOf<String>()
            for(noteId in rawNotes) {
                val note = fetchNote(noteId)
                storage.addNote(note)
                rebuilt.add(note.id)
                Log.d("Fetch notes", "Fetched note $noteId")
            }
            for(note in storage.getNotes(server.address, me.getId())) {
                if(!rebuilt.contains(note.id)) {
                    if(note.syncState==Note.SyncState.synced) {
                        storage.deleteNote(server.address, me.id, note.id)
                    }
                }
            }
        } catch (e: RuntimeException) {
            if(e.equals("401")) {
                reloadToken()
                fetchNotes()
            } else {
                throw e
            }
        }
    }

    fun fetchNote(id: String): Note {
        try {
            return worker.getNote(id, server, serverStorage.getToken(server.address).toString())
                .setMe(me)
        } catch (e: RuntimeException) {
            if(e.equals("401")) {
                reloadToken()
                return fetchNote(id)
            }
            throw e
        }
    }

    fun processQueue() {
        var element = storage.popNoteFromQueue()
        while (element!=null) {
            try {
                Log.d("Upload notes", "Processing note ${element.id}")
                worker.editNote(server, element.id, element.content, element.name, serverStorage.getToken(server.address).toString())
                element = storage.popNoteFromQueue()
            } catch (e: RuntimeException) {
                if(e.equals("401")) {
                    reloadToken()
                } else {
                    element = storage.popNoteFromQueue()
                }
            }
        }
    }

    fun deleteNote(id: String) {
        val note = storage.getNote(server.address, me.id, id)
        if(note!=null) {
            try {
                worker.deleteNote(server, id, serverStorage.getToken(server.address).toString())
                storage.deleteNote(server.address, me.id, note.id)
            } catch (e: RuntimeException) {
                if(e.equals("401"))  {reloadToken(); deleteNote(id)}
            }
        }
    }

    private fun reloadToken() {
        worker.refreshToken(server.address, serverStorage.getRefreshToken(server.address).toString())
    }
}