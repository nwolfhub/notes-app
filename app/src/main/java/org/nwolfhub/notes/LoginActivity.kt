package org.nwolfhub.notes

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.util.WebWorker


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val pref = getSharedPreferences("web_updated", MODE_PRIVATE)
        val rawSvInfo = pref.getString("server", null)
        if (rawSvInfo != null) {
            val svInfo = Gson().fromJson(rawSvInfo, ServerInfo::class.java)
            val url = WebWorker().prepareLogin(svInfo)
            val web = findViewById<WebView>(R.id.loginWebView)
            if (url != null) {
                web.loadUrl(url)

            }
        }
    }

    class MyWebViewClient: WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest?
        ): Boolean {
            val url = view.url
            // Log.d("LOG","previous_url: " + url);
            return false
        }
    }
}