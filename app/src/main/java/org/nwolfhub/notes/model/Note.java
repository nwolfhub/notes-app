package org.nwolfhub.notes.model;

public class Note {
    public String id;
    public String name;
    public String content;
    public User owner;

    public Note(String id, String name, String content, User owner) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.owner = owner;
    }

    public Note() {
    }

    public String getId() {
        return id;
    }

    public Note setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Note setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Note setContent(String content) {
        this.content = content;
        return this;
    }

    public User getOwner() {
        return owner;
    }

    public Note setOwner(User owner) {
        this.owner = owner;
        return this;
    }
}
