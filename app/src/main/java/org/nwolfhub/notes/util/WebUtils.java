package org.nwolfhub.notes.util;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebUtils {
    @Nullable
    public static String getMe(String token, String server) throws IOException {
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(new Request.Builder().url(server + "/api/users/getMe").get().addHeader("token", token).build()).execute();
            int code = response.code();
            String body = response.body().string();
            response.close();
            Log.d("WebUtils", body);
            if(code==200) {
                try {
                    String username = JsonParser.parseString(body).getAsJsonObject().get("user").getAsString();
                    return username;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {return null;}
    }

    public static boolean checkAuth(String token, String server) {
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(new Request.Builder().url(server + "/api/users/checkAuth").get().addHeader("token", token).build()).execute();
            int code = response.code();
            String body = response.body() != null ? response.body().string() : null;
            response.close();
            if(body!=null && code != 200) {
                Log.d("Check auth result", body);
            }
            return code == 200;
        } catch (Exception ignored) {
            return false;
        }
    }
}
