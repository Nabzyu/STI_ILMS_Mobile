---
title: Startup and navigation
tags: [navigation, lifecycle]
---

# Startup and navigation

Back to [index](README.md). Related: [Authentication and session](authentication-and-session.md), [Screens](screens.md).

## Cold start

`MainActivity.onCreate()` (`app/src/main/java/com/example/stismartlib/MainActivity.kt`):

1. `installSplashScreen()` and keeps the system splash visible while `keepSplashVisible` is true.
2. Launches `repository.restoreSession()` on `lifecycleScope`. That reads the stored token and calls `GET api/v1/auth/me` to confirm it is still valid.
3. Picks the start destination:
   - session restored and role is staff → `staff_home`
   - session restored otherwise → `dashboard/{STUDENT|FACULTY}`
   - no session or `/me` failed → `login`
4. Hides the splash and composes `AppNavGraph(startDestination)` inside the theme and `ProvideLibraryRepository`.

Nothing is drawn until the start destination is known, so there is no flash of the login screen for signed-in users. If the network is down, `/me` fails and the user lands on login with the stored session cleared.

`ui/splash/SplashScreen.kt` is a **preview-only** branded splash; real cold start uses the Android SplashScreen API and the `Theme.STIOrmocLibrary` styles in `res/values/themes.xml`.

## Two navigation graphs

### Top level — `navigation/NavGraph.kt`

| Route (`Screen`) | Composable | Notes |
|---|---|---|
| `login` | `LoginScreen` | default start |
| `activate_account` | `ActivateAccountScreen` | students only |
| `staff_home` | `StaffHomeScreen` | librarian / admin |
| `dashboard/{userType}` | `DashboardScreen` | `userType` = enum name, defaults to `STUDENT` |

Logout from either shell calls `repository.logout()` and navigates to `login` with `popUpTo(0) { inclusive = true }`, wiping the back stack.

### Inside the dashboard — `dashboard/DashboardScreen.kt`

`DashboardScreen` owns its own `NavHost` (start: `overview`) plus the chrome: top bar, bottom bar, modal drawer, logout confirm dialog. Routes are in `dashboard/DashboardRoute.kt`:

`overview`, `book_catalog`, `book_detail/{bookId}`, `book_cart`, `research_catalog`, `research_detail/{researchId}`, `borrowing_history`, `borrow_process`, `return_book`, `reserve_book`, `notifications`, `qr_login`, `printing_service`, `printing_history`, `fines`, `clearance`.

**Bottom bar:** Home · Books · Research · Activity · More (More opens the drawer). Detail/sub-routes highlight their parent tab (e.g. `book_detail` and `book_cart` → Books; `reserve_book`, `borrow_process`, `return_book` → Activity).

**Drawer** (built by `drawerItems(userType)`), grouped:
- *My library* — Overview, Book catalog, Book cart, Research & thesis
- *My activity* — Borrowing history, Reservations, Printing service (**students only**)
- *My account* — Notifications, Fines, Clearance status
- then "My QR attendance" (`qr_login` → `AttendanceScreen`), a light/dark toggle, Log out, and a profile chip.

Top-level drawer and bottom-bar taps use `popUpTo(start) { saveState = true }`, `launchSingleTop`, `restoreState` so each tab keeps its scroll position.

**Top bar:** menu, STI mark, and two icons (Messages, Notifications) that both open `notifications` and both show the unread count.

## Data loading on entry

`DashboardScreen` calls `repository.setUserType(userType)` and, keyed on `session.accountId`, runs `refreshStudentData()` once. That loads every list sequentially (see [Data layer](data-layer.md#refresh)). The catalogue screens and the attendance screen also load their own data when opened.
