package org.nwolfhub.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import org.nwolfhub.notes.deprecated.WebLogin
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.util.ServerStorage
import org.nwolfhub.notes.util.ServerUtils
import org.nwolfhub.notes.util.WebWorker
import java.util.concurrent.LinkedBlockingQueue


class LoginActivity : AppCompatActivity() {
    companion object {
        var context:Context? = null;
    }
    lateinit var svInfo:ServerInfo
    lateinit var storage: ServerStorage
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
        storage = ServerStorage(getSharedPreferences("web_updated", MODE_PRIVATE))
        val timedSv = storage.activeServer;
        Log.d("Active server", timedSv?.address ?: "null")
        //check if any server is active
        if (timedSv == null) {
            startActivity(Intent(this, ServerSelect::class.java))
            finish()
        } else if(!timedSv.version.equals("legacy")){
            svInfo=timedSv
            val cont = checkLogin()
            if(cont) {
                val codes = ServerUtils().prepareCodes()
                Thread {
                    val url =
                        WebWorker().prepareLogin(svInfo) + "?response_type=code&client_id=notes&code_challenge_method=S256&code_challenge=" + codes[0]
                    runOnUiThread {
                        Log.d("Codes", codes.toString())
                        val web = findViewById<WebView>(R.id.loginWebView)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);
                        web.getSettings().domStorageEnabled = true;
                        web.webViewClient = MyWebViewClient(verifier = codes[1])
                        web.loadUrl(url)
                    }
                }.start()
            }
        } else {
            val webPref = getSharedPreferences("servers", MODE_PRIVATE)
            webPref.edit().putString("servers", "Your server").putString("Your server", timedSv.address).commit()
            startActivity(Intent(this, WebLogin::class.java))
            finish()
        }


    }

    fun checkLogin():Boolean {
        val token = storage.getToken(svInfo.address)
        if(token!=null) {
            startCircle()
            Thread {
                val worker = WebWorker()
                try {
                    worker.getMe(svInfo, token)
                    runOnUiThread {
                        startActivity(Intent(this, Notes::class.java))
                        finish()
                    }
                } catch (e: RuntimeException) {
                    try {
                        val result = worker.refreshAndPut(storage)
                        if (result.has("access_token")) { //always true. Forces this shit not to run
                            runOnUiThread {
                                startActivity(Intent(this, Notes::class.java))
                                finish()
                            }
                        }
                    } catch (e: RuntimeException) {
                        storage.clearTokens(svInfo.address)
                        runOnUiThread {
                            startActivity(Intent(this, Notes::class.java))
                            finish()
                        }
                    }
                }
            }.start()
        }
        return token==null
    }
    fun startCircle() {
        val loader:ProgressBar = findViewById(R.id.afterLoginLoader)
        val webview:WebView = findViewById(R.id.loginWebView)
        webview.isEnabled=false;
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

    fun getToken(code:String, verifier: String) {
        Thread {
            val token = WebWorker().getToken(WebWorker().prepareLogin(svInfo).toString(), code, verifier)
            if(token==null) {
                Toast.makeText(this, "Failed to obtain token", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val obj = JsonParser.parseString(token).asJsonObject
                storage.setTokens(svInfo.address, obj.get("access_token").asString, obj.get("refresh_token").asString)
                WebWorker().postLogin(storage.activeServer!!, obj.get("access_token").asString)
                startActivity(Intent(this, Notes::class.java))
                finish()
            }
        }.start()
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
                (context as LoginActivity).startCircle()
                (context as LoginActivity).getToken(code,verifier)
            }
        }
    }
}