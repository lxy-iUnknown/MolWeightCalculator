package com.lxy.molweightcalculator.util

import android.content.Context
import androidx.startup.Initializer
import com.lxy.molweightcalculator.BuildConfig
import timber.log.Timber


@Suppress("unused")
class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}