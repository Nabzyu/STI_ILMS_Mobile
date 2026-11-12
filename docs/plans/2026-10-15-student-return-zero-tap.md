# Student return zero-tap Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Load `@.claude/skills/android-visual-verification/SKILL.md` after Compose changes and `@.claude/skills/verification-before-completion/SKILL.md` before claiming done. Design: `docs/plans/2026-10-15-student-return-zero-tap-design.md`.

**Goal:** Remove the fake student “Initiate return” flow so returning feels zero-tap: bring the book to the counter; admin web completes the return; the app only shows status.

**Architecture:** Navigation + Compose-only cleanup. No `LibraryApi` / repository changes. Loans stay on Overview and Activity (`BorrowingHistoryScreen`). Delete or stop routing to `ReturnBookScreen`.

**Tech Stack:** Kotlin, Jetpack Compose Navigation, STI Portal/Elms components, Gradle, emulator visual verification.

**Skills:** `@writing-plans` (this doc) · `@android-visual-verification` · `@verification-before-completion` · `@sti-design-system` (copy/components) · `@mobile-design` (reduce friction)

---

### Task 1: Strip Overview “Return” CTA

**Files:**
- Modify: `app/src/main/java/dashboard/overview/DashboardOverviewScreen.kt`
- Modify: callers of `onNavigateToReturnBook` in `app/src/main/java/dashboard/DashboardScreen.kt`
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt` (overview preview lambdas)

**Step 1: Remove the callback prop**

In `DashboardOverviewScreen`, delete parameter:

```kotlin
onNavigateToReturnBook: () -> Unit,
```

Remove every call site that passed `onNavigateToReturnBook = { … }` or `NoOp`.

**Step 2: Replace the loan card “Return” button with guidance**

Where the active-loan card currently has:

```kotlin
PortalButton(
    text = "Return",
    onClick = onNavigateToReturnBook,
    variant = PortalButtonVariant.Secondary,
    modifier = Modifier.fillMaxWidth()
)
```

Replace with short static copy (no button), e.g.:

```kotlin
Text(
    "Bring this book to the library counter to return it. Your account updates after staff process the return.",
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)
```

Optional: keep a secondary link **“View activity”** → existing `onNavigateToBorrowingHistory` only if that prop already exists on the screen; do not invent a new return route.

**Step 3: Compile check**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL` (or only errors from remaining ReturnBook references — fix in later tasks).

**Step 4: Commit** (only if user asked to commit)

```bash
git add app/src/main/java/dashboard/overview/DashboardOverviewScreen.kt app/src/main/java/dashboard/DashboardScreen.kt app/src/main/java/ui/preview/ScreenPreviews.kt
git commit -m "fix(return): remove overview return CTA; counter guidance only"
```

---

### Task 2: Unhook navigation route

**Files:**
- Modify: `app/src/main/java/dashboard/DashboardScreen.kt`
- Modify: `app/src/main/java/dashboard/DashboardRoute.kt`

**Step 1: Remove NavHost destination**

Delete:

```kotlin
composable(DashboardRoute.ReturnBook.route) {
    ReturnBookScreen(onBack = { dashboardNavController.popBackStack() })
}
```

Remove unused `ReturnBookScreen` import.

**Step 2: Remove stacked-route entry**

In `stacksOverTabs`, remove `DashboardRoute.ReturnBook.route`.

**Step 3: Fix bottom-nav parent mapping**

Remove or retarget:

```kotlin
currentRoute == DashboardRoute.ReturnBook.route -> DashboardRoute.BorrowingHistory.route
```

(If nothing else references `ReturnBook`, deleting the branch is enough.)

**Step 4: Remove route object**

In `DashboardRoute.kt`, delete:

```kotlin
object ReturnBook : DashboardRoute("return_book")
```

Grep the repo for `ReturnBook` / `return_book` and clear leftovers.

**Step 5: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`.

**Step 6: Commit** (if requested)

```bash
git commit -m "fix(return): remove return_book navigation route"
```

---

### Task 3: Delete ReturnBookScreen + previews

**Files:**
- Delete: `app/src/main/java/dashboard/returnbook/ReturnBookScreen.kt`
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt`

**Step 1: Remove preview**

Delete `ReturnBookScreenPreview` and its import of `ReturnBookScreen`.

**Step 2: Delete the screen file**

Remove `ReturnBookScreen.kt`. If `returnbook/` is empty, remove the package directory.

**Step 3: Final grep**

```powershell
rg -n "ReturnBook|return_book|Initiate return|ReturnBookScreen" app/src/main
```

Expected: no matches (except possibly unrelated words like “Returned” status / clearance copy “Return unreturned books…”).

**Step 4: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`.

**Step 5: Commit** (if requested)

```bash
git commit -m "fix(return): delete fake student return screen"
```

---

### Task 4: Activity screen — optional clarity copy

**Files:**
- Modify: `app/src/main/java/dashboard/history/BorrowingHistoryScreen.kt`

**Step 1: Add one-line subtitle or banner for active loans**

Under the existing Activity header description, ensure copy does **not** invite an in-app return action. Prefer extending the subtitle:

```text
Loans, returns, and due dates. Bring books to the counter to return them.
```

Do **not** add a Report-lost-style return button. Keep Cancel (pending) and Report lost (active) as they are — those are real APIs.

**Step 2: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

**Step 3: Commit** (if requested)

```bash
git commit -m "docs(ui): clarify counter return on activity screen"
```

---

### Task 5: Visual verification

**Skill:** `@.claude/skills/android-visual-verification/SKILL.md`

**Step 1: Install debug build**

```powershell
.\gradlew.bat :app:installDebug --console=plain
```

**Step 2: Capture Overview + Activity previews** (or signed-in navigation)

From `ScreenPreviews.kt`, drive PreviewActivity for overview and borrowing history; pull screenshots to `build/uishots/`.

Confirm:

- [ ] No “Return” / “Initiate return” button on overview loan cards
- [ ] Counter guidance visible on overview (and Activity if Task 4 ran)
- [ ] No crash navigating drawer / tabs
- [ ] Cancel pending / Report lost still present on Activity (unchanged)

**Step 3: Dark mode smoke** (per visual skill) on the same two screens if time allows.

---

### Task 6: Verification gate + gap note

**Skill:** `@.claude/skills/verification-before-completion/SKILL.md`

**Step 1: Fresh compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL` with exit code 0.

**Step 2: Update gap canvas status** (if still maintained)

In `canvases/web-vs-app-feature-gaps.canvas.tsx` (Cursor canvases dir), change student “Return at counter flow” from **Partial** to something like:

- App: “Guidance only (admin completes return)”
- Status: **Matched** *or* keep Partial only if you still want a dedicated help screen — prefer **Matched** for zero-tap intent once fake submit is gone.

**Step 3: Manual checklist**

- [ ] Student cannot open a fake return submit UI
- [ ] Active loans still visible
- [ ] Copy tells them to use the counter
- [ ] No new API endpoints
- [ ] Admin web return unchanged (out of scope)

---

## Done when

- [ ] `ReturnBook` route and screen gone
- [ ] Overview has no return CTA that implies app submission
- [ ] Activity still lists loans; cancel / lost unchanged
- [ ] Compile green + visual screenshots reviewed
- [ ] No student return API invented

## Out of scope

Admin scan-to-return, push on return, staff Android desk tools, Profile deploy, P2 deferred items.

## Execution handoff

After this plan is saved, choose:

1. **Subagent-Driven (this session)** — fresh subagent per task, review between tasks (`@subagent-driven-development`)
2. **Parallel Session** — new session with `@executing-plans`, batch with checkpoints
