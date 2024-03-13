package org.nwolfhub.notes

import android.animation.ArgbEvaluator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.nwolfhub.notes.deprecated.util.UpdateColors
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.util.ServerStorage
import org.nwolfhub.notes.util.ServerUtils


class ServerSelect : AppCompatActivity() {
    private lateinit var serverStorage:ServerStorage;
    companion object {
        var context:Context? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        UpdateColors.updateBars(this)
        setContentView(R.layout.activity_server_select)
        context = this;
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
        createNewServer.setOnClickListener { createNewServerInterface(null) }

        loadServerList()
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

    fun loadServerList() {
        val servers = serverStorage.servers;
        val adapter = ServersAdapter(servers, serverStorage)
        val recycler:RecyclerView = findViewById(R.id.serversList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter=adapter
    }

    private fun createNewServerInterface(prevUrl:String?) {
        val urlInput:EditText = EditText(this)
        if(prevUrl!=null) {
            urlInput.setText(prevUrl)
        }
        urlInput.inputType = InputType.TYPE_TEXT_VARIATION_URI
        val builder = AlertDialog.Builder(this)
            .setTitle("Create new server")
            .setMessage("Type the server uri")
            .setView(urlInput)
            .setPositiveButton("Ok", DialogInterface.OnClickListener { _, _ ->
                run {
                    if (urlInput.text.toString().contains("https://") || urlInput.text.contains("http://")) {
                        try {
                            Thread {
                                val svInfo = ServerUtils().readServer(urlInput.text.toString())
                                runOnUiThread {
                                    serverStorage.addServer(svInfo)
                                    loadServerList()
                                }
                            }.start()
                        } catch (e:Exception) {
                            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                            createNewServerInterface(urlInput.text.toString())
                        }
                    }
                }
            })
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    class ServersAdapter(private val servers: List<ServerInfo>, private val storage:ServerStorage):RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val serverName: TextView
            val serverAddress: TextView
            val serverVersion: TextView
            init {
                serverName = view.findViewById(R.id.serverName)
                serverAddress = view.findViewById(R.id.serverUrl)
                serverVersion = view.findViewById(R.id.serverVer)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_server, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return servers.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.setOnClickListener {
                AlertDialog.Builder(context!!)
                    .setTitle("Pick action")
                    .setPositiveButton("Select") { _, _, ->
                        run {
                            storage.selectServer(storage.getServer(servers[position].name))
                            context!!.startActivity(Intent(context, LoginActivity::class.java))
                            val copy = (context as Activity);
                            context = null
                            copy.finish()
                        }
                    }
                    .setNegativeButton("Delete"
                    ) { _, _, ->
                        run {
                            storage.removeServer(servers[position].address)
                            (context as ServerSelect).loadServerList()
                        }
                    }.show()
            }
            holder.serverName.text=servers[position].name
            holder.serverVersion.text=servers[position].version
            holder.serverAddress.text=servers[position].address
        }
    }
}