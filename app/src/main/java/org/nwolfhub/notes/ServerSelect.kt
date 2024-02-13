package org.nwolfhub.notes

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import org.nwolfhub.notes.deprecated.util.UpdateColors
import org.nwolfhub.notes.util.ServerStorage


class ServerSelect : AppCompatActivity() {
    private lateinit var serverStorage:ServerStorage;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        UpdateColors.updateBars(this)
        setContentView(R.layout.activity_server_select)
        serverStorage = ServerStorage(getSharedPreferences("web_updated", MODE_PRIVATE))

        //prepare spinning globe
        val globeView:WebView = findViewById(R.id.globeView)
        globeView.setBackgroundColor(Color.TRANSPARENT)
        globeView.setOnTouchListener { v, _ -> true }
        globeView.isVerticalScrollBarEnabled = false
        globeView.isHorizontalScrollBarEnabled = false
        globeView.loadUrl("file:///android_res/raw/globe_icon_texted.svg")
        val globeContainer:ConstraintLayout = findViewById(R.id.globeVIewContainer)
        animateGlobe(globeContainer)

        //prepare "create new server" button
        val createNewServer:Button = findViewById(R.id.newServer)
        animateGradient(createNewServer)
    }



    private fun animateGlobe(view: View) {
        val animation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation.duration=20000
        animation.interpolator=LinearInterpolator()
        animation.repeatCount=Animation.INFINITE
        animation.repeatMode=Animation.RESTART
        view.startAnimation(animation)
    }

    private fun animateGradient(view:View) {
        val start = Color.BLACK
        val mid = Color.BLACK
        val end = Color.WHITE
        val gradient = view.background as GradientDrawable

        val evaluator = ArgbEvaluator()
        val animator = TimeAnimator.ofFloat(-1.0f, 1.0f)
        animator.duration = 10000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener {
            val fraction = it.animatedFraction
            val newStart = evaluator.evaluate(fraction, start, end) as Int
            val newMid = evaluator.evaluate(fraction, mid, start) as Int
            val newEnd = evaluator.evaluate(fraction, end, mid) as Int
            gradient.colors = intArrayOf(newStart, newMid, newEnd)
        }

        animator.start()
    }

    private fun loadServerList() {
        val servers = Se
    }
}