package dev.crazo7924.onlymusic

import android.app.Application
import android.content.Context
import org.acra.config.dialog
import org.acra.ktx.initAcra

class OnlyMusicApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = org.acra.data.StringFormat.JSON

            dialog {
                text = "The application crashed."
                title = "Crash"
                reportDialogClass = CrashDialogActivity::class.java
            }
        }
    }
}
