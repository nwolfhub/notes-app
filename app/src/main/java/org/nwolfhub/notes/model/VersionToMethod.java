package org.nwolfhub.notes.model;

import java.util.HashMap;

public class VersionToMethod {
    public static HashMap<String, HashMap<String, String>> versions;
    static {
        versions = new HashMap<>();
        HashMap<String, String> v1 = new HashMap<>();
        v1.put("login", "/api/v1/server/login");
        v1.put("create", "/api/v1/notes/create");
        v1.put("edit", "/api/v1/notes/{id}/edit");
        v1.put("delete", "/api/v1/notes/{id}/delete");
        v1.put("search", "/api/v1/users/searchUsers");
        v1.put("getme", "/api/v1/users/getMe");
        v1.put("postlogin", "/api/v1/users/postLogin");
        v1.put("get", "/api/v1/notes/{id}/get");
        v1.put("getnotes", "/api/v1/notes/getNotes");
        versions.put("1", v1);
    }
}
