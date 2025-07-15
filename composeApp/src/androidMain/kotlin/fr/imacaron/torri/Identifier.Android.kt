package fr.imacaron.torri

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings

actual val platform: String = "Android"

actual val version: String = Build.VERSION.SDK_INT.toString()

actual val brand: String = Build.BRAND

actual val model: String = Build.MODEL

@SuppressLint("HardwareIds")
actual val deviceId: String = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)