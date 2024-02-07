package org.nwolfhub.notes.deprecated.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.nwolfhub.notes.deprecated.model.OldNote;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Deprecated
public class Cache {
    private Context context;

    public Cache(Context context) {
        this.context = context;
    }

    public void cacheOnlineNotes(List<OldNote> notes) {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if(!cacheDir.isDirectory()) {
            cacheDir.mkdirs();
        }
        Log.d("Notes caching", "Caching " + notes.size() + " notes");
        for(OldNote note:notes) {
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
        List<String> names = notes.stream().map(OldNote::getName).collect(Collectors.toList());
        for(File noteFile:Objects.requireNonNull(cacheDir.listFiles())) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(noteFile.toPath()))) {
                OldNote note = (OldNote) in.readObject();
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

    public List<OldNote> getCachedNotes() {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if(!cacheDir.isDirectory()) {
            return new ArrayList<>();
        }
        else {
            List<OldNote> notes = new ArrayList<>();
            for(File noteFile: Objects.requireNonNull(cacheDir.listFiles())) {
                if(noteFile.getName().endsWith(".note")) {
                    try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(noteFile.toPath()))) {
                        OldNote note = (OldNote) in.readObject();
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
    public OldNote getCachedNote(String name) {
        File cacheDir = new File(context.getCacheDir(), "notes");
        if (!cacheDir.isDirectory()) return null;
        File noteFile = new File(cacheDir, name + ".note");
        if (!noteFile.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(noteFile.toPath()))) {
            return (OldNote) in.readObject();
        } catch (IOException e) {
            Log.d("Notes caching", "Could note read cached note: " + e);
            return null;
        } catch (ClassNotFoundException e) {
            Log.d("Notes caching", "Corrupted note: " + noteFile.getAbsolutePath());
            return null;
        }
    }

    public void cacheNote(OldNote note) {
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
