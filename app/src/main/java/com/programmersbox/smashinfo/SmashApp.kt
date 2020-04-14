package com.programmersbox.smashinfo

import android.app.Application
import com.programmersbox.helpfulutils.defaultSharedPrefName
import com.programmersbox.loggingutils.Loged

class SmashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Loged.TAG = "SmashApp"
        Loged.FILTER_BY_PACKAGE_NAME = "programmersbox"
        defaultSharedPrefName = "smashApp"
    }
}