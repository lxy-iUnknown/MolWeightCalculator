package com.lxy.molweightcalculator.startup

import android.content.Context
import androidx.profileinstaller.ProfileInstallerInitializer
import androidx.startup.Initializer
import com.lxy.molweightcalculator.BuildConfig
import timber.log.Timber

@Suppress("unused")
class GlobalInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return if (BuildConfig.DEBUG)
            emptyList()
        else
        // Install baseline profile
            listOf(ProfileInstallerInitializer::class.java)
    }
}