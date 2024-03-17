package org.nwolfhub.notes.model;

public class Note {
    public String id;
    public String name;
    public String content;
    public Long created;
    public Long edited;
    public User owner;
    public SyncState syncState;
    public User me;
    public String serverAddr;


    public Note(String id, String name, String content, Long created, Long edited, User owner, SyncState syncState, User me, String serverAddr) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.created = created;
        this.edited = edited;
        this.owner = owner;
        this.syncState = syncState;
        this.me = me;
        this.serverAddr = serverAddr;
    }

    public Note(String id, String name, String content, User owner) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.owner = owner;
    }

    public Note() {}

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

    public Long getCreated() {
        return created;
    }

    public Note setCreated(Long created) {
        this.created = created;
        return this;
    }

    public Long getEdited() {
        return edited;
    }

    public Note setEdited(Long edited) {
        this.edited = edited;
        return this;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public Note setSyncState(SyncState syncState) {
        this.syncState = syncState;
        return this;
    }

    public User getMe() {
        return me;
    }

    public Note setMe(User me) {
        this.me = me;
        return this;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public Note setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        return this;
    }

    public static enum SyncState {
        local,
        synced
    }
}
