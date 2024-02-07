package org.nwolfhub.notes.deprecated

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.nwolfhub.notes.R

class Welcome : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        getSupportActionBar()?.hide()
        var state:Int = 1
        val nextBtn = findViewById<Button>(R.id.continueButton)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        val preferences = getSharedPreferences("main", MODE_PRIVATE)
        val welcomed = preferences.getBoolean("welcomed", false)
        if(welcomed) {startActivity(Intent(this, Notes::class.java)); finish()}
        nextBtn.setOnClickListener {
            when (state) {
                1 -> {
                    nextBtn.text = "Let's get started!"
                    welcomeText.text = "This app lets you to create notes and sync them with server"
                    state = 2
                }
                2 -> {
                    preferences.edit().putBoolean("welcomed", true).apply()
                    val intent:Intent = Intent(this, Notes::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}