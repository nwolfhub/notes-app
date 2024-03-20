package org.nwolfhub.notes.util;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.nwolfhub.notes.model.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class NotesStorage {
    private SharedPreferences notesPref;
    private SharedPreferences syncPref;
    private Gson gson;

    public NotesStorage(SharedPreferences preferences, SharedPreferences sync) {
        this.notesPref = preferences;
        this.syncPref = sync;
        this.gson = new Gson();
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        for(String record: notesPref.getAll().values().stream().map(Object::toString).collect(Collectors.toList())) {
            try {
                notes.add(gson.fromJson(record, Note.class));
            } catch (Exception e) {
                Log.w("Note cache parsing", "Failed to parse note: " + e);
            }
        }
        return notes;
    }
    public List<Note> getNotes(String server, String me) {
        return getNotes().stream().filter(e -> e.getServerAddr().equals(server)&&e.getMe().getId().equals(me)).collect(Collectors.toList());
    }
    @Nullable
    public Note getNote(String server, String me, String id) {
        List<Note> notes = getNotes();
        return notes.stream().filter(e -> e.id.equals(id) && e.serverAddr.equals(server) && e.getMe().getId().equals(me)).findFirst().orElse(null);
    }

    public void addNote(@NonNull Note note) {
        Note prevNote = getNote(note.serverAddr, note.me.id, note.id);
        if(prevNote!=null) {
            if(prevNote.syncState==Note.SyncState.local) {
                if(prevNote.edited>note.edited) {
                    addNoteToSyncQueue(note);
                }
            } else {
                notesPref.edit().putString(note.serverAddr + note.getMe().getId() + note.getId(), gson.toJson(note)).apply();
            }
        } else {
            notesPref.edit().putString(note.serverAddr + note.getMe().getId() + note.getId(), gson.toJson(note)).apply();
        }
    }

    public void addNoteToSyncQueue(@NonNull Note note) {
        syncPref.edit().putString(note.getServerAddr() + note.getMe().getId() + note.getId(), gson.toJson(note)).apply();
    }
    public Note popNoteFromQueue() {
        try {
            String key = syncPref.getAll().entrySet().iterator().next().getKey();
            String value = syncPref.getString(key, null);
            syncPref.edit().remove(key).apply();
            return gson.fromJson(value, Note.class);
        } catch (NullPointerException| NoSuchElementException e) {
            return null;
        }
    }

    public void deleteNote(String server, String me, String id) {
        notesPref.edit().remove(server + me + id).apply();
    }

}
