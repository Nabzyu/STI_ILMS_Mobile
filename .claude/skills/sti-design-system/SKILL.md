---
name: sti-design-system
description: Design tokens, shared Compose components, and UI conventions for the STI ILMS Android app (STIOrmocLibraryTheme, ElmsCyan/StiYellow tokens, Portal* and Elms* components). Use when adding or changing any screen, composable, colour, typography, spacing, or shape in this app, or when reviewing UI code for design consistency.
---

# STI ILMS Design System

The app uses a hand-built design system layered on Material 3, aligned with the STI SmartLib web app (`STI-College-Library-System` / `apps/web/src/index.css`). Reuse these tokens and components instead of inventing new ones.

## Source layout

Directories do **not** mirror packages. Match the package declaration, not the folder.

| Directory | Package |
|---|---|
| `app/src/main/java/components/` | `com.example.stismartlib.ui.components` |
| `app/src/main/java/auth/`, `dashboard/`, `navigation/`, `ui/` | `com.example.stismartlib.ui.*` |
| `app/src/main/java/com/example/stismartlib/ui/theme/` | `com.example.stismartlib.ui.theme` |

Key files:
- `ui/theme/Color.kt` — all colour tokens
- `ui/theme/Type.kt` — type scale + `EyebrowStyle`, `PortalKickerStyle`
- `ui/theme/Theme.kt` — `STIOrmocLibraryTheme`, light/dark `ColorScheme`
- `ui/theme/ThemeMode.kt` — `ThemeMode`, `ThemeController`, `LocalThemeController`, `isAppDarkTheme()`
- `components/PortalUi.kt` — shared portal components + shape tokens
- `dashboard/components/DashboardComponents.kt` — dashboard-only components

## Colour

Brand tokens from `Color.kt`:

| Token | Value | Use |
|---|---|---|
| `StiBrandBlue` | `#0B5EA2` | Primary interactive colour: buttons, links, focus, active tabs (web `--sl-brand`) |
| `ElmsCyan` / `ElmsCyanSoft` / … | aliases of brand navy | Legacy name; prefer `StiBrandBlue*` in new code |
| `StiYellow` | `#FFF200` | STI brand mark and yellow CTA variant (web `--sl-accent`) |
| `StiMarkBlue` | `#0B5EA2` | Ink on yellow surfaces (same as brand — web yellow→navy rule) |
| Light text | `#0B5EA2` | Light-mode body ink matches web `--sl-text` |
| Dark surfaces | `#14151B` / `#22232E` / `#F7F9FF` | Background / elevated / ink |
| `SuccessGreen` / `WarningOrange` / `ErrorRed` | — | Status semantics |

**Rules**

- Never write a raw `Color(0xFF…)` in a screen or component. Pull from `MaterialTheme.colorScheme` first; if the role genuinely does not exist, add a named token to `Color.kt` and use that.
- Prefer `MaterialTheme.colorScheme.*` over direct token references so dark mode resolves automatically. Reach for a raw token (`ElmsCyan`) only when the colour must stay fixed across themes.
- `PrimaryBlue`, `PrimaryBlue5` … `PrimaryBlue90` are **deprecated aliases** of the cyan ramp. Do not use them in new code. When you touch a line that uses one, migrate it to `ElmsCyan*` or the `colorScheme` role.
- `BrandMutedBg`, `BrandBorder`, `BrandTextMuted`, `BrandPlaceholder` and friends are legacy light-only aliases. They do not flip in dark mode — replace with `colorScheme` roles when editing nearby code.
- Yellow is brand chrome, not a status colour. Use `WarningOrange` for warnings.

## Theming and dark mode

Dark mode is first class and user-controlled, not just system-driven.

```kotlin
val controller = LocalThemeController.current   // LIGHT / DARK / SYSTEM
val isDark = isAppDarkTheme()
```

- `STIOrmocLibraryTheme(darkTheme: Boolean)` picks the scheme and syncs the status bar. There is **no dynamic colour** — brand consistency wins over Material You here. Do not add `dynamicColorScheme`.
- Every visual change must be checked in both light and dark. The most common defect in this codebase is a hardcoded light-only colour that turns invisible in dark mode.

## Typography

Use `MaterialTheme.typography.*`. Do not inline `fontSize` on a `Text` unless you are matching an existing adjacent line.

