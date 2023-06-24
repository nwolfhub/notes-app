package org.nwolfhub

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class Welcone : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        getSupportActionBar()?.hide()
        var state:Int = 1
        val nextBtn = findViewById<Button>(R.id.continueButton)
        val welcomeText = findViewById<TextView>(R.id.welcomeText)
        nextBtn.setOnClickListener {
            when (state) {
                1 -> {
                    nextBtn.text = "Let's get started!"
                    welcomeText.text = "This app lets you to create notes and sync them with server"
                    state = 2
                }
                2 -> {
                    val intent:Intent = Intent(this, Notes::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}