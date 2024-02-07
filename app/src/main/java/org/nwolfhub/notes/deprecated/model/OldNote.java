package org.nwolfhub.notes.deprecated.model;

import java.io.Serializable;

@Deprecated
public class OldNote implements Serializable {
    public String name;
    public String description;

    public int encryption = 0;
    public boolean online = false;

    public OldNote() {
    }

    public OldNote(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public OldNote(String name, String description, boolean online) {
        this.name = name;
        this.description = description;
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEncryption() {
        return encryption;
    }

    public void setEncryption(int encryption) {
        this.encryption = encryption;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
