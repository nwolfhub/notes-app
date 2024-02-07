package org.nwolfhub.notes.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.model.VersionToMethod
import java.net.URLEncoder

class WebWorker(context: Context) {
    private val client:OkHttpClient = OkHttpClient()

    fun readServer(url: String): ServerInfo {
        val response = client.newCall(Request.Builder().url("$url/api/v1/server/info").get().build()).execute()
        return if(response.isSuccessful) {
            val respObject = JsonParser.parseString(response.body!!.string()).asJsonObject
            val serverInfo = ServerInfo(
                respObject.get("api_version").asString,
                url,
                respObject.get("name").asString
            )
            serverInfo
        } else {
            ServerInfo(
                "legacy",
                url,
                "Legacy server"
            )
        }
    }

    fun prepareLogin(info: ServerInfo): String? {
        val response = client.newCall(Request.Builder().url(info.address + VersionToMethod.versions[info.version]!!["create"]).build()).execute()
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
}