package org.nwolfhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.nwolfhub.utils.TextAction
import org.nwolfhub.utils.Utils

class WebInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_info)
        val userInfo:TextView = findViewById(R.id.signInfo)
        var clicked = 0
        userInfo.setOnClickListener {
            clicked++
            if(clicked>=10) {
                startActivity(Intent(this, EasterActivity::class.java))
                finish()
            }
        }
        val logout:Button = findViewById(R.id.logout)
        UpdateColors.updateColors(this, findViewById(R.id.mainLoginInfoLayout), userInfo, logout)
        UpdateColors.updateBars(this)
        val preferences = getSharedPreferences("web", MODE_PRIVATE)
        logout.setOnClickListener {
            preferences.edit().putString("token", "").apply()
            startActivity(Intent(this, WebLogin::class.java))
            finish()
        }
        Thread {
            val username = WebUtils.getMe(
                preferences.getString("token", ""),
                preferences.getString("server", "")
            )
            Log.d("web check", "Token: " + preferences.getString("token", "") + ", server: " + preferences.getString("server", ""))
            Log.d("web check", "Obtained username: $username")
            if (username == null) {
                runOnUiThread {
                    startActivity(Intent(this, WebLogin::class.java))
                    finish()
                }
            } else {
                var prevText = ""
                runOnUiThread {
                    prevText = userInfo.text.toString()
                    logout.isEnabled=true
                }
                Utils.typeText(prevText, true, "", "Signed in online on " + preferences.getString("server", "") + " as $username", 50, 40, 100, object: TextAction() {
                    override fun applyText(text: String?) {
                        runOnUiThread {
                            userInfo.setText(text)
                        }
                    }
                })
            }
        }.start()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Notes::class.java))
        finish()
    }
}