package com.krstech.chesshelp

import android.app.Application

class KrsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Integration: install the real controller here, e.g. AppGraph.session = <service-backed SessionController>.
    }
}
