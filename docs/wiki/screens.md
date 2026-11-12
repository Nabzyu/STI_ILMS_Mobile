---
title: Screens
tags: [ui, screens]
---

# Screens

Back to [index](README.md). Related: [Startup and navigation](startup-and-navigation.md), [Data layer](data-layer.md), [Known gaps](known-gaps.md).

"Server" means the action calls the API; "local" means it only changes on-device state.

## Auth

| Screen | File | Does |
|---|---|---|
| Login | `auth/LoginScreen.kt` | School ID + password, show-password toggle, link to register. Server: login with portal fallback. See [Authentication](authentication-and-session.md#sign-in--one-form-two-server-portals). |
| Register | `auth/RegisterScreen.kt` | Student sign-up form with program and year-level pickers. Server: register, then back to login. |

## Staff (Librarian / Admin)

| Screen | File | Does |
|---|---|---|
| Staff home | `dashboard/staff/StaffHomeScreen.kt` | "Admin session active" card, three **"Soon"** tiles (inventory barcode scan, borrow/return confirm, attendance scan), sign-out with confirm. No librarian features are implemented yet. |

## Student / Faculty dashboard

All are hosted inside `DashboardScreen` (top bar, bottom bar, drawer).

| Route | Screen | Reads | Actions |
|---|---|---|---|
| `overview` | `dashboard/overview/DashboardOverviewScreen.kt` | session, loans, notifications, fines, print history, reservations, books | Students: hero, active loans, recommended books, unread/fines stats, printing, reservations, latest update, quick links. **Faculty get a minimal `FacultyOverview` welcome card instead.** |
| `book_catalog` | `dashboard/catalog/BookCatalogScreen.kt` | `books` | Search (server `q`, reloads on each change), category chips (client-side), cart shortcut, retry on error. |
| `book_detail/{bookId}` | `dashboard/catalog/BookDetailScreen.kt` | `books` (looked up by id) | Add to cart (local, toasts for duplicate / student limit), reserve (server). |
| `book_cart` | `dashboard/catalog/BookCartScreen.kt` | `cart` | Remove items (local), submit borrow request (server) → dialog with the server's pickup instructions. |
| `borrow_process` | `dashboard/borrow/BorrowProcessScreen.kt` | `cart` | Explains the borrow steps; links to catalogue and cart. |
| `return_book` | `dashboard/returnbook/ReturnBookScreen.kt` | active loans | Pick a loan → confirm → "Return submitted". **Local only**: nothing is sent; the user is told to bring the book to the counter. |
| `reserve_book` | `dashboard/reserve/ReserveBookScreen.kt` | `reservations` | Lists reservations with queue position; cancel (server). |
| `borrowing_history` | `dashboard/history/BorrowingHistoryScreen.kt` | `borrowingHistory`, `fines` | All / Active / Returned filter; shows outstanding fines total. |
| `research_catalog` | `dashboard/research/ResearchCatalogScreen.kt` | `research` | Search (server), list. |
| `research_detail/{researchId}` | `dashboard/research/ResearchDetailScreen.kt` | `research` (by id) | Abstract, metadata, APA citation. |
| `notifications` | `dashboard/notifications/NotificationsScreen.kt` | `notifications` | List with unread highlight; **"Mark all as read" is local only** and is undone by the next refresh. |
| `qr_login` | `dashboard/qr/AttendanceScreen.kt` | `attendancePass` | Loads the pass on open; shows the server-generated QR image, name, school ID, visits this month, today's check-in, common purpose. |
| `printing_service` | `dashboard/printing/PrintingServiceScreen.kt` | `printHistory` | Students only (drawer). New-request form with cost estimate; **submit is local only** (shows a "Request submitted" card, no API call). Recent history below. |
| `printing_history` | `dashboard/printing/PrintingHistoryScreen.kt` | `printHistory` | Full print request list. |
| `fines` | `dashboard/account/FinesScreen.kt` | `fines` | Fine history and "digital receipts" for paid fines; payment is cash at the counter. |
| `clearance` | `dashboard/account/ClearanceScreen.kt` | loans, fines | Cleared when there are no borrowed/overdue loans **and** zero unpaid fines; otherwise a red blocked banner listing what is outstanding. Computed on the device. |

### Unwired screen

`dashboard/qr/QrLoginScreen.kt` (check-in / check-out toggle with a scanner placeholder) is only used from previews. The `qr_login` route renders `AttendanceScreen`.

## Detail screens depend on the list

`BookDetailScreen` and `ResearchDetailScreen` look the item up in the already-loaded list. If the list was not loaded (or was replaced by a search that excludes the item), the detail screen has nothing to show. `GET catalog/books/{titleId}` exists in the API interface but is not used.