- `displayLarge/Medium/Small` — Black weight, tight negative tracking. Splash and hero only.
- `headlineLarge/Medium/Small` — Bold. Screen titles.
- `titleLarge/Medium/Small` — Section and card headings.
- `bodyLarge/Medium/Small` — Content. `bodyMedium` (14sp) is the workhorse.
- `labelLarge/Medium/Small` — Buttons, chips, metadata.

Two custom styles live outside the Material scale:

- `EyebrowStyle` — 11sp bold uppercase, wide tracking. Section kickers above a heading.
- `PortalKickerStyle` — 10sp, for portal chrome labels.

## Shape and spacing

Shape tokens in `components/PortalUi.kt`:

| Token | Radius | Use |
|---|---|---|
| `ShapeXl` | 12dp | Buttons, inputs, small cards |
| `Shape2Xl` | 16dp | Standard cards and sheets |
| `Shape3Xl` | 24dp | Hero and feature cards |
| `ShapePill` | full | Chips, badges, pill tabs |

Spacing follows a 4dp grid. Established rhythm, by frequency of use:

- `8.dp` — tight gaps inside a row or between label and value
- `12.dp` — default gap between stacked elements; internal card padding
- `16.dp` — screen horizontal padding; gap between cards
- `20.dp` / `24.dp` — gap between major sections
- `48.dp` — minimum touch target

Avoid introducing new off-grid values (`14.dp`, `18.dp`, `22.dp` exist but are legacy; do not add more).

## Component inventory

Reuse before building. Check both files — some names appear twice.

**`ui/components` (`components/PortalUi.kt`)** — app-wide:

| Component | Purpose |
|---|---|
| `PortalButton(text, onClick, variant, enabled, loading, leadingIcon)` | Primary button. Variants: `Primary`, `Secondary`, `Ghost`, `Yellow`. Has built-in 48dp min target, loading spinner, and button semantics. |
| `PortalField(label, value, onValueChange, …)` | Text input with label and focus states |
| `PageHeader` | Screen-level header |
| `SectionCard` / `ElmsSectionCard` | Titled card container |
| `StatCard` | Metric tile |
| `StatusBadge(label, warning)` | Small status pill |
| `ElmsPillTab`, `ElmsCountBadge` | Tab bar and counters |
| `ElmsListRow` | Standard list item |
| `ElmsBanner` | Inline informational banner |
| `ElmsEmptyState(title, description, actionLabel, onAction)` | Empty state |
| `ElmsLoadingBlock` | Centred loading spinner |
| `StiMarkTile`, `StiMark` | Brand mark |

**`dashboard/components/DashboardComponents.kt`** — dashboard-only:

`DashboardTopBar`, `QuickActionsSection`, `FeaturedQuickActionCard`, `QuickActionTile`, `ProcessStepCard`, `InfoRow`, `StatusChip`, `BookCoverPlaceholder`, `NotificationListItem`, `QrScannerPlaceholder`, `TopBarIconAction`, plus its own `StatCard`, `SectionCard`, `EmptyStateMessage`.

**Name collisions** — `StatCard`, `SectionCard`, and the empty-state components exist in both files with different signatures. Default to the `ui.components` (Portal) version for anything new. If you are editing inside `dashboard/`, check which one is already imported in that file before adding a call.

## Icons

Lucide is the primary icon set: `import com.composables.icons.lucide.*`. `material-icons-extended` is also on the classpath and used in places. Prefer Lucide for new work and keep a single set within any one screen.

## Loading, empty, and error states

Every data-backed screen needs all four states. Use the shared components rather than ad-hoc `Text("Loading…")`:

- loading → `ElmsLoadingBlock`
- empty → `ElmsEmptyState`
- error → `ElmsBanner`, with user-facing copy from `ui/feedback/UserFacingErrors.kt`
- success → content

## Known debt

Be aware of these; fix opportunistically when already editing the file, but do not start a sweeping migration unless asked.

- Deprecated `PrimaryBlue*` and legacy `Brand*` aliases still in use.
- Duplicate `StatCard` / `SectionCard` definitions across the two component files.
- Only 3 files use `stringResource`; most UI copy is hardcoded English literals.
- Only `DashboardScreen.kt` uses a Material `Scaffold`; other screens build their own chrome, so insets and top-bar behaviour vary between screens.
- No screenshot or UI test coverage. Verify changes by rendering — see the `android-visual-verification` skill.

## Related skills

- `android-visual-verification` — build, install, and screenshot the app to confirm a change actually looks right.
- `compose-ui-audit` — structured UI/UX review producing prioritised findings.
- `android` — general Material 3 and Android platform rules, including accessibility.
