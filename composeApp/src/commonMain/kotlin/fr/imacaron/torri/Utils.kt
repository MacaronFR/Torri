package fr.imacaron.torri

import androidx.window.core.layout.WindowSizeClass

enum class SizeClass {
	COMPACT,
	MEDIUM,
	EXPANDED
}

fun WindowSizeClass.isHeightAtLeast(height: SizeClass): Boolean = when(height) {
	SizeClass.COMPACT -> true
	SizeClass.MEDIUM -> this.isHeightAtLeastBreakpoint(480)
	SizeClass.EXPANDED -> this.isHeightAtLeastBreakpoint(900)
}

fun WindowSizeClass.isWidthAtLeast(width: SizeClass): Boolean = when(width) {
	SizeClass.COMPACT -> true
	SizeClass.MEDIUM -> this.isWidthAtLeastBreakpoint(600)
	SizeClass.EXPANDED -> this.isWidthAtLeastBreakpoint(840)
}

expect fun Double.formatPrice(): String