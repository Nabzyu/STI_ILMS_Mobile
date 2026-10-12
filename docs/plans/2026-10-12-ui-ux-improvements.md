# STI ILMS UI/UX Improvements — Implementation Plan

> **For Claude:** Implement this plan task-by-task. Before each phase, read the skills listed under that phase. After each task that changes UI, verify with `@android-visual-verification` (PreviewActivity path). Do not claim a phase done without screenshots of the affected screens in light mode (and dark when the task touches theme or colour).

**Goal:** Bring the STI ILMS Android app to a consistent, accessible, SmartLib-aligned UI so every screen respects insets, uses the web palette correctly, communicates state honestly, and shares one component language.

**Architecture:** One shared screen chrome (`PortalScreen` / insets wrapper) + existing `Portal*` / `Elms*` components from `@sti-design-system`. Fix systemic issues once at the foundation, then polish journeys in severity order. Palette tokens already match the web SmartLib system (`#0B5EA2` / `#FFF200`); remaining work is layout, semantics, consistency, and state coverage.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Lucide icons, existing `PreviewApp` + `PreviewActivity` verification loop. No new UI libraries.

**Skills used across this plan:**

| Skill | When |
|---|---|
| `@sti-design-system` | Every visual change — tokens, shapes, Portal/Elms components |
| `@android-visual-verification` | After every UI task — PreviewActivity screenshots |
| `@compose-ui-audit` | Re-audit after Phase A and at plan end |
| `@android` | Accessibility, touch targets, TalkBack, contrast |
| `@mobile-design` | Touch-first decisions, offline/empty behaviour |
| `@ui-ux-pro-max` | Hierarchy, empty states, CTA clarity when redesigning a screen |
| `@error-handling-patterns` | Loading / empty / error / success on data screens |
| `@verification-before-completion` | Gate before any “phase done” or PR claim |
| `@code-review` / `@code-review-checklist` | End of each phase |
| `@unit-testing-test-generate` | Pure logic / ViewModel-adjacent helpers only |
| `@code-refactoring-refactor-clean` | Component consolidation (Phase D) — keep scope tight |
| `@create-pr` | Only when user asks for a PR |

**Already done (do not redo):**
- SmartLib palette applied in `Color.kt`, `Theme.kt`, `colors.xml` (`StiBrandBlue` `#0B5EA2`, yellow `#FFF200`, dark `#14151B` / `#22232E`)
- PreviewActivity verification path documented in `@android-visual-verification`
- Auth + most student screens audited via preview screenshots under `build/uishots/`

**Evidence baseline (from audit):**
- Status bar collision on nearly every screen (no `statusBarsPadding` / `systemBarsPadding` anywhere except incidental login spacing)
- Only `DashboardScreen.kt` uses `Scaffold`
- Clearance blocked state uses brand navy + checkmark icon (`ClearanceScreen.kt:64-66`)
- Borrow process has duplicate browse CTAs (`BorrowProcessScreen.kt:77-117`)
- Register dropdowns look like text fields; year level silently ignores taps
- Cold launch: stock Android theme + template launcher icon
- Text truncation on history / notifications dates
- Duplicate `StatCard` / `SectionCard` in `PortalUi.kt` vs `DashboardComponents.kt`

---

## Phase map (execute in order)

```
A  Foundation (insets + screen chrome)     ← biggest win / line of code
B  Honesty & affordance (misleading UI)
C  Auth journey polish
D  Component consistency
E  State coverage (loading/empty/error)
F  Brand shell (splash / launcher)
G  Dark-mode & large-font verification
H  Final audit + cleanup
```

Do not start Phase N+1 until Phase N verification shots exist in `build/uishots/phase-<letter>/`.

---

### Task 1: Shared screen insets wrapper

**Skills:** `@sti-design-system`, `@android`, `@mobile-design`

**Files:**
- Create: `app/src/main/java/components/PortalScreen.kt` (package `com.example.stismartlib.ui.components`)
- Modify: every screen that currently uses a bare `Column` / `LazyColumn` with only `padding(...)` and no insets (auth + dashboard screens listed in `ui/preview/ScreenPreviews.kt`)

**Step 1: Add `PortalScreen`**

```kotlin
// app/src/main/java/components/PortalScreen.kt
package com.example.stismartlib.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PortalScreen(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            content = content
        )
    }
}
```

**Step 2: Migrate Register first (known collision)**

