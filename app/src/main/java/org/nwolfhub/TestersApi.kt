package org.nwolfhub

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log

class TestersApi {
    fun checkVersion(context: Context) {
        val version:String = BuildConfig.VERSION_NAME
        val pref = context.getSharedPreferences("testers", MODE_PRIVATE)
        Log.d("testers api", "Last version: " + pref.getString("lastVer", "") + ", new version: $version")
        if(!pref.getString("lastVer", "").equals(version)) {
            pref.edit().putString("lastVer", version).apply()
            Log.d("testers api", "Calling testers ui")
            val i = context.packageManager.getLaunchIntentForPackage("org.nwolfhub.testers")
            i!!.action = "org.nwolfhub.testers.version_report"
            i.putExtra("app", "6")
            i.putExtra("ver", version)
            i.putExtra("appPackage", "org.nwolfhub.notes")
            println("Reporting update to testers!")
            context.startActivity(i)
        }
    }
}