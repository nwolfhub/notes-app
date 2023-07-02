package org.nwolfhub

import android.R.attr.data
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception
import java.util.Random
import java.util.stream.Collectors
import org.nwolfhub.utils.*
import java.lang.NullPointerException


class WebLogin : AppCompatActivity() {

    private fun animate(button:Button) {
        var prevX = 10f
        var prevY = 10f
        val random = Random()
        while (true) {
            runOnUiThread {
                button.setShadowLayer(25f, if (prevX>50f) prevX-1 else prevX + random.nextInt(10)-3, if (prevY>50) prevY-1 else prevY+random.nextInt(10)-3, Color.valueOf(random.nextInt(17-13+1)+13f, random.nextInt(70-30+1)+30f, random.nextInt(200-140+1)+140f).toArgb())
            }
            Thread.sleep(50)
        }
    }

    private fun playAnimation(view1:ConstraintLayout, view2:ConstraintLayout) {
        view2.visibility=View.VISIBLE
        view1.rotationX=0f
        view2.rotationX=-10f
        view1.rotationY=0f
        view2.rotationY=-30f
        view1.translationX=0f
        view2.translationX=0f
        view1.translationY=0f
        view2.translationY=0f
        val objectAnimator = ObjectAnimator(); objectAnimator.setPropertyName("rotationX"); objectAnimator.target=view1;objectAnimator.setFloatValues(0f, 30f);objectAnimator.setDuration(800).start()
        val objectAnimator2 = ObjectAnimator(); objectAnimator2.setPropertyName("rotationY"); objectAnimator2.target=view1;objectAnimator2.setFloatValues(0f, 20f);objectAnimator2.setDuration(800).start()
        val objectAnimator1 = ObjectAnimator(); objectAnimator1.setPropertyName("rotationX"); objectAnimator1.target=view2;objectAnimator1.setFloatValues(-20f, 0f);objectAnimator1.setDuration(800).start()
        val objectAnimator12 = ObjectAnimator(); objectAnimator12.setPropertyName("rotationY"); objectAnimator12.target=view2;objectAnimator12.setFloatValues(-30f, 0f);objectAnimator12.setDuration(800).start()
        val objectAnimator3 = ObjectAnimator(); objectAnimator3.setPropertyName("alpha"); objectAnimator3.target=view1;objectAnimator3.setFloatValues(1f, 0f);objectAnimator3.setDuration(800).start()
        val objectAnimator4 = ObjectAnimator(); objectAnimator4.setPropertyName("alpha"); objectAnimator4.target=view2;objectAnimator4.setFloatValues(0f, 1f);objectAnimator4.setDuration(800).start()
        val objectAnimator5 = ObjectAnimator(); objectAnimator5.setPropertyName("translationX"); objectAnimator5.target=view1;objectAnimator5.setFloatValues(0f, 300f);objectAnimator5.setDuration(800).start()
        val objectAnimator6 = ObjectAnimator(); objectAnimator6.setPropertyName("translationY"); objectAnimator6.target=view1;objectAnimator6.setFloatValues(0f, -300f);objectAnimator6.setDuration(800).start()
        Handler(Looper.getMainLooper()).postDelayed({
            view1.visibility=View.GONE
        }, 800)
    }

    private fun disableAll(layout: ConstraintLayout) {
        for (i in layout.children) {
            if(i is ConstraintLayout) {
                disableAll(i)
            }
            if(i !is TextView) {
                i.isEnabled = false
            }
        }
    }

