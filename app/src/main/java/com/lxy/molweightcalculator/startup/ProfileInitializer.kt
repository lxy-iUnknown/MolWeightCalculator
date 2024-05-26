package com.lxy.molweightcalculator.startup

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import androidx.profileinstaller.ProfileInstaller
import androidx.startup.Initializer
import com.lxy.molweightcalculator.BuildConfig
import java.util.Random
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Suppress("UNUSED")
class ProfileInitializer : Initializer<Unit> {
    companion object {
        private const val TAG = "ProfileInitializer"
        private const val DELAY_MS = 5000

        @SuppressLint("LogNotTimber")
        private fun logDebug(message: String) {
            Log.d(TAG, message)
        }

        @SuppressLint("LogNotTimber")
        private fun logError(message: String, data: Any?) {
            if (data is Throwable) {
                Log.e(TAG, message, data)
            } else {
                logDebug(message)
            }
        }

        private val PROFILER_INSTALLER_CALLBACK = object : ProfileInstaller.DiagnosticsCallback {
            override fun onDiagnosticReceived(code: Int, data: Any?) {
                logDebug(
                    when (code) {
                        ProfileInstaller.DIAGNOSTIC_CURRENT_PROFILE_EXISTS ->
                            "DIAGNOSTIC_CURRENT_PROFILE_EXISTS"

                        ProfileInstaller.DIAGNOSTIC_CURRENT_PROFILE_DOES_NOT_EXIST ->
                            "DIAGNOSTIC_CURRENT_PROFILE_DOES_NOT_EXIST"

                        ProfileInstaller.DIAGNOSTIC_REF_PROFILE_EXISTS ->
                            "DIAGNOSTIC_REF_PROFILE_EXISTS"

                        ProfileInstaller.DIAGNOSTIC_REF_PROFILE_DOES_NOT_EXIST ->
                            "DIAGNOSTIC_REF_PROFILE_DOES_NOT_EXIST"

                        ProfileInstaller.DIAGNOSTIC_PROFILE_IS_COMPRESSED ->
                            "DIAGNOSTIC_PROFILE_IS_COMPRESSED"

                        else -> "DIAGNOSTIC_UNKNOWN"
                    }
                )
            }

            override fun onResultReceived(code: Int, data: Any?) {
                when (code) {
                    ProfileInstaller.RESULT_INSTALL_SUCCESS ->
                        logDebug("RESULT_INSTALL_SUCCESS")

                    ProfileInstaller.RESULT_ALREADY_INSTALLED ->
                        logDebug("RESULT_ALREADY_INSTALL")

                    ProfileInstaller.RESULT_UNSUPPORTED_ART_VERSION ->
                        logDebug("RESULT_UNSUPPORTED_ART_VERSION")

                    ProfileInstaller.RESULT_NOT_WRITABLE ->
                        logDebug("RESULT_NOT_WRITABLE")

                    ProfileInstaller.RESULT_DESIRED_FORMAT_UNSUPPORTED ->
                        logDebug("RESULT_DESIRED_FORMAT_UNSUPPORTED")

                    ProfileInstaller.RESULT_BASELINE_PROFILE_NOT_FOUND ->
                        logError("RESULT_BASELINE_PROFILE_NOT_FOUND", data)

                    ProfileInstaller.RESULT_IO_EXCEPTION ->
                        logError("RESULT_IO_EXCEPTION", data)

                    ProfileInstaller.RESULT_PARSE_EXCEPTION ->
                        logError("RESULT_PARSE_EXCEPTION", data)

                    ProfileInstaller.RESULT_META_FILE_REQUIRED_BUT_NOT_FOUND ->
                        logDebug("RESULT_META_FILE_REQUIRED_BUT_NOT_FOUND")

                    ProfileInstaller.RESULT_INSTALL_SKIP_FILE_SUCCESS ->
                        logDebug("RESULT_INSTALL_SKIP_FILE_SUCCESS")

                    ProfileInstaller.RESULT_DELETE_SKIP_FILE_SUCCESS ->
                        logDebug("RESULT_DELETE_SKIP_FILE_SUCCESS")

                    ProfileInstaller.RESULT_SAVE_PROFILE_SIGNALLED ->
                        logDebug("RESULT_SAVE_PROFILE_SIGNALLED")

                    ProfileInstaller.RESULT_SAVE_PROFILE_SKIPPED ->
                        logDebug("RESULT_SAVE_PROFILE_SKIPPED")

                    ProfileInstaller.RESULT_BENCHMARK_OPERATION_SUCCESS ->
                        logDebug("RESULT_BENCHMARK_OPERATION_SUCCESS")

                    ProfileInstaller.RESULT_BENCHMARK_OPERATION_FAILURE ->
                        logDebug("RESULT_BENCHMARK_OPERATION_FAILURE")

                    ProfileInstaller.RESULT_BENCHMARK_OPERATION_UNKNOWN ->
                        logDebug("RESULT_BENCHMARK_OPERATION_UNKNOWN")

                    else -> logDebug("RESULT_UNKNOWN")
                }
            }
        }
    }

    override fun create(context: Context) {
        if (!BuildConfig.DEBUG) {
            Choreographer.getInstance().postFrameCallback {
                @SuppressLint("LogNotTimber")
                val handler = if (Build.VERSION.SDK_INT >= 28) {
                    Handler.createAsync(Looper.getMainLooper())
                } else {
                    Handler(Looper.getMainLooper())
                }
                val random = Random()
                val extra = random.nextInt(max(DELAY_MS / 5, 1))
                handler.postDelayed({
                    val executor = ThreadPoolExecutor(
                        0,
                        1,
                        0,
                        TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue()
                    )
                    ProfileInstaller.writeProfile(
                        context.applicationContext,
                        executor,
                        PROFILER_INSTALLER_CALLBACK
                    )
                }, (DELAY_MS + extra).toLong())
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}