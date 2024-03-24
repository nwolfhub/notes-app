package org.nwolfhub.notes.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.userAgent
import org.nwolfhub.notes.model.Note
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.model.User
import org.nwolfhub.notes.model.VersionToMethod
import java.net.URLEncoder

class WebWorker() {
    private val client:OkHttpClient = OkHttpClient()

    fun prepareLogin(info: ServerInfo): String? {
        val response = client.newCall(Request.Builder().url(info.address + VersionToMethod.versions[info.version]!!["login"]).build()).execute()
        return if(response.isSuccessful) {
            JsonParser.parseString(response.body!!.string()).asJsonObject.get("url").asString
        } else null
    }

    fun createNote(info: ServerInfo, content:String, token:String):String {
        val response = client
            .newCall(Request.Builder()
                .url(info.address + VersionToMethod.versions[info.version]!!["create"] + "?name")
                .addHeader("Authorization", "Bearer $token")
                .post(content.toRequestBody())
                .build()).execute();
        var body:String? = null
        if(response.body!=null) {
            body = response.body?.string()
        }
        Log.d("Server response", "Note creation result: " + ( body?:response.code))
        if(response.isSuccessful) {
            return JsonParser.parseString(body).asJsonObject.get("id").asString
        } else {
            if(body!=null) {
                Log.e("Note create", body)
            }
            throw RuntimeException(response.code.toString())
        }
    }

    fun editNote(info: ServerInfo, id: String, content: String, name: String, token: String) {
        val response = client
            .newCall(Request.Builder()
                .url(info.address + VersionToMethod.versions[info.version]!!["edit"]!!.replace("{id}", id) + "?name=" + URLEncoder.encode(name, Charsets.UTF_8.name()))
                .addHeader("Authorization", "Bearer $token")
                .post(content.toRequestBody())
                .build()).execute();
        if(!response.isSuccessful) {
            if(response.body!=null) {
                Log.e("Note edit", response.body!!.string())
            }
            throw RuntimeException(response.code.toString())
        }
    }

    fun getNotes(server: ServerInfo, token: String): List<String> {
        val response = client.newCall(Request.Builder()
            .url(server.address + VersionToMethod.versions[server.version]!!["getnotes"])
            .addHeader("Authorization", "Bearer $token")
            .build()).execute()
        if(!response.isSuccessful) {
            Log.e("Notes fetch", "Server responded with code: ${response.code}")
            if(response.body!=null) {
                Log.e("Notes fetch", response.body!!.string())
            }
            throw RuntimeException(response.code.toString())
        }
        try {
            val list = mutableListOf<String>()
            val body = response.body!!.string()
            val arr = JsonParser.parseString(body).asJsonObject.get("notes").asJsonArray
            for(e in arr) {
                val noteObj = e.asJsonObject
                list.add(noteObj.get("id").asString)
            }
            return list
        } catch (e: NullPointerException) {
            Log.e("Notes fetch", e.toString())
            throw RuntimeException("Wrong response")
        }
    }

    fun getNote(id: String, server: ServerInfo, token: String): Note {
        val response = client.newCall(Request.Builder()
            .url(server.address + VersionToMethod.versions[server.version]!!["get"]!!.replace("{id}", id))
            .addHeader("Authorization", "Bearer $token")
            .build()).execute()
        if(!response.isSuccessful) {
            Log.e("Note fetch", "Server responded with code: ${response.code}")
            if(response.body!=null) {
                Log.e("Note fetch", response.body!!.string())
            }
            throw RuntimeException(response.code.toString())
        } else {
            try {
                val note = Note()
                val body = response.body!!.string()
                Log.d("Note fetch", "Server response: $body")
                val obj = JsonParser.parseString(body).asJsonObject
                note
                    .setId(obj.get("id").asString)
                    .setContent(obj.get("content").asString)
                    .setName(obj.get("name").asString)
                    .setCreated(obj.get("created").asLong)
                    .setEdited(obj.get("edited").asLong)
                    .setServerAddr(server.address)
                    .setOwner(User()
                        .setId(obj.get("owner").asJsonObject.get("id").asString)
                        .setFirstname(obj.get("owner").asJsonObject.get("name").asString)
                        .setUsername(obj.get("owner").asJsonObject.get("username").asString))
                    .setSyncState(Note.SyncState.synced)
                return note
            } catch (e: NullPointerException) {
                Log.e("Notes fetch", e.toString())
                throw RuntimeException("Wrong response")
            }
        }
    }

