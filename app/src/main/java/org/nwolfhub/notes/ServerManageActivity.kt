package org.nwolfhub.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.nwolfhub.notes.R
import org.nwolfhub.notes.model.Server

class ServerManageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_manage)
        PublicShared.serverManageActivity =this
        val recyclerView:RecyclerView = findViewById(R.id.serversRecycler)
        val preferences = getSharedPreferences("servers", MODE_PRIVATE)
        val list = PublicShared.buildServersList(preferences)
        list.add(Server("Add new", ""))
        Log.d("server list", "Showing server list of " + list.size + " elements")
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter= ServerRecyclerManager(list)
    }


    class ServerRecyclerManager(private val servers:List<Server>): RecyclerView.Adapter<ServerRecyclerManager.ServerHolder>() {
        class ServerHolder(view:View):RecyclerView.ViewHolder(view) {
            val serverName = view.findViewById<TextView>(R.id.servername)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.server, parent, false)
            return ServerHolder(itemView)
        }

        override fun getItemCount(): Int {
            return servers.size
        }

        override fun onBindViewHolder(holder: ServerHolder, position: Int) {
            holder.serverName.text=servers[position].name
            if(servers[position].name.equals("Add new")) {
                holder.serverName.tag="new"
            } else holder.serverName.tag=""
            holder.itemView.setOnClickListener {
                val pref = PublicShared.serverManageActivity.getSharedPreferences(
                    "servers",
                    MODE_PRIVATE
                )
                Log.d("server selector", "Requested server " + holder.serverName.text + " (new: " + holder.serverName.tag + ", url: " + pref.getString(holder.serverName.text.toString(), "") + ")")
                if(holder.serverName.tag.equals("new")) {
                    PublicShared.serverManageActivity.runOnUiThread {
                        PublicShared.buildServerDialog(
                            "",
                            "",
                            PublicShared.serverManageActivity.getSharedPreferences(
                                "servers",
                                MODE_PRIVATE
                            )
                        ).show()
                    }
                } else {
                    PublicShared.serverManageActivity.runOnUiThread {
                        PublicShared.buildServerDialog(
                            holder.serverName.text.toString(),
                            pref.getString(holder.serverName.text.toString(), ""),
                            pref
                        ).show()
                    }
                }
            }
            holder.itemView.setOnLongClickListener {
                if(holder.serverName.text.toString() != "Nwolfhub (official)") {
                    AlertDialog.Builder(PublicShared.serverManageActivity)
                        .setTitle("Do you want to delete this server?")
                        .setPositiveButton("Yes") { _, _ ->
                            Log.d(
                                "server deletion",
                                "Deleting server " + holder.serverName.text.toString()
                            )
                            val pref = PublicShared.serverManageActivity.getSharedPreferences(
                                "servers",
                                MODE_PRIVATE
                            )
                            pref.edit().remove(holder.serverName.text.toString()).apply()
                            PublicShared.serverManageActivity.restart()
                        }.setNegativeButton("No") { _, _ -> }.show()
                }
                true
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, WebLogin::class.java))
        finish()
    }

    fun restart() {
        startActivity(Intent(this, ServerManageActivity::class.java))
        finish()
    }
}