    private fun login(username:String, password:String, server:String, buttons:List<Button>, client: OkHttpClient) {
        val welcomeToWeb: TextView = findViewById(R.id.welcomeToWeb)
        val progressBar: ProgressBar = findViewById(R.id.loginBar)
        Log.d("web login", "Attempting to login on $server")
        val action:TextAction = object: TextAction() {
            override fun applyText(text: String?) {
                runOnUiThread {
                    welcomeToWeb.text=text
                }
            }
        }
        for(button in buttons) {
            button.isEnabled=false
        }
        Thread {
            runOnUiThread {
                ObjectAnimator.ofFloat(welcomeToWeb, "alpha", 0f).setDuration(100).start()
                ObjectAnimator.ofFloat(progressBar, "alpha", 1f).setDuration(100).start()
            }
            try {
                val response = client.newCall(
                    Request.Builder().url("$server/api/users/login")
                        .post("username=$username\npassword=$password".toRequestBody()).build()
                ).execute()
                Log.d("web login", "Obtained code " + response.code)
                if(response.code==200) {
                    runOnUiThread {
                        ObjectAnimator.ofFloat(welcomeToWeb, "alpha", 1f).setDuration(100).start()
                        ObjectAnimator.ofFloat(progressBar, "alpha", 0f).setDuration(100).start()
                    }
                    runOnUiThread {
                        disableAll(findViewById(R.id.webLoginMainView))
                    }
                    Utils.typeText(welcomeToWeb.text.toString(), true, "", "Welcome to web!", 50, 30, 0, action)
                    Thread.sleep(3000)
                    val token = JsonParser.parseString(response.body!!.string()).asJsonObject.get("token").asString
                    Log.d("web login","Obtained token $token")
                    response.close()
                    runOnUiThread {
                        val preferences = getSharedPreferences("web", MODE_PRIVATE)
                        preferences.edit().putString("token", token).apply()
                        preferences.edit().putString("server", server).apply()
                        startActivity(Intent(this, Notes::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        ObjectAnimator.ofFloat(welcomeToWeb, "alpha", 1f).setDuration(100).start()
                        ObjectAnimator.ofFloat(progressBar, "alpha", 0f).setDuration(100).start()
                    }
                    var error = "wrong server response"
                    try {
                         error=JsonParser.parseString(response.body!!.string()).asJsonObject.get("error").asString
                    } catch (_:NullPointerException) {}
                    Utils.typeText(welcomeToWeb.text.toString(), true, "", "Failed to login: $error", 50, 40, 0, action)
                }
            } catch (e:NullPointerException) {
                runOnUiThread {
                    Toast.makeText(this, "Error while executing request: $e", Toast.LENGTH_LONG).show()
                    welcomeToWeb.alpha=1f
                    progressBar.alpha=0f
                    welcomeToWeb.text=e.toString()
                }
            }
            runOnUiThread {
                for (button in buttons) {
                    button.isEnabled = true
                }
            }
        }.start()
    }

    private fun register(username:String, password:String, server:String, buttons:List<Button>, client: OkHttpClient) {
        val welcomeToWeb: TextView = findViewById(R.id.welcomeToWeb)
        val progressBar: ProgressBar = findViewById(R.id.loginBar)
        Log.d("web login", "Attempting to register on $server")
        val action:TextAction = object: TextAction() {
            override fun applyText(text: String?) {
                runOnUiThread {
                    welcomeToWeb.text=text
                }
            }
        }
        for(button in buttons) {
            button.isEnabled=false
        }
        Thread {
            runOnUiThread {
                ObjectAnimator.ofFloat(welcomeToWeb, "alpha", 0f).setDuration(100).start()
                ObjectAnimator.ofFloat(progressBar, "alpha", 1f).setDuration(100).start()
            }
            try {
                val response = client.newCall(
                    Request.Builder().url("$server/api/users/register")
                        .post("username=$username\npassword=$password".toRequestBody()).build()
                ).execute()
                Log.d("web login", "Obtained code " + response.code)
                if(response.code==200) {
                    runOnUiThread {
                        ObjectAnimator.ofFloat(welcomeToWeb, "alpha", 1f).setDuration(100).start()
                        ObjectAnimator.ofFloat(progressBar, "alpha", 0f).setDuration(100).start()
                    }
                    runOnUiThread {
                        disableAll(findViewById(R.id.webLoginMainView))
                    }
                    Utils.typeText(welcomeToWeb.text.toString(), true, "", "Thanks for registering, welcome to web!", 50, 30, 0, action)
                    Thread.sleep(3000)
                    val token = JsonParser.parseString(response.body!!.string()).asJsonObject.get("token").asString
                    Log.d("web login","Obtained token $token")
                    response.close()
                    runOnUiThread {
                        val preferences = getSharedPreferences("web", MODE_PRIVATE)
                        preferences.edit().putString("token", token).apply()
                        preferences.edit().putString("server", server).apply()
                        startActivity(Intent(this, Notes::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread {
                        ObjectAnimator.ofFloat(welcomeToWeb, "alpha", 1f).setDuration(100).start()
                        ObjectAnimator.ofFloat(progressBar, "alpha", 0f).setDuration(100).start()
                    }
                    var error = "wrong server response"
                    try {
                        error=JsonParser.parseString(response.body!!.string()).asJsonObject.get("error").asString
                    } catch (_:NullPointerException) {}
                    Utils.typeText(welcomeToWeb.text.toString(), true, "", "Failed to login: $error", 50, 40, 0, action)
                }
            } catch (e:NullPointerException) {
                runOnUiThread {
                    Toast.makeText(this, "Error while executing request: $e", Toast.LENGTH_LONG).show()
                    welcomeToWeb.alpha=1f
                    progressBar.alpha=0f
                    welcomeToWeb.text=e.toString()
                }
            }
            runOnUiThread {
                for (button in buttons) {
                    button.isEnabled = true
                }
            }
        }.start()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)
        val registerButton:Button = findViewById(R.id.selectorRegister)
        val loginButton:Button = findViewById(R.id.selectorLogin)
        val selector:ConstraintLayout = findViewById(R.id.loginSelector)
        val secondLayout:ConstraintLayout = findViewById(R.id.loginLayout)
        val returnButton:Button = findViewById(R.id.cancelLogin)
        val proceedLogin:Button = findViewById(R.id.continueLogin)
        val username:EditText = findViewById(R.id.usernameLogin)
        val password:EditText = findViewById(R.id.passwordLogin)
        val client = OkHttpClient()
        val spinner:Spinner = findViewById(R.id.serverSelector)
        secondLayout.alpha=0f
        val servers = HashMap<String, String>()
        servers["Nwolfhub (official)"] = "https://notes.nwolfhub.org"
        val spinnerArrayAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, servers.keys.stream().collect(Collectors.toList())
        )
        spinner.adapter=spinnerArrayAdapter
        //Thread {animate(loginButton)}.start(); Thread{animate(registerButton)}.start()
        UpdateColors.updateColors(this, registerButton, loginButton, selector, secondLayout, returnButton, proceedLogin, findViewById(R.id.webLoginMainView))
        UpdateColors.updateBars(this)
        var selected = 0 //0 - nothing, 1 - login, 2 - register
        secondLayout.visibility = View.GONE
        selector.visibility=View.VISIBLE
        loginButton.setOnClickListener {
            selected = 1
            proceedLogin.text="Login"
            loginButton.isClickable=false
            registerButton.isClickable=false
            returnButton.isClickable=false
            Handler(Looper.getMainLooper()).postDelayed({
                returnButton.isClickable=true
            }, 800)
            playAnimation(selector, secondLayout)
        }
        registerButton.setOnClickListener {
            selected = 2
            proceedLogin.text="Register"
            loginButton.isClickable=false
            registerButton.isClickable=false
            returnButton.isClickable=false
            Handler(Looper.getMainLooper()).postDelayed({
                returnButton.isClickable=true
            }, 800)
            playAnimation(selector, secondLayout)
        }
        returnButton.setOnClickListener {
            selected = 0
            loginButton.isClickable=false
            registerButton.isClickable=false
            returnButton.isClickable=false
            Handler(Looper.getMainLooper()).postDelayed({
                loginButton.isClickable=true
                registerButton.isClickable=true
                username.setText("")
                password.setText("")
            }, 800)
            playAnimation(secondLayout, selector)
        }
        password.setOnKeyListener { view, i, keyEvent ->
            if(i==KeyEvent.KEYCODE_ENTER && keyEvent.action==KeyEvent.ACTION_UP) {
                when(selected) {
                    1 -> {
                        login(username.text.toString(), password.text.toString(), servers[(spinner.selectedItem as String).toString()].toString(), listOf(proceedLogin, returnButton), client)
                    }
                    2-> {
                        register(username.text.toString(), password.text.toString(), servers[(spinner.selectedItem as String).toString()].toString(), listOf(proceedLogin, returnButton), client)
                    }
                }
            }
            false
        }
        proceedLogin.setOnClickListener {
            when(selected) {
                1 -> {
                    login(username.text.toString(), password.text.toString(), servers[(spinner.selectedItem as String).toString()].toString(), listOf(proceedLogin, returnButton), client)
                }
                2-> {
                    register(username.text.toString(), password.text.toString(), servers[(spinner.selectedItem as String).toString()].toString(), listOf(proceedLogin, returnButton), client)
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Notes::class.java))
        finish()
    }
}