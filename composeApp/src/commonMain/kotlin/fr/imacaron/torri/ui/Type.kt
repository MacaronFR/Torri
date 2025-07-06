package fr.imacaron.torri.ui

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import torri.composeapp.generated.resources.*
import torri.composeapp.generated.resources.Res

@Composable
fun JosefinSansFamily() = FontFamily(
    Font(Res.font.JosefinSans_Thin, FontWeight.Thin),
    Font(Res.font.JosefinSans_ExtraLight, FontWeight.ExtraLight),
    Font(Res.font.JosefinSans_Light, FontWeight.Light),
    Font(Res.font.JosefinSans_Regular, FontWeight.Normal),
    Font(Res.font.JosefinSans_Medium, FontWeight.Medium),
    Font(Res.font.JosefinSans_SemiBold, FontWeight.SemiBold),
    Font(Res.font.JosefinSans_Bold, FontWeight.Bold),
    Font(Res.font.JosefinSans_ThinItalic, FontWeight.Thin, FontStyle.Italic),
    Font(Res.font.JosefinSans_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.JosefinSans_LightItalic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.JosefinSans_Italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.JosefinSans_MediumItalic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.JosefinSans_SemiBold, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.JosefinSans_Bold, FontWeight.Bold, FontStyle.Italic),
)

@Composable
fun JosefinSlabFamily(): FontFamily {
    val fontFamily = FontFamily(
        Font(Res.font.JosefinSlab_Thin, FontWeight.Thin),
        Font(Res.font.JosefinSlab_ExtraLight, FontWeight.ExtraLight),
        Font(Res.font.JosefinSlab_Light, FontWeight.Light),
        Font(Res.font.JosefinSlab_Regular, FontWeight.Normal),
        Font(Res.font.JosefinSlab_Medium, FontWeight.Medium),
        Font(Res.font.JosefinSlab_SemiBold, FontWeight.SemiBold),
        Font(Res.font.JosefinSlab_Bold, FontWeight.Bold),
        Font(Res.font.JosefinSlab_ThinItalic, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.JosefinSlab_ExtraLightItalic, FontWeight.ExtraLight, FontStyle.Italic),
        Font(Res.font.JosefinSlab_LightItalic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.JosefinSlab_Italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.JosefinSlab_MediumItalic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.JosefinSlab_SemiBold, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.JosefinSlab_Bold, FontWeight.Bold, FontStyle.Italic),
    )
    return fontFamily
}

// Default Material 3 typography values
val baseline = Typography()

@Composable
fun AppTypography() = Typography().run {
    val displayFontFamily = JosefinSansFamily()
    val bodyFontFamily = JosefinSansFamily()
    copy(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )
}

