package org.nwolfhub.notes

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.nwolfhub.notes.deprecated.util.UpdateColors
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.util.ServerStorage
import org.nwolfhub.notes.util.WebWorker

class Notes : AppCompatActivity() {
    lateinit var storage:ServerStorage
    lateinit var svInfo:ServerInfo
    lateinit var token: String
    lateinit var refreshToken: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        UpdateColors.updateBars(this)
        setContentView(R.layout.activity_notes2)
        //load resources
        val stateText = findViewById<TextView>(R.id.connectionStateText)
        val state = findViewById<View>(R.id.connectionState)
        storage = ServerStorage(getSharedPreferences("web_updated", MODE_PRIVATE))
        svInfo = storage.activeServer!!
        token = storage.getToken(svInfo.address)!!
        refreshToken = storage.getRefreshToken(svInfo.address)!!
        animateGradient(state, 2)
        val worker = WebWorker()

        //begin syncing
        Thread() {
            try {
                val me = worker.getMe(svInfo, token)
            } catch (e:RuntimeException) {
                if(e.equals("401")) {

                }
            }
        }

    }


    private fun animateGradient(view:View, colors:Int) {
        var start = Color.BLACK
        var mid = Color.BLACK
        var end = Color.WHITE
        when (colors) {
            1 -> {
                start = Color.GREEN
                mid = Color.CYAN
                end = Color.BLUE
            }
            2 -> {
                start = Color.BLUE
                mid = Color.BLUE
                end = Color.WHITE
            }
            3 -> {
                start = Color.GREEN
                mid = Color.GREEN
                end = Color.BLUE
            }
            4 -> {
                start = Color.RED
                mid = Color.BLACK
                end = Color.MAGENTA
            }
        }
        val gradient = view.background as GradientDrawable
        val evaluator = ArgbEvaluator()
        val animator = TimeAnimator.ofFloat(-1.0f, 1.0f)
        animator.duration = 1000
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