package org.nwolfhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class EasterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easter)
        findViewById<ImageView>(R.id.colinea).setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://steamcommunity.com/profiles/76561198004814890"))
            startActivity(browserIntent)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, Notes::class.java))
        finish()
    }
}