package org.nwolfhub.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.nwolfhub.model.Note;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Cache {
    private Context context;

    public Cache(Context context) {
        this.context = context;
    }

    public void cacheOnlineNotes(List<Note> notes) {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if(!cacheDir.isDirectory()) {
            cacheDir.mkdirs();
        }
        Log.d("Notes caching", "Caching " + notes.size() + " notes");
        for(Note note:notes) {
            if(note.isOnline()) {
                File noteFile = new File(cacheDir, note.getName() + ".note");
                if (!noteFile.exists()) {
                    try {
                        noteFile.createNewFile();
                        try(ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(noteFile.toPath()))) {
                            stream.writeObject(note);
                        } catch (IOException e) {
                            Log.d("Notes caching", "Failed to cache note: " + note.getName() + " (" + e + ")");
                        }
                    } catch (IOException e) {
                        Log.d("Notes caching", "Failed to cache note: " + note.getName() + " (" + e + ")");
                    }
                }
            }
        }
        Log.d("Notes caching", "Cleaning up deleted online notes");
        List<String> names = notes.stream().map(Note::getName).collect(Collectors.toList());
        for(File noteFile:Objects.requireNonNull(cacheDir.listFiles())) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(noteFile.toPath()))) {
                Note note = (Note) in.readObject();
                if(!names.contains(note.getName())) {
                    noteFile.delete();
                }
            } catch (IOException e) {
                Log.d("Notes caching", "Failed to delete note " + noteFile.getAbsolutePath());
            } catch (ClassNotFoundException e) {
                Log.d("Notes caching", "Broken note on " + noteFile.getAbsolutePath());
            }
        }
        Log.d("Notes caching", "Finished caching notes");
    }

    public List<Note> getCachedNotes() {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if(!cacheDir.isDirectory()) {
            return new ArrayList<>();
        }
        else {
            List<Note> notes = new ArrayList<>();
            for(File noteFile: Objects.requireNonNull(cacheDir.listFiles())) {
                if(noteFile.getName().endsWith(".note")) {
                    try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(noteFile.toPath()))) {
                        Note note = (Note) in.readObject();
                        notes.add(note);
                    } catch (IOException e) {
                        Log.d("Notes caching", "Could not load note " + noteFile.getAbsolutePath());
                    } catch (ClassNotFoundException e) {
                        Log.d("Notes caching", "Broken note at " + noteFile.getAbsolutePath());
                    }
                }
            }
            return notes;
        }
    }

    @Nullable
    public Note getCachedNote(String name) {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if (!cacheDir.isDirectory()) return null;
        File noteFile = new File(cacheDir, name + ".note");
        if (!noteFile.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(noteFile.toPath()))) {
            return (Note) in.readObject();
        } catch (IOException e) {
            Log.d("Notes caching", "Could note read cached note: " + e);
            return null;
        } catch (ClassNotFoundException e) {
            Log.d("Notes caching", "Corrupted note: " + noteFile.getAbsolutePath());
            return null;
        }
    }

    public void cacheNote(Note note) {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if (!cacheDir.isDirectory()) cacheDir.mkdirs();
        File noteFile = new File(cacheDir, note.getName() + ".note");
        if(!noteFile.exists()) try {
            noteFile.createNewFile();
        } catch (IOException e) {
            Log.d("Notes caching", "Failed to cache note " + note.getName() + ": " + e);
            return;
        }
        try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(noteFile.toPath()))) {
            out.writeObject(note);
        } catch (IOException e) {
            Log.d("Notes caching", "Failed to cache note " + note.getName() + ": " + e);
        }
    }
}
