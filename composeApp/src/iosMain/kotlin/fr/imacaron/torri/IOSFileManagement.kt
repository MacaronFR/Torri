package fr.imacaron

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun saveToFile(file: String, text: String) {
	val path = fileManager.URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
    NSString.create(text).dataUsingEncoding(encoding = NSUTF8StringEncoding)?.writeToFile(path!!.path + "/$file", atomically = true)
}

private val fileManager = NSFileManager.defaultManager