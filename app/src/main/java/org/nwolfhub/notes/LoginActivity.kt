package org.nwolfhub.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.util.ServerStorage
import org.nwolfhub.notes.util.ServerUtils
import org.nwolfhub.notes.util.WebWorker


class LoginActivity : AppCompatActivity() {
    companion object {
        var context:Context? = null;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        context = this;

        //initialize back button listener
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

        //initialize all the stuff
        val pref = getSharedPreferences("web_updated", MODE_PRIVATE)
        val storage = ServerStorage(getSharedPreferences("web_updated", MODE_PRIVATE))
        val svInfo = storage.activeServer
        Log.d("Active server", svInfo?.address ?: "null")
        //check if any server is active
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
    fun startCircle() {
        val loader:ProgressBar = findViewById(R.id.afterLoginLoader)
        val webview:WebView = findViewById(R.id.loginWebView)
        loader.animate().apply {
            interpolator = LinearInterpolator()
            duration = 500
            alpha(1f)
            start()
        }
        webview.animate().apply {
            interpolator = LinearInterpolator()
            duration = 500
            alpha(0f)
            start()
        }

    }

    override fun finish() {
        context = null
        super.finish()
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
            Log.d("Url change detected", url.toString())
            if(url!!.contains("/postlogin")) {
                val code = url.split("&code=")[1].split("&")[0]
                Log.d("Keycloak code", code)

            }
        }
    }
}