package org.nwolfhub.notes.util;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.nwolfhub.notes.model.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerStorage {
    private final SharedPreferences preferences;
    private final Gson gson = new Gson();

    public ServerStorage(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @NonNull
    public List<ServerInfo> getServers() {
        String serverListRaw = preferences.getString("servers", null);
        if(serverListRaw==null) {
            return new ArrayList<>();
        } else {
            List<?> uncheckedList = (List<?>) gson.fromJson(serverListRaw, List.class);
            return uncheckedList.stream().map(e -> {
                try {
                    return (ServerInfo) e;
                } catch (ClassCastException e1) {
                    return new ServerInfo("❤", "Failed to parse server info", "Broken server");
                }
            }).collect(Collectors.toList());
        }
    }

    @Nullable
    public ServerInfo getServer(String name) {
        return getServers().stream().filter(e -> e.name.equals(name)).findAny().orElse(null);
    }

    public void addServer(ServerInfo serverInfo) {
        List<ServerInfo> current = getServers();
        
    }
}
