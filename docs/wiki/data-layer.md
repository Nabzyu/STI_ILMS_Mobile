---
title: Data layer
tags: [data, state]
---

# Data layer

Back to [index](README.md). Related: [Networking and API](networking-and-api.md), [Screens](screens.md).

## `LibraryRepository` (`data/LibraryRepository.kt`)

One instance per process (see [Architecture](architecture.md#object-lifetime-and-wiring)). It is the app's state holder: screens read its fields directly and recompose when they change.

| State | Type | Filled by |
|---|---|---|
| `session` | `LibrarySession` | login, restore, logout |
| `isLoading`, `lastError` | `Boolean`, `String?` | `refreshStudentData()` |
| `books` | list of `Book` | `loadBooks(query)` |
| `research` | list of `Research` | `loadResearch(query)` |
| `borrowingHistory` | list of `BorrowingRecord` | `loadBorrowingHistory()` |
| `reservations` | list of `Reservation` | `loadReservations()` |
| `fines` | list of `FineRecord` | `loadFines()` |
| `notifications` | list of `NotificationItem` | `loadNotifications()` |
| `printHistory` | list of `PrintRequest` | `loadPrintHistory()` |
| `attendancePass` | `AttendancePass?` | `loadAttendancePass()` |
| `cart` | list of `CartItem` | local only |

Every `load*` runs on `Dispatchers.IO`, calls the API, then **clears and replaces** the list. State is written from the IO thread; Compose snapshot state tolerates this, but it is not formally main-thread confined.

### Refresh

`refreshStudentData()` runs the eight loaders **one after another** (books → research → history → reservations → fines → notifications → print history → attendance pass). The first failure stops the chain, sets `lastError` and rethrows, so later lists stay empty. `DashboardScreen` wraps the call in `runCatching`. Screens show "loading" when `isLoading && list.isEmpty()` and an error banner when `lastError != null && list.isEmpty()`.

### Actions

| Function | API | Then |
|---|---|---|
| `submitBorrowCart()` | `POST borrow/submit-request` with the cart's title ids | clears cart, reloads history, returns the server's `instructions` (or a default message) |
| `reserveBook(book)` | `POST catalog/books/{id}/reservations` | reloads reservations |
| `cancelReservation(id)` | `PUT reservations/{id}/cancel` | reloads reservations |

### Cart rules

- Students can hold **2** books (`cartLimit()`); faculty have no limit.
- `addToCart` returns `Success`, `AlreadyInCart` or `LimitReached`.
- An item records `isAvailable = availableCopies > 0` at the time it was added; checkout is enabled only when every item is available (`allCartItemsAvailable()`).
- The cart is in memory only — it is lost on process death and on logout.

## Domain models (`data/models/LibraryModels.kt`)

`Book`, `Research`, `BorrowingRecord`, `Reservation`, `FineRecord`, `NotificationItem`, `PrintRequest`, `CartItem`, `LibrarySession`, `AttendancePass`, plus enums:

- `BorrowStatus`: Pending, Borrowed, Returned, Overdue, Cancelled
- `FineStatus`: No Fine, Unpaid, Paid
- `PrintStatus`: Pending, Printing, Ready for Pickup, Completed, Cancelled
- `NotificationCategory`: Books, Printing, Reminders, General

Helpers: `Book.apaCitation()`, `Research.apaCitation()` (school name defaults to "STI College"), `Book.availabilityLabel()` ("N of M available" / "Unavailable").

IDs are strings in the domain model even though the API uses integers; they are parsed back with `toIntOrNull()` when calling the API.

## Mapping

Notable conversions when turning DTOs into models:

- **Dates:** `formatDate()` keeps the first 10 characters (`YYYY-MM-DD`); missing dates become "—". Screens then format with `ui/format/LibraryDates.kt` (`formatDateShort` → "Sep 15", `formatDateMedium` → "Sep 15, 2026", raw string on parse failure).
- **Books:** missing category → "General"; missing publisher/shelf → "—"; `description` is filled with the server's *availability status*, not a real description.
- **Borrow status:** lower-cased server string → enum; anything unrecognised becomes `BORROWED`. `fineStatus` is always `NONE` (fines are a separate list).
- **Fines:** unpaid when `balance > 0` or status contains "unpaid" or equals "Open"; amount = balance, else assessed.
- **Notifications:** category guessed from the `type` string (contains "print" / "book" / "remind").
- **Print requests:** `isColored` when print type contains "color"; `note` holds the *paper size*; `amountPaid` is the full cost when payment status is "Paid", else 0.
- **Attendance pass:** profile and summary fields fall back to the current session; the QR PNG is fetched separately and decoded to a `Bitmap` (failures leave it null).
- Borrowing-history and reservation rows embed a mostly empty `Book` (only id, title, author).
