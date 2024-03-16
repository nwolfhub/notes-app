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

    fun createNote(info: ServerInfo, name:String, content:String, token:String):String {
        val response = client
            .newCall(Request.Builder()
                .url(info.address + VersionToMethod.versions[info.version]!!["create"] + "?name=" + URLEncoder.encode(name, Charsets.UTF_8.name()))
                .addHeader("Authorization", "Bearer $token")
                .post(content.toRequestBody())
                .build()).execute();
        Log.d("Server response", "Note creation result: " + (response.body?.string() ?: response.code))
        if(response.isSuccessful) {
            return JsonParser.parseString(response.body!!.string()).asJsonObject.get("id").asString
        } else {
            throw RuntimeException(response.body?.string() ?: response.code.toString())
        }
    }

    fun editNote(info: ServerInfo, id: String, content: String, token: String) {
        val response = client
            .newCall(Request.Builder()
                .url(info.address + VersionToMethod.versions[info.version]!!["create"]!!.replace("{id}", id))
                .addHeader("Authorization", "Bearer $token")
                .post(content.toRequestBody())
                .build()).execute();
        if(!response.isSuccessful) {
            throw RuntimeException(response.body?.string() ?: response.code.toString())
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
            Log.d("Token exchange", "Response body: " + strigified)
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

    fun getMe(server: ServerInfo, token: String): User? {
        val response = client.newCall(Request.Builder()
            .url(server.address + VersionToMethod.versions[server.version]!!["getme"]!!)
            .addHeader("Authorization", "Bearer $token")
            .build())
            .execute()
        if(!response.isSuccessful) {
            if(response.body!=null) {
                Log.d("getMe", "Fail: " + response.body!!.string())
                return null
            } else {
                Log.d("getMe", "Fail: " + response.code)
            }
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

    fun refreshToken(url:String, refresh: String):String? {
        Log.d("Token refresh", "Original: " + url + ", replaced: " + url.replace("/auth", "/token"))
        val response = client.newCall(Request.Builder()
            .url(url.replace("/auth", "/token"))
            .post(FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("client_id", "notes")
                .add("refresh_token", refresh)
                .build()).build()).execute()
        Log.d("Token refresh", "Server responded with code " + response.code)
        if(response.body!=null) {
            val strigified = response.body!!.string()
            Log.d("Token refresh", "Response body: " + strigified)
            if(response.isSuccessful) {
                return strigified;
            }
        }
        return null
    }
}