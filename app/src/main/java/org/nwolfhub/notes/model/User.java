package org.nwolfhub.notes.model;

public class User {
    public String id;
    public String username;
    public String firstname;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public User setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public User() {}

    public User(String id, String username, String firstname) {
        this.id = id;
        this.username = username;
        this.firstname = firstname;
    }
}
