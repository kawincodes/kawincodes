package com.krstech.chesshelp

import android.app.Application
import com.krstech.chesshelp.service.ServiceSessionController

class KrsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.session = ServiceSessionController
    }
}
