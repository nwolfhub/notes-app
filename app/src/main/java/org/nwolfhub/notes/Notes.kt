package org.nwolfhub.notes

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.nwolfhub.notes.deprecated.util.UpdateColors
import org.nwolfhub.notes.model.Note
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.model.User
import org.nwolfhub.notes.util.NotesAdapter
import org.nwolfhub.notes.util.NotesStorage
import org.nwolfhub.notes.util.ServerStorage
import org.nwolfhub.notes.util.WebCacher
import org.nwolfhub.notes.util.WebWorker
import org.nwolfhub.utils.TextAction
import org.nwolfhub.utils.Utils
import java.util.Date


class Notes : AppCompatActivity() {
    lateinit var storage:ServerStorage
    lateinit var svInfo:ServerInfo
    lateinit var token: String
    lateinit var refreshToken: String
    lateinit var cacher: WebCacher
    private var cState = 0;
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
        val userPref = getSharedPreferences("userData", MODE_PRIVATE)
        val notesStorage = NotesStorage(getSharedPreferences("notes_updated", MODE_PRIVATE), getSharedPreferences("sync", MODE_PRIVATE))
        cacher = WebCacher(storage, notesStorage, User().setId(userPref.getString("currentUser", null)), svInfo)
        cState = 2
        animateGradient(state, 2)

        //begin syncing
        updateUserInfo(0)


        //load notes list
        reloadList(notesStorage, userPref)
        //Buttons click listeners, etc
        state.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pick action")
                .setPositiveButton("logout") { _, _, ->
                    run {
                        clearCookies()
                        storage.clearTokens(storage.activeServer!!.address)
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }
                    .setNegativeButton("Sync") { _, _, ->
                        run {
                            reloadList(notesStorage, userPref)
                        }
                    }.show()

        }
    }

    private fun updateUserInfo(attempts: Int) {
        val worker = WebWorker()
        val state = findViewById<View>(R.id.connectionState)
        val stateText = findViewById<TextView>(R.id.connectionStateText)
        Thread() {
            Log.d("updateUserInfo", "Thread launched")
            try {
                val me = worker.getMe(svInfo, token)
                Log.d("updateUserInfo", "Obtained user " + me.username)
                Utils.typeText(stateText.text.toString(), true, "", "Welcome, " + me.firstname, 60, 50, 0, object: TextAction() {
                    override fun applyText(text: String?) {
                        runOnUiThread {
                            stateText.text = text
                        }
                    }
                })
            } catch (e:RuntimeException) {
                Log.d("updateUserInfo", "Exception: $e")
                if(e.equals("401")) {
                    runOnUiThread {
                        cState=1
                        animateGradient(state, 1)
                    }
                    try {
                        worker.refreshAndPut(storage)
                        updateUserInfo(attempts+1)
                    } catch (e: RuntimeException) {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                } else if(e.equals("400") || e.equals("404")) {
                    if(attempts>=1) {
                        runOnUiThread {
                            cState=4
                            animateGradient(state, 4)
                        }
                        Utils.typeText(stateText.text.toString(), true, "", "Failed to connect to server", 60, 50, 0, object: TextAction() {
                            override fun applyText(text: String?) {
                                runOnUiThread {
                                    stateText.text = text
                                }
                            }
                        })
                    } else {
                        Thread.sleep(1000)
                        updateUserInfo(attempts+1)
                    }
                }
            }
            val userPref = getSharedPreferences("userData", MODE_PRIVATE)
            if(userPref.getLong("lastSync", 0)+86400000<Date().time) runOnUiThread {
                userPref.edit().putLong("lastSync", Date().time).apply()
                beginNoteFetch(userPref)
            }
            cacher.processQueue()
        }.start()
    }

    private fun reloadList(notesStorage: NotesStorage, userPref: SharedPreferences) {
        val dataset = notesStorage.getNotes(svInfo.address, userPref.getString("currentUser", null))
        val adapter = NotesAdapter(dataset.toTypedArray(), cacher, this)
        val recyclerView: RecyclerView = findViewById(R.id.notesList)
        recyclerView.adapter=adapter
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
                start = Color.WHITE
                mid = Color.GREEN
                end = Color.CYAN
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
        if(colors==3) {
            animator.duration=5000;
        } else {
            animator.duration = 1000
        }
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
    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun beginNoteFetch(userPref: SharedPreferences) {
        val notesStorage = NotesStorage(getSharedPreferences("notes_updated", MODE_PRIVATE), getSharedPreferences("sync", MODE_PRIVATE))
        val state = findViewById<View>(R.id.connectionState)
        cState=1
        animateGradient(state, 1)
        Thread() {
            var begin = true
            Thread() {
                while (begin) {
                    runOnUiThread {
                        reloadList(notesStorage, userPref)
                    }
                    Thread.sleep(100)
                }
            }.start()
            cacher.fetchNotes()
            Thread.sleep(500)
            if(cState==2 || cState==1) runOnUiThread {
                animateGradient(state, 3)
                cState=3
            }
            begin = false
        }.start()
    }
}