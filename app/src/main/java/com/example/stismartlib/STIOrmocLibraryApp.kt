package com.example.stismartlib

import android.app.Application
import com.example.stismartlib.data.LibraryRepository
import com.example.stismartlib.network.ApiClient
import com.example.stismartlib.session.SessionStore

class STIOrmocLibraryApp : Application() {
    lateinit var sessionStore: SessionStore
        private set
    lateinit var libraryRepository: LibraryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionStore = SessionStore(this)
        ApiClient.initialize(sessionStore)
        libraryRepository = LibraryRepository(sessionStore)
    }

    companion object {
        lateinit var instance: STIOrmocLibraryApp
            private set
    }
}
