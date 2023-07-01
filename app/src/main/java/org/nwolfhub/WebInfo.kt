package org.nwolfhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class WebInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_info)
        val userInfo:TextView = findViewById(R.id.signInfo)
        val logout:Button = findViewById(R.id.logout)
        UpdateColors.updateColors(this, findViewById(R.id.mainLoginInfoLayout), userInfo, logout)
    }
}