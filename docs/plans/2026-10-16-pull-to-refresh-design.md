# Pull-to-refresh on core lists (design)

**Date:** 2026-10-16  
**Status:** Approved (Approach 2 — thin shared wrapper)  
**Audience:** Student / Faculty  

## Goal

Let users pull down on core dashboard list screens to reload **that screen’s** data, without a global refresh control.

## Context

- Dashboard open already calls `LibraryRepository.refreshStudentData()` once (`DashboardScreen` `LaunchedEffect`).
- No `PullToRefreshBox` (or equivalent) exists in the app today.
- Screens already use `ElmsBanner` + `userFacingMessage` for load/action errors.
- Profile refreshes on open; Search has its own debounced refresh — both out of scope.

## Decisions

| Topic | Choice |
|-------|--------|
| Scope | Per-screen pull only (not global toolbar) |
| Screens | Overview (student), Books, Research, Borrowing history, Reservations, Notifications, Fines, Clearance, Print history |
| Shared UI | `ElmsPullRefresh` wrapper around Material3 `PullToRefreshBox` |
| Error UX | Keep current list; show `ElmsBanner` with short message |
| Success UX | Spinner stops only — no success banner |
| Repository | Reuse existing `load*()`; no new refresh API; do not set global `isLoading` for pull |
| Faculty Overview | Skip — static welcome copy, no live list |

## Architecture

```
User pulls on list screen
  → ElmsPullRefresh sets isRefreshing, clears refreshError
  → screen’s onRefresh suspend block calls load*() for that screen
  → on failure: refreshError = userFacingMessage(...); list unchanged
  → on success: spinner stops; banner cleared
```

- Wrapper owns spinner + pull gesture.
- Screen owns `refreshError` state and banner placement (controlled: `onRefreshError`).
- Dashboard-open `refreshStudentData()` stays unchanged.

## Per-screen reload map

| Screen | Pull calls |
|--------|------------|
| Overview (student) | `loadBorrowingHistory`, `loadNotifications`, `loadFines`, `loadReservations`, `loadPrintHistory`, `loadBooks` |
| Books | `loadBooks` (preserve current search query) |
| Research | `loadResearch` (preserve current query) |
| Borrowing history | `loadBorrowingHistory` |
| Reservations | `loadReservations` |
| Notifications | `loadNotifications` |
| Fines | `loadFines` + `runCatching { loadFinePolicy() }` |
| Clearance | `loadClearance` |
| Print history | `loadPrintHistory` + `runCatching { loadPrintReceipts() }` |

## Wrapper API (sketch)

```kotlin
@Composable
fun ElmsPullRefresh(
    onRefresh: suspend () -> Unit,
    onRefreshError: (String?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
```

- Clear error at pull start (`onRefreshError(null)`).
- On failure: `onRefreshError(userFacingMessage(error, fallback))`.
- Ignore overlapping pulls while refreshing (Material default).

Placement: `app/src/main/java/components/` same package as other `Elms*` (`com.example.stismartlib.ui.components`). Prefer a small dedicated file if `PortalUi.kt` stays large (~638 lines).

## Non-goals

- Global / toolbar refresh
- Profile, Search, detail, wizard, cart, QR, Staff home pull
- Auth token refresh
- Changing dashboard-open `refreshStudentData()`
- Success toasts, “last updated” timestamps, haptics beyond Material defaults

## Success criteria

- Listed screens show Material pull indicator and reload their mapped data
- Failed pull keeps prior data and shows `ElmsBanner`
- Successful pull clears that banner
- No full-screen loading flash when the list already has items
- Light + dark spot-check on Books and student Overview
