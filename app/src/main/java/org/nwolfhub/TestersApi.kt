package org.nwolfhub

import android.content.Context
import android.content.Context.MODE_PRIVATE

class TestersApi {
    fun checkVersion(context: Context) {
        val version:String = BuildConfig.VERSION_NAME
        val pref = context.getSharedPreferences("versions", MODE_PRIVATE)
        if(!pref.getString("lastVer", "").equals(version)) {
            pref.edit().putString("lastVer", version).apply()
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