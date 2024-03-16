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

import org.nwolfhub.notes.model.ServerInfo;

import java.util.ArrayList;
import java.util.List;

public class ServerStorage {

    /**
     * Servers data is stored inside web_updated SharedPreferences
     * Servers list is stored in servers as JsonArray of json-formatted objects.
     * All tokens (including refresh tokens) are stored inside *server url*+tkn and *server url* + fresh
     */
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

    public ServerInfo getServerByAddress(String address) {
        if (address==null) return null;
        return getServers().stream().filter(e -> e.address.equals(address)).findAny().orElse(null);
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

    public void setTokens(String server, String token, String refreshToken) {
        preferences.edit()
                .putString(server + "tkn", token)
                .putString(server + "fresj", refreshToken)
                .apply();
    }
    public void setTokens(String server, JsonObject obj) {
        setTokens(server, obj.get("access_token").getAsString(), obj.get("refresh_token").getAsString());
        new WebWorker().postLogin(getActiveServer(), obj.get("access_token").getAsString());
    }

    public void clearTokens(String server) {
        preferences.edit()
                .remove(server + "tkn")
                .remove(server + "fresh")
                .apply();
    }

    @Nullable
    public String getToken(String server) {
        return preferences.getString(server + "tkn", null);
    }

    @Nullable
    public String getRefreshToken(String server) {
        return preferences.getString(server + "fresh", null);
    }
}
