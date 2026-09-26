package com.example.stismartlib.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * STI Ormoc Smart Library design tokens — synced from
 * apps/web/src/index.css (:root / html.dark) and apps/web/src/components/ui.tsx
 */
val PrimaryBlue = Color(0xFF003399)
val AccentYellow = Color(0xFFFFF200)

val BrandMutedBg = Color(0x0D003399) // rgba(0, 51, 153, 0.05)
val BrandBorder = Color(0x26003399) // rgba(0, 51, 153, 0.15)
val BrandBorderSoft = Color(0x1A003399) // rgba(0, 51, 153, 0.10)
val BrandBorderStrong = Color(0x33003399) // rgba(0, 51, 153, 0.20)
val BrandTextMuted = Color(0xA6003399) // rgba(0, 51, 153, 0.65)
val BrandTextSubtle = Color(0x73003399) // rgba(0, 51, 153, 0.45)
val BrandPlaceholder = Color(0x66003399) // rgba(0, 51, 153, 0.40)

val SuccessGreen = Color(0xFF2B8A3E)
val WarningYellow = AccentYellow
val WarningOrange = Color(0xFFF76707)
val ErrorRed = Color(0xFFE03131)
val InfoBlue = PrimaryBlue

val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = BrandMutedBg
val LightOnSurface = PrimaryBlue
val LightOnSurfaceVariant = BrandTextMuted

/** Dark mode — STI palette only (deep navy / white / yellow) */
val DarkBackground = Color(0xFF000D2B)
val DarkSurface = Color(0xFF001A4D)
val DarkSurfaceVariant = Color(0x14FFFFFF) // rgba(255,255,255,0.08)
val DarkOnSurface = Color(0xFFF7F9FF)
val DarkOnSurfaceMuted = Color(0xE0F7F9FF) // ~0.88
val DarkOnSurfaceSubtle = Color(0xB8F7F9FF) // ~0.72
val DarkBorder = Color(0x2EFFFFFF) // rgba(255,255,255,0.18)
val DarkInput = Color(0xFF001433)
val DarkFocusFill = Color(0xFF002266)

val PrimaryBlue5 = Color(0x0D003399)
val PrimaryBlue10 = Color(0x1A003399)
val PrimaryBlue15 = Color(0x26003399)
val PrimaryBlue20 = Color(0x33003399)
val PrimaryBlue45 = Color(0x73003399)
val PrimaryBlue50 = Color(0x80003399)
val PrimaryBlue60 = Color(0x99003399)
val PrimaryBlue65 = Color(0xA6003399)
val PrimaryBlue70 = Color(0xB3003399)
val PrimaryBlue90 = Color(0xE6003399)

val AccentYellow10 = Color(0x1AFFF200)
val AccentYellow35 = Color(0x59FFF200)
val AccentYellow40 = Color(0x66FFF200)

/** Soft card elevation matching shadow-[0_1px_3px_rgba(0,51,153,0.08)] */
val CardShadow = Color(0x14003399)
