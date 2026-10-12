package com.example.stismartlib.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SmartLib design tokens — aligned with STI-College-Library-System (apps/web).
 * Brand navy for interactive states and light-mode ink; yellow STI mark; white/navy surfaces.
 *
 * Source of truth (web `index.css`):
 *   --sl-brand / --sl-text (light): #0B5EA2
 *   --sl-accent: #FFF200
 *   dark --sl-bg: #14151B, --sl-bg-elevated: #22232E, --sl-text: #F7F9FF
 */

/** Brand primary — STI SmartLib navy */
val StiBrandBlue = Color(0xFF0B5EA2)
val StiBrandBlueSoft = Color(0x0D0B5EA2)   // ~5% — muted fills
val StiBrandBlueMedium = Color(0x1A0B5EA2) // ~10%
val StiBrandBlueStrong = Color(0x660B5EA2) // ~40%

/**
 * @deprecated Name kept for call-site compatibility; value is SmartLib navy, not eLMS cyan.
 * Prefer [StiBrandBlue] in new code.
 */
val ElmsCyan = StiBrandBlue
val ElmsCyanSoft = StiBrandBlueSoft
val ElmsCyanMedium = StiBrandBlueMedium
val ElmsCyanStrong = StiBrandBlueStrong

/** STI brand yellow (logo tile / accent CTAs) */
val StiYellow = Color(0xFFFFF200)
val AccentYellow = StiYellow
val AccentYellow10 = Color(0x1AFFF200)
val AccentYellow35 = Color(0x59FFF200)
val AccentYellow40 = Color(0x66FFF200)

/** Ink on yellow surfaces — same as brand (web rule: yellow fill → navy text) */
val StiMarkBlue = StiBrandBlue

/** @deprecated Prefer StiBrandBlue / ElmsCyan; kept as alias for gradual migration */
val PrimaryBlue = StiBrandBlue
val PrimaryBlue5 = StiBrandBlueSoft
val PrimaryBlue10 = StiBrandBlueMedium
val PrimaryBlue15 = Color(0x260B5EA2)
val PrimaryBlue20 = Color(0x330B5EA2)
val PrimaryBlue45 = Color(0x730B5EA2)
val PrimaryBlue50 = Color(0x800B5EA2)
val PrimaryBlue60 = Color(0x990B5EA2)
val PrimaryBlue65 = Color(0xA60B5EA2)
val PrimaryBlue70 = Color(0xB30B5EA2)
val PrimaryBlue90 = Color(0xE60B5EA2)

val SuccessGreen = Color(0xFF2B8A3E)
val WarningYellow = AccentYellow
val WarningOrange = Color(0xFFF76707)
val ErrorRed = Color(0xFFE03131)
val InfoBlue = StiBrandBlue

/* —— Light surfaces (web :root) —— */
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0x0D0B5EA2) // brand @ 5%
val LightOnSurface = Color(0xFF0B5EA2)
val LightOnSurfaceVariant = Color(0xA60B5EA2) // brand @ ~65%
val LightOutline = Color(0x260B5EA2) // brand @ ~15%
val LightOutlineStrong = Color(0x400B5EA2)
val LightPlaceholder = Color(0x660B5EA2)
val LightInput = Color(0xFFFFFFFF)
val CardShadow = Color(0x14000000)

/* —— Dark surfaces (web html.dark) —— */
val DarkBackground = Color(0xFF14151B)
val DarkSurface = Color(0xFF22232E)
val DarkSurfaceVariant = Color(0xFF2A2B36)
val DarkOnSurface = Color(0xFFF7F9FF)
val DarkOnSurfaceMuted = Color(0xE0F7F9FF) // ~88%
val DarkOnSurfaceSubtle = Color(0xB8F7F9FF) // ~72%
val DarkBorder = Color(0x2EFFFFFF) // ~18%
val DarkInput = Color(0xFF001433)
val DarkFocusFill = Color(0xFF002266)

/* Legacy brand aliases → SmartLib light semantics */
val BrandMutedBg = StiBrandBlueSoft
val BrandBorder = LightOutline
val BrandBorderSoft = LightOutline
val BrandBorderStrong = LightOutlineStrong
val BrandTextMuted = LightOnSurfaceVariant
val BrandTextSubtle = Color(0x660B5EA2)
val BrandPlaceholder = LightPlaceholder
