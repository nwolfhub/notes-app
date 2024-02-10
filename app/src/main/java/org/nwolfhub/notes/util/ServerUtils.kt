package org.nwolfhub.notes.util

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.text.StringEscapeUtils
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.utils.Utils
import java.io.IOException
import java.security.MessageDigest
import java.util.Base64


class ServerUtils {
    private val client:OkHttpClient = OkHttpClient()
    private val gson = Gson()
    fun refreshToken(pref: SharedPreferences) {
        val active = gson.fromJson(pref.getString("active_server", null), ServerInfo::class.java)
        val last = pref.getString("refresh", null)
        val codes = prepareCodes()
        val body = FormBody.Builder().add("grant_type", "refresh_token")
            .add("refresh_token", last!!)
            .add("client_id", "notes")
            .build()
        val response = client.newCall(Request.Builder().url(
            WebWorker().prepareLogin(active)!!.replace("/auth", "/token"))
            .post(body)
            .build()).execute()
        if(!response.isSuccessful) {
            if(response.code==400) {
                throw RuntimeException("Relogin")
            }
            throw IOException("Failed to contact server: " + (response.body?.string() ?: response.code))
        } else {
            val obj = JsonParser.parseString(response.body!!.string()).asJsonObject
            pref.edit()
                .putString("token", obj.get("access_token").asString)
                .putString("refresh", obj.get("refresh_token").asString)
                .apply()
        }
    }

    fun prepareCodes():List<String> {
        val encoder: Base64.Encoder = Base64.getEncoder().withoutPadding()
        val inp: String =
            encoder.encodeToString(Utils.generateString(43).toByteArray()).replace("+", "-")
                .replace("=", "").replace("/", "_")
        val digest = MessageDigest.getInstance("SHA-256")
        var res: String = encoder.encodeToString(digest.digest(inp.toByteArray())).replace("+", "-")
            .replace("=", "").replace("/", "_")
        res = StringEscapeUtils.escapeHtml3(res);
        Log.d("S256 gen", "Obtained pair: $res to verifier $inp")
        return listOf(res, inp)
    }
}