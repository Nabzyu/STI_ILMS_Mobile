---
title: UI and design system
tags: [ui, theme, design-system]
---

# UI and design system

Back to [index](README.md). Detailed conventions live in the project skill `.claude/skills/sti-design-system/SKILL.md`; this page is the overview.

## Theme

`com/example/stismartlib/ui/theme/`:

- **`Color.kt`** — tokens aligned with the SmartLib web app. Brand blue `#0B5EA2` (`StiBrandBlue`, aliased as `ElmsCyan`, `PrimaryBlue`, `StiMarkBlue`), brand yellow `#FFF200` (`StiYellow`), alpha variants (`PrimaryBlue5…90`, `AccentYellow10/35/40`), status colours (`SuccessGreen`, `WarningOrange`, `ErrorRed`). Light mode uses white surfaces with brand-blue text; dark mode uses `#14151B` background and `#22232E` surfaces.
- **`Theme.kt`** — `STIOrmocLibraryTheme(darkTheme)` builds Material 3 light/dark schemes from those tokens and sets status-bar colour and icon contrast. No dynamic colour.
- **`Type.kt`** — `Typography`.
- **`ThemeMode.kt`** — `ThemeMode` (Light / Dark / System), `ThemeController` in `LocalThemeController`, `isAppDarkTheme()`. The controller is `rememberSaveable`, so the choice survives rotation but **resets to System on a fresh app start**. The drawer toggle flips between Light and Dark.

## Shared components

`components/PortalUi.kt`:
- Shapes: `ShapeXl` 12 dp, `Shape2Xl` 16 dp, `Shape3Xl` 24 dp, `ShapePill`.
- Brand: `StiMarkTile`, `StiMark`.
- Layout: `PageHeader` (eyebrow / title / description), `SectionCard`, `ElmsSectionCard`, `StatCard`, `ElmsListRow`.
- Status and feedback: `StatusBadge`, `ElmsCountBadge`, `ElmsPillTab`, `ElmsBanner` (tones), `ElmsEmptyState`, `ElmsLoadingBlock`.
- Inputs: `PortalButton` (variants, loading, leading icon), `PortalField`.

`components/AppDialog.kt` — `AppDialog`, the app's styled dialog.
`components/PortalScreen.kt` — `PortalScreen` wrapper and `Modifier.portalInsets()` (status + navigation bar padding).
`dashboard/components/DashboardComponents.kt` — older dashboard pieces: `DashboardTopBar`, `InfoRow`, `StatusChip`, `BookCoverPlaceholder`, `QuickActionsSection` / tiles, `ProcessStepCard`, `QrScannerPlaceholder`, `NotificationListItem`.

Icons come from Lucide (`com.composables:icons-lucide-cmp`) plus Material extended icons.

## Previews

`ui/preview/PreviewScaffold.kt` provides `PreviewApp(userType, seedSampleData, darkTheme) { }`, which builds an in-memory `LibraryRepository` with sample books, loans, fines etc., and supplies it through `LocalLibraryRepository`. `ui/preview/ScreenPreviews.kt` has 36 `@Preview` functions covering the screens.

Because `ui-tooling` is a debug dependency, a debug build can render any preview on a device via `androidx.compose.ui.tooling.PreviewActivity` — no login needed. The full loop (build, install, launch preview, screenshot) is in `.claude/skills/android-visual-verification/SKILL.md`.

## Current UI work

The phased UI/UX plan — insets, palette usage, state coverage, component consolidation — is in [`docs/plans/2026-10-12-ui-ux-improvements.md`](../plans/2026-10-12-ui-ux-improvements.md).
