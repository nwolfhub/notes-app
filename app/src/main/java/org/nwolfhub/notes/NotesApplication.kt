package org.nwolfhub.notes

import android.app.Application
import android.content.Context
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

open class NotesApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            //each plugin you chose above can be configured in a block like this:
            mailSender {
                enabled=true
                mailTo="bug-reports@nwolfhub.org"
            }
        }
    }
}