- Modify: `app/src/main/java/auth/RegisterScreen.kt:72-78`
- Replace outer `Column(Modifier = Modifier.fillMaxSize().background(...).verticalScroll(...).padding(24.dp))` with `PortalScreen(scrollable = true, horizontalPadding = 16.dp, verticalPadding = 16.dp) { ... }`
- Keep form fields unchanged

**Step 3: Verify Register**

```powershell
# From @android-visual-verification
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
.\gradlew.bat :app:installDebug --console=plain
& $adb shell am start -n com.example.stismartlib/androidx.compose.ui.tooling.PreviewActivity `
  -e composable com.example.stismartlib.ui.preview.ScreenPreviewsKt.RegisterScreenPreview
# wait 6s, screencap → build/uishots/phase-a/register.png
# Read the PNG: STI mark must sit fully below status bar
```

**Step 4: Migrate remaining screens that lack insets**

Apply the same pattern (or wrap existing `LazyColumn` with outer `Modifier.statusBarsPadding().navigationBarsPadding()` if LazyColumn must stay):

- `auth/LoginScreen.kt` — use `PortalScreen` (or keep center arrangement but add insets)
- `dashboard/overview/DashboardOverviewScreen.kt`
- `dashboard/catalog/BookCatalogScreen.kt`, `BookDetailScreen.kt`, `BookCartScreen.kt`
- `dashboard/research/*`, `borrow/BorrowProcessScreen.kt`, `returnbook/ReturnBookScreen.kt`
- `reserve/ReserveBookScreen.kt`, `history/BorrowingHistoryScreen.kt`
- `notifications/NotificationsScreen.kt`, `qr/*`, `printing/*`
- `account/FinesScreen.kt`, `ClearanceScreen.kt`
- `staff/StaffHomeScreen.kt`
- `DashboardScreen.kt` — prefer Material `Scaffold` contentWindowInsets; do not double-pad

**Rule:** Horizontal padding standard = **16.dp** (was 20/24 mismatched). `@sti-design-system` spacing grid.

**Step 5: Batch verify**

Capture at least: Register, Login, Dashboard shell, Catalog, Fines, Clearance → `build/uishots/phase-a/`. Confirm no header under status bar.

**Step 6: Commit** (only if user asks)

```
fix(ui): respect system bars with shared PortalScreen chrome
```

---

### Task 2: Clearance blocked state — honest colour and icon

**Skills:** `@sti-design-system`, `@ui-ux-pro-max`, `@android` (contrast)

**Files:**
- Modify: `app/src/main/java/dashboard/account/ClearanceScreen.kt:56-87`

**Step 1: Split cleared vs blocked visuals**

- Cleared: keep brand navy (`StiBrandBlue` / `PrimaryBlue`) + `Lucide.BadgeCheck` + yellow accent OK
- Blocked: use `ErrorRed` (or `colorScheme.error`) background, `Lucide.ShieldAlert` or `Lucide.CircleAlert` icon, white text — **never** a checkmark on blocked

```kotlin
val bannerColor = if (cleared) StiBrandBlue else ErrorRed
val bannerIcon = if (cleared) Lucide.BadgeCheck else Lucide.ShieldAlert
// ...
.background(bannerColor)
Icon(bannerIcon, contentDescription = null, tint = if (cleared) AccentYellow else Color.White, ...)
```

**Step 2: Verify**

Preview `ClearanceScreenPreview` → `build/uishots/phase-b/clearance.png`. Blocked must read as danger at a glance.

**Step 3: Commit message (if asked)**

```
fix(clearance): use error styling for blocked standing
```

---

### Task 3: Borrow process — one primary CTA

**Skills:** `@ui-ux-pro-max`, `@sti-design-system`, `@mobile-design`

**Files:**
- Modify: `app/src/main/java/dashboard/borrow/BorrowProcessScreen.kt:70-120`

**Step 1: Simplify actions**

When cart empty:
- Keep steps card
- Keep `ElmsEmptyState` **without** nested action, OR keep empty action and remove bottom duplicate
- **One** primary: `PortalButton("Browse catalog", onBrowseCatalog)`
- Secondary only if cart non-empty: `Open book cart (N)`
- Back: `PortalButton(..., variant = Ghost)` or top-bar back — not a fourth stacked primary

When cart has items:
- Primary: `Open book cart (N)`
- Secondary: `Browse catalog`
- Ghost: Back

**Step 2: Verify**

`BorrowProcessScreenPreview` → `build/uishots/phase-b/borrow.png`. Exactly one filled primary button visible when cart empty.

---

### Task 4: Register dropdown affordance + year-level gate

**Skills:** `@sti-design-system`, `@android` (enabled states), `@mobile-design`

**Files:**
- Modify: `app/src/main/java/components/PortalUi.kt` (`PortalField` — optional `trailingIcon`)
- Modify: `app/src/main/java/auth/RegisterScreen.kt:140-188`

**Step 1: Trailing chevron on dropdown fields**

- Add optional `trailingIcon: ImageVector? = null` to `PortalField` if missing
- Program + year fields: `trailingIcon = Lucide.ChevronDown`
- Leading icons: program → `Lucide.GraduationCap` (or similar); year → `Lucide.Calendar`; stop reusing `Lucide.UserRound` for everything

**Step 2: Year level disabled state**

When `selectedProgram.isEmpty()`:
- Visually disable year field (alpha / muted border)
- Do not silently no-op; optional `ElmsBanner` or helper text: “Select a program first”

**Step 3: Fix deprecated `menuAnchor()`**

Use `Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)` (API already warned in build).

**Step 4: Align labels**

Replace hand-rolled `Text("Program or strand", fontSize = 14.sp)` with `PortalField` labels or `MaterialTheme.typography.titleSmall`.

**Step 5: Verify** Register light + dark via PreviewActivity / real nav.

---

### Task 5: Text truncation — dates and notification body

**Skills:** `@android`, `@sti-design-system`

**Files:**
- Modify: `app/src/main/java/dashboard/history/BorrowingHistoryScreen.kt` (Next due card, loan row)
- Modify: `app/src/main/java/dashboard/notifications/NotificationsScreen.kt` (message `maxLines`)
- Possibly: `dashboard/components/DashboardComponents.kt` list rows

**Step 1: Date formatting**

- Prefer short display: `Sep 15, 2026` or `2026-09-15` that fits one line
- Stat cards: `maxLines = 1`, `overflow = TextOverflow.Ellipsis` only after shortening format
- Notification message: show full due date; if truncate title, keep date visible (e.g. two-line layout: title + date on its own line)

**Step 2: Verify** History + Notifications previews at default and (if possible) `font_scale 1.3` on real activity.

---

### Task 6: Catalog filter labelling

**Skills:** `@ui-ux-pro-max`, `@sti-design-system`

**Files:**
- Modify: `app/src/main/java/dashboard/catalog/BookCatalogScreen.kt` (filter chip rows)

**Step 1:** Label rows (“Availability” / “Category”) with `EyebrowStyle` or `PortalKickerStyle`, OR rename chips so both “All” aren’t ambiguous (`All statuses` / `All categories`).

**Step 2:** Use one chip style (`ElmsPillTab`) for both rows.

**Step 3: Verify** `BookCatalogScreenPreview`.

---

### Task 7: Consolidate duplicate cards (optional but planned)

**Skills:** `@code-refactoring-refactor-clean`, `@sti-design-system` — **YAGNI if time-boxed; do after Tasks 1–6**

**Files:**
- Modify: `app/src/main/java/components/PortalUi.kt`
- Modify: `app/src/main/java/dashboard/components/DashboardComponents.kt`
- Update imports across `dashboard/**`

**Step 1:** Pick `ui.components` versions as canonical (`ElmsSectionCard` / `StatCard` in PortalUi).

**Step 2:** Delete or deprecate dashboard duplicates; fix call sites one package at a time.

**Step 3:** Compile + smoke previews for Overview, Fines, Clearance, Staff.

---

### Task 8: Loading / empty / error coverage on data screens

**Skills:** `@error-handling-patterns`, `@sti-design-system`, `@mobile-design`

**Files (representative):**
- Catalog, Research, History, Reservations, Fines, Notifications, Printing history
- Shared: `ElmsLoadingBlock`, `ElmsEmptyState`, `ElmsBanner`
- Errors: `ui/feedback/UserFacingErrors.kt`

**Step 1:** For each screen with network/repo load, ensure four states exist. Prefer repository flags / empty lists already available; do not invent a ViewModel layer unless required.

**Step 2:** Empty cart / empty history must use `ElmsEmptyState` with one CTA (see Task 3 pattern).

**Step 3:** Verify by seeding empty preview data (extend `PreviewScaffold.kt` with `seedSampleData = false` variants if needed).

---

### Task 9: Splash + launcher + DayNight theme

**Skills:** `@android`, `@sti-design-system`, `@mobile-design`

**Files:**
- Modify: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/values-night/themes.xml`
- Modify: `AndroidManifest.xml` (splash theme if using AndroidX SplashScreen)
- Replace: `mipmap` / adaptive icon foreground with STI mark (export from `StiMarkTile` design: yellow tile + navy “STI”)
- Optional dependency: `androidx.core:core-splashscreen` in `gradle/libs.versions.toml` + `app/build.gradle.kts`

**Step 1:** Parent theme → DayNight; window background → `light_background` / `dark_background`.

**Step 2:** SplashScreen API: brand background + STI icon; remove white flash in dark mode.

**Step 3:** Replace template `ic_launcher` Android robot.

**Step 4: Verify** cold start light + dark (`cmd uimode night yes/no`) — capture `build/uishots/phase-f/cold-*.png`.

---

### Task 10: Dark-mode preview matrix

**Skills:** `@android-visual-verification`, `@compose-ui-audit`, `@sti-design-system`

**Files:**
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt` — add dark variants for high-traffic screens OR add `@Preview(uiMode = UI_MODE_NIGHT_YES)` where `PreviewApp(darkTheme = true)` is used

**Step 1:** Add dark previews for: Login, Register, Dashboard shell, Overview, Catalog, Clearance, Fines.

**Step 2:** Capture all via PreviewActivity → `build/uishots/phase-g/`.

**Step 3:** Fix any invisible text / light-only `Brand*` leftovers found (`@compose-ui-audit` dimensions).

---

### Task 11: Accessibility sweep

**Skills:** `@android` §Accessibility, `@mobile-design`

**Checklist (per screen touched in earlier phases):**
- [ ] Interactive targets ≥ 48dp
- [ ] `contentDescription` on icon buttons; `null` on decorative
- [ ] Contrast ≥ 4.5:1 for body text (navy on white is OK; yellow text on white is not)
- [ ] Font scale 1.3 on Login + Overview + Catalog — no overlapping rows
- [ ] “Back” and primary actions reachable without gesture-only paths

**Verify:** real app or PreviewActivity + `settings put system font_scale 1.3`, then restore to `1.0`.

---

### Task 12: Final audit + verification gate

**Skills:** `@compose-ui-audit`, `@verification-before-completion`, `@code-review-checklist`

**Step 1:** Re-run scoped audit (student journey: Splash → Login → Dashboard → Catalog → Borrow → Fines → Clearance).

**Step 2:** Produce findings only for regressions / leftovers; severity table.

**Step 3:** `@verification-before-completion` — refuse “done” unless:
- Phase A–F verification folders exist with PNGs
- `:app:assembleDebug` succeeds
- No new critical findings

**Step 4:** Optional `@create-pr` when user requests.

---

## Out of scope (explicit)

- Backend / API contract changes
- Full string-resource migration of all hardcoded copy (opportunistic only)
- Roborazzi / Compose screenshot Gradle plugin (proven broken on AGP 9.0.1)
- Staff inventory / borrow-return tools (placeholder “Soon” cards stay until product specs exist)
- Renaming every `ElmsCyan` call site to `StiBrandBlue` (aliases are fine)

---

## Suggested commit cadence

One commit per Task 1–6 and Task 9; Tasks 7–8 may split by screen group. Conventional Commits, e.g.:

- `fix(ui): respect system bars with PortalScreen`
- `fix(clearance): error styling for blocked status`
- `fix(borrow): collapse duplicate browse actions`
- `fix(auth): dropdown chevrons and year-level disabled state`
- `fix(ui): prevent date truncation on activity and notifications`
- `feat(brand): DayNight splash and STI launcher icon`

Do **not** commit unless the user asks.

---

## Execution options

**Plan complete and saved to `docs/plans/2026-10-12-ui-ux-improvements.md`.**

**1. Subagent-Driven (this session)** — Fresh agent per task, review between tasks, start at Task 1 (PortalScreen).

**2. Parallel Session** — New chat: “Execute `docs/plans/2026-10-12-ui-ux-improvements.md` starting at Task 1.”

**3. Single-threaded here** — I implement Task 1 → verify → pause for your OK → continue.

Which approach?
