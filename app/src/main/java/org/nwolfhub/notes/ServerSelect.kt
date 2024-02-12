package org.nwolfhub.notes

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout


class ServerSelect : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_select)

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
        animation.duration=25000
        animation.interpolator=LinearInterpolator()
        animation.repeatCount=Animation.INFINITE
        animation.repeatMode=Animation.RESTART
        view.startAnimation(animation)
    }

    private fun animateGradient(view:View) {
        val start = Color.DKGRAY
        val mid = Color.MAGENTA
        val end = Color.BLUE
        val gradient = view.background as GradientDrawable

        val evaluator = ArgbEvaluator()
        val animator = TimeAnimator.ofFloat(0.0f, 1.0f)
        animator.duration = 1500
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
}