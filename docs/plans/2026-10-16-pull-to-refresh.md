# Pull-to-refresh Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add per-screen pull-to-refresh on core student/faculty list screens via a shared `ElmsPullRefresh` wrapper.

**Architecture:** Thin Compose wrapper around Material3 `PullToRefreshBox` owns the spinner and runs a screen-supplied suspend `onRefresh`. Screens keep `refreshError` and show `ElmsBanner`. Reload uses existing `LibraryRepository.load*()` methods only — no global pull, no new repository API.

**Tech Stack:** Kotlin, Jetpack Compose, Material3 (`PullToRefreshBox` via compose BOM), existing `userFacingMessage`, STI `ElmsBanner` / design system.

**Design:** `docs/plans/2026-10-16-pull-to-refresh-design.md`

**Skills:** @android @sti-design-system @lean-build

---

### Task 1: Add `ElmsPullRefresh` wrapper

**Files:**
- Create: `app/src/main/java/components/ElmsPullRefresh.kt`
- Package: `com.example.stismartlib.ui.components` (same as `PortalUi.kt`)

**Step 1: Create the composable**

```kotlin
package com.example.stismartlib.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.stismartlib.ui.feedback.userFacingMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElmsPullRefresh(
    onRefresh: suspend () -> Unit,
    onRefreshError: (String?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            if (isRefreshing) return@PullToRefreshBox
            scope.launch {
                isRefreshing = true
                onRefreshError(null)
                runCatching { onRefresh() }
                    .onFailure { error ->
                        onRefreshError(
                            userFacingMessage(error, "Unable to refresh. Please try again.")
                        )
                    }
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize(),
        state = state,
        contentAlignment = Alignment.TopCenter,
    ) {
        content()
    }
}
```

Adjust imports if the installed Material3 version nests `PullToRefreshBox` differently — match IDE resolve / BOM `2024.09.00`. Remove unused `Box` import if unused.

**Step 2: Compile check**

Run: `./gradlew :app:compileDebugKotlin` (Windows: `.\gradlew.bat :app:compileDebugKotlin`)

Expected: SUCCESS (or only pre-existing unrelated errors).

**Step 3: Commit** (only if user asked to commit)

```bash
git add app/src/main/java/components/ElmsPullRefresh.kt
git commit -m "feat(ui): add ElmsPullRefresh wrapper"
```

---

### Task 2: Wire Books + Research

**Files:**
- Modify: `app/src/main/java/dashboard/catalog/BookCatalogScreen.kt`
- Modify: `app/src/main/java/dashboard/research/ResearchCatalogScreen.kt`

**Step 1: Books**

- `var refreshError by remember { mutableStateOf<String?>(null) }`
- Wrap the existing scroll content (`LazyVerticalGrid` / `LazyColumn` branch) in:

```kotlin
ElmsPullRefresh(
    onRefresh = {
        repository.loadBooks(searchQuery.takeIf { it.isNotBlank() })
    },
    onRefreshError = { refreshError = it },
) { /* existing list UI */ }
```

- Show banner when `refreshError != null` (same place as existing `loadError` banner, or adjacent item). Prefer showing refresh error even when the list is non-empty.

**Step 2: Research** — same pattern with `loadResearch(query…)`.

**Step 3: Compile**

Run: `.\gradlew.bat :app:compileDebugKotlin`  
Expected: SUCCESS

**Step 4: Commit** (if user asked)

```bash
git commit -m "feat(catalog): pull-to-refresh books and research"
```

---

### Task 3: Wire activity lists (history, reservations, notifications)

**Files:**
- Modify: `app/src/main/java/dashboard/history/BorrowingHistoryScreen.kt`
- Modify: `app/src/main/java/dashboard/reserve/ReserveBookScreen.kt`
- Modify: `app/src/main/java/dashboard/notifications/NotificationsScreen.kt`

**Step 1:** Each screen: `refreshError` state + `ElmsPullRefresh` + banner.

| Screen | `onRefresh` body |
|--------|------------------|
| Borrowing history | `repository.loadBorrowingHistory()` |
| Reservations | `repository.loadReservations()` |
| Notifications | `repository.loadNotifications()` |

**Step 2: Compile** — same gradle command, expect SUCCESS.

**Step 3: Commit** (if user asked): `feat(dashboard): pull-to-refresh history reservations notifications`

---

### Task 4: Wire account + print lists (fines, clearance, print history)

**Files:**
- Modify: `app/src/main/java/dashboard/account/FinesScreen.kt`
- Modify: `app/src/main/java/dashboard/account/ClearanceScreen.kt`
- Modify: `app/src/main/java/dashboard/printing/PrintingHistoryScreen.kt`

**Step 1: `onRefresh` bodies**

| Screen | Body |
|--------|------|
| Fines | `repository.loadFines()` then `runCatching { repository.loadFinePolicy() }` |
| Clearance | `repository.loadClearance()` |
| Print history | `repository.loadPrintHistory()` then `runCatching { repository.loadPrintReceipts() }` |

Keep existing `LaunchedEffect` initial loads. Pull must not clear lists on failure.

**Step 2: Compile** — SUCCESS.

**Step 3: Commit** (if user asked): `feat(account): pull-to-refresh fines clearance print history`

---

### Task 5: Wire student Overview

**Files:**
- Modify: `app/src/main/java/dashboard/overview/DashboardOverviewScreen.kt`

**Step 1:** Only the student branch (`DashboardOverviewScreen` before `FacultyOverview` return). Skip `FacultyOverview` (static).

```kotlin
var refreshError by remember { mutableStateOf<String?>(null) }

ElmsPullRefresh(
    onRefresh = {
        repository.loadBorrowingHistory()
        repository.loadNotifications()
        repository.loadFines()
        repository.loadReservations()
        repository.loadPrintHistory()
        repository.loadBooks()
    },
    onRefreshError = { refreshError = it },
) {
    LazyColumn(...) {
        if (refreshError != null) {
            item {
                ElmsBanner(message = refreshError.orEmpty(), tone = ElmsBannerTone.Error)
            }
        }
        // existing items
    }
}
```

If any of these throws, wrapper surfaces one banner (first failure from sequential calls is fine — wrap the block so one failure fails the whole pull). Prefer:

```kotlin
onRefresh = {
    repository.loadBorrowingHistory()
    repository.loadNotifications()
    repository.loadFines()
    repository.loadReservations()
    repository.loadPrintHistory()
    repository.loadBooks()
}
```

Do **not** call `refreshStudentData()` (would flip global `isLoading`).

**Step 2: Compile** — SUCCESS.

**Step 3: Commit** (if user asked): `feat(overview): pull-to-refresh student home`

---

### Task 6: Verify

**Step 1: Unit / smoke**

No new pure logic required beyond `userFacingMessage` (already tested elsewhere). Optional: none.

**Step 2: Manual / emulator** (@android-visual-verification if using that skill)

1. Open Books with data → pull → spinner → data stays/updates; no blank flash.
2. Airplane mode → pull → list kept + error `ElmsBanner`.
3. Restore network → pull → banner clears.
4. Repeat once on Overview and Notifications.
5. Light + dark on Books.

**Step 3: Confirm out of scope untouched**

- No pull on Profile, Search, Book detail, Staff home.
- `DashboardScreen` still calls `refreshStudentData()` on session only.

---

## Done when

- All nine targets from the design (student Overview + eight lists) use `ElmsPullRefresh`.
- Failures keep data + `ElmsBanner`.
- Compile green; manual checks above pass.