    fun getToken(url:String, code:String, verifier:String):String? {
        Log.d("Token exchange", "Original: " + url + ", replaced: " + url.replace("/auth", "/token"))
        val response = client.newCall(Request.Builder()
            .url(url.replace("/auth", "/token"))
            .post(FormBody.Builder()
                .add("code", code)
                .add("code_verifier", verifier)
                .add("grant_type", "authorization_code")
                .add("client_id", "notes")
                .build()).build()).execute()
        Log.d("Token exchange", "Server responded with code " + response.code)
        if(response.body!=null) {
            val strigified = response.body!!.string()
            Log.d("Token exchange", "Response body: $strigified")
            if(response.isSuccessful) {
                return strigified;
            }
        }
        return null
    }

    fun postLogin(server: ServerInfo, token: String) {
        Thread {
            val response = client.newCall(Request.Builder()
                .url(server.address + VersionToMethod.versions[server.version]!!["postlogin"]!!)
                .addHeader("Authorization", "Bearer $token")
                .build())
                .execute()
            Log.d("PostLogin result", response.code.toString())
        }.start()
    }

    fun getMe(server: ServerInfo, token: String): User {
        val response = client.newCall(Request.Builder()
            .url(server.address + VersionToMethod.versions[server.version]!!["getme"]!!)
            .addHeader("Authorization", "Bearer $token")
            .build())
            .execute()
        if(!response.isSuccessful) {
            if(response.body!=null) {
                Log.d("getMe", "Fail: " + response.body!!.string())
            } else {
                Log.d("getMe", "Fail: " + response.code)
            }
            throw RuntimeException(response.code.toString())
        }
        val body = response.body!!.string()
        Log.d("getMe", body)
        val obj = JsonParser.parseString(body).asJsonObject
        val user = User()
        user
            .setId(obj.get("id").asString)
            .setUsername(obj.get("username").asString)
            .setFirstname(obj.get("name").asString)
        return user
    }

    fun refreshToken(url:String, refresh: String):String {
        Log.d("Token refresh", "Original: " + url + ", replaced: " + url.replace("/auth", "/token"))
        val response = client.newCall(Request.Builder()
            .url(url.replace("/auth", "/token"))
            .post(FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("client_id", "notes")
                .add("refresh_token", refresh)
                .build()).build()).execute()
        Log.d("Token refresh", "Server responded with code " + response.code)
        if(!response.isSuccessful) {
            throw RuntimeException(response.code.toString())
        }
        if(response.body!=null) {
            val strigified = response.body!!.string()
            Log.d("Token refresh", "Response body: $strigified")
            if(response.isSuccessful) {
                return strigified;
            }
        }
        return "" //should be unreachable all the times
    }

    fun deleteNote(server: ServerInfo, id: String, token: String) {
        Log.d("Delete note", "Delete requested for note $id")
        val response = client.newCall(Request.Builder()
            .url(server.address + VersionToMethod.versions[server.version]!!["delete"]!!.replace("{id}", id))
            .addHeader("Authorization", "Bearer $token")
            .build()).execute()
        Log.d("Delete note", "Server responded with code ${response.code}")
        if(!response.isSuccessful) {
            throw RuntimeException(response.code.toString())
        }
    }

    fun refreshAndPut(storage: ServerStorage):JsonObject {
        val refreshResult = refreshToken(prepareLogin(storage.activeServer!!)!!, storage.getRefreshToken(storage.activeServer!!.address)!!)
        val obj = JsonParser.parseString(refreshResult).asJsonObject
        storage.setTokens(storage.activeServer!!.address, obj)
        return obj
    }
}