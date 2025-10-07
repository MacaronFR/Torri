package fr.imacaron.torri

import platform.UIKit.UIDevice

actual val platform: String = UIDevice.currentDevice.systemName

actual val version: String = UIDevice.currentDevice.systemVersion

actual val brand: String = UIDevice.currentDevice.model

actual val model: String = UIDevice.currentDevice.name

actual val deviceId: String = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: ""