package fr.imacaron.torri

import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

fun WindowSizeClass.isHeightAtLeast(height: WindowHeightSizeClass): Boolean = when(height) {
	WindowHeightSizeClass.COMPACT -> true
	WindowHeightSizeClass.MEDIUM -> this.windowHeightSizeClass == WindowHeightSizeClass.MEDIUM || this.windowHeightSizeClass == WindowHeightSizeClass.EXPANDED
	WindowHeightSizeClass.EXPANDED -> this.windowHeightSizeClass == WindowHeightSizeClass.EXPANDED
	else -> false
}

fun WindowSizeClass.isWidthAtLeast(width: WindowWidthSizeClass): Boolean = when(width) {
	WindowWidthSizeClass.COMPACT -> true
	WindowWidthSizeClass.MEDIUM -> this.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM || this.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
	WindowWidthSizeClass.EXPANDED -> this.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
	else -> false
}