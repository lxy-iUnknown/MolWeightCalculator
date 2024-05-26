package com.lxy.molweightcalculator.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun MainTheme(
    deviceKind: DeviceKind,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDarkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        useDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = when (deviceKind) {
            DeviceKind.Phone,
            DeviceKind.PhoneLandscape -> {
                Typography(
                    bodySmall = TextStyle(fontSize = 12.sp),
                    bodyLarge = TextStyle(fontSize = 16.sp)
                )
            }

            DeviceKind.Medium,
            DeviceKind.FoldingHorizontal,
            DeviceKind.FoldingVertical -> {
                Typography(
                    bodySmall = TextStyle(fontSize = 16.sp),
                    bodyLarge = TextStyle(fontSize = 20.sp)
                )
            }

            DeviceKind.Large -> {
                Typography(
                    bodySmall = TextStyle(fontSize = 22.sp),
                    bodyLarge = TextStyle(fontSize = 24.sp)
                )
            }
        },
        content = content
    )
}