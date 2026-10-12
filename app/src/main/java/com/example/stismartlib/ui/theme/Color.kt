package com.example.stismartlib.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * STI eLMS–inspired design tokens (light + dark).
 * Accent cyan for interactive states; yellow STI mark; charcoal/white surfaces.
 */

/** Interactive accent — eLMS sky/cyan */
val ElmsCyan = Color(0xFF00A0D2)
val ElmsCyanSoft = Color(0x1A00A0D2)
val ElmsCyanMedium = Color(0x3300A0D2)
val ElmsCyanStrong = Color(0x6600A0D2)

/** STI brand yellow (logo tile) */
val StiYellow = Color(0xFFFFF200)
val AccentYellow = StiYellow
val AccentYellow10 = Color(0x1AFFF200)
val AccentYellow35 = Color(0x59FFF200)
val AccentYellow40 = Color(0x66FFF200)

/** Deep blue for STI mark lettering on yellow */
val StiMarkBlue = Color(0xFF003399)

/** @deprecated Prefer ElmsCyan; kept as alias for gradual migration */
val PrimaryBlue = ElmsCyan
val PrimaryBlue5 = ElmsCyanSoft
val PrimaryBlue10 = Color(0x1A00A0D2)
val PrimaryBlue15 = Color(0x2600A0D2)
val PrimaryBlue20 = Color(0x3300A0D2)
val PrimaryBlue45 = Color(0x7300A0D2)
val PrimaryBlue50 = Color(0x8000A0D2)
val PrimaryBlue60 = Color(0x9900A0D2)
val PrimaryBlue65 = Color(0xA600A0D2)
val PrimaryBlue70 = Color(0xB300A0D2)
val PrimaryBlue90 = Color(0xE600A0D2)

val SuccessGreen = Color(0xFF2B8A3E)
val WarningYellow = AccentYellow
val WarningOrange = Color(0xFFF76707)
val ErrorRed = Color(0xFFE03131)
val InfoBlue = ElmsCyan

/* —— Light surfaces —— */
val LightBackground = Color(0xFFF5F6F8)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEBEDF0)
val LightOnSurface = Color(0xFF1A1B1F)
val LightOnSurfaceVariant = Color(0xFF5C5F66)
val LightOutline = Color(0x1F1A1B1F)
val LightOutlineStrong = Color(0x331A1B1F)
val LightPlaceholder = Color(0x991A1B1F)
val LightInput = Color(0xFFFFFFFF)
val CardShadow = Color(0x14000000)

/* —— Dark surfaces (eLMS charcoal) —— */
val DarkBackground = Color(0xFF121218)
val DarkSurface = Color(0xFF1E1E26)
val DarkSurfaceVariant = Color(0xFF2A2A34)
val DarkOnSurface = Color(0xFFF5F6F8)
val DarkOnSurfaceMuted = Color(0xFFB8BAC0)
val DarkOnSurfaceSubtle = Color(0xFF8A8D96)
val DarkBorder = Color(0x33FFFFFF)
val DarkInput = Color(0xFF18181F)
val DarkFocusFill = Color(0xFF243040)

/* Legacy brand aliases → map to ELMS light semantics */
val BrandMutedBg = ElmsCyanSoft
val BrandBorder = LightOutline
val BrandBorderSoft = LightOutline
val BrandBorderStrong = LightOutlineStrong
val BrandTextMuted = LightOnSurfaceVariant
val BrandTextSubtle = Color(0x991A1B1F)
val BrandPlaceholder = LightPlaceholder
