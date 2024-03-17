package org.nwolfhub.notes.util

import okhttp3.OkHttpClient
import org.nwolfhub.notes.model.ServerInfo
import org.nwolfhub.notes.model.User

class WebCacher(storage: NotesStorage, me: User, server:ServerInfo) {
    private val client = OkHttpClient()
}