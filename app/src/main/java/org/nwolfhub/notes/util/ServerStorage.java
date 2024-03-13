package org.nwolfhub.notes.util;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.reflect.TypeToken;

import org.nwolfhub.notes.model.ServerInfo;

import java.util.ArrayList;
import java.util.Collections;
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
            Log.d("Servers list", "Raw storage: " + serverListRaw);
            JsonArray array = JsonParser.parseString(serverListRaw).getAsJsonArray();
            Log.d("Servers list", "Rebuilding server list");
            List<ServerInfo> serverInfos = new ArrayList<>();
            for(JsonElement serverElement:array) {
                Log.d("Rebuilding server", serverElement.getAsString());
                serverInfos.add(gson.fromJson(serverElement.getAsString(), ServerInfo.class));
            }
            return serverInfos;
        }
    }

    @Nullable
    public ServerInfo getActiveServer() {
        String serverRaw = preferences.getString("server", null);
        if(serverRaw==null) return null;
        else {
            return gson.fromJson(serverRaw, ServerInfo.class);
        }
    }

    public void selectServer(ServerInfo serverInfo) {
        if(serverInfo==null) preferences.edit().remove("server").apply();
        else preferences.edit().putString("server", gson.toJson(serverInfo)).apply();
    }

    @Nullable
    public ServerInfo getServer(@Nullable String name) {
        if (name==null) return null;
        return getServers().stream().filter(e -> e.name.equals(name)).findAny().orElse(null);
    }

    public void addServer(ServerInfo serverInfo) {
        List<ServerInfo> current = getServers();
        current.add(serverInfo);
        JsonArray array = new JsonArray();
        for(ServerInfo info:current) {
            array.add(gson.toJson(info));
        }
        if(getActiveServer()==null) selectServer(serverInfo);
        preferences.edit().putString("servers", array.toString()).apply();
    }

    public void removeServer(String address) {
        List<ServerInfo> current = getServers();
        JsonArray array = new JsonArray();
        for(ServerInfo info:current) {
            if (!info.address.equals(address)) {
                array.add(gson.toJson(info));
            }
        }
        if(getActiveServer()!=null && getActiveServer().address.equals(address)) {
            selectServer(null);
        }
        preferences.edit().putString("servers", array.toString()).apply();
    }
}
