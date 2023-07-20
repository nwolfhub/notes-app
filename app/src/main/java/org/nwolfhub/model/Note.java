package org.nwolfhub.model;

public class Note {
    public String name;
    public String description;

    public int encryption = 0;
    public boolean online = false;

    public Note() {
    }

    public Note(String name, String description) {
        this.name = name;
        this.description = description;
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
