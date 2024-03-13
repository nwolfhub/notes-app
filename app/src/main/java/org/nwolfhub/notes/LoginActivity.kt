package org.nwolfhub.notes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.util.ServerStorage
import org.nwolfhub.notes.util.ServerUtils
import org.nwolfhub.notes.util.WebWorker


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@LoginActivity)
                    .setTitle("Pick action")
                    .setPositiveButton("Exit") { _, _, ->
                        run {
                            this@LoginActivity.finish()
                        }
                    }
                    .setNegativeButton("Server select") { _, _, ->
                        run {
                            startActivity(Intent(this@LoginActivity, ServerSelect::class.java))
                            this@LoginActivity.finish()
                        }
                    }.show()
            }
        })
        val pref = getSharedPreferences("web_updated", MODE_PRIVATE)
        val storage = ServerStorage(getSharedPreferences("web_updated", MODE_PRIVATE))
        val svInfo = storage.activeServer
        Log.d("Active server", svInfo?.address ?: "null")
        if (svInfo == null) {
            startActivity(Intent(this, ServerSelect::class.java))
            finish()
        } else {
            val codes = ServerUtils().prepareCodes()
            Thread {
                val url =
                    WebWorker().prepareLogin(svInfo) + "?response_type=code&client_id=notes&code_challenge_method=S256&code_challenge=" + codes[0]
                runOnUiThread {
                    val web = findViewById<WebView>(R.id.loginWebView)
                    web.webViewClient = MyWebViewClient(verifier = codes[1])
                    web.loadUrl(url)
                }
            }.start()
        }
    }

    class MyWebViewClient(val verifier: String) : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest?
        ): Boolean {
            val url = view.url
            Log.d("Previous url", "$url")
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if(url!!.contains("/postlogin")) {
                val code = url.split("&code=")[1].split("&")[0]
                Log.d("Keycloak code", code)
            }
        }
    }
}