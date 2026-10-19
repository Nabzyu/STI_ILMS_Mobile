# Web Parity Batch 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close four gaps between the STI ILMS web Student/Faculty portal and the Android app: load more than 50 records, a My books page, cart sync with the server, and the clearance deadline.

**Architecture:** Each feature follows the app's existing shape: a Gson DTO in `network/dto`, a Retrofit call in `LibraryApi`, state and a `load…` function in `LibraryRepository`, and a Compose screen under `dashboard/`. All decision logic goes in pure top-level functions so it can be JVM-tested without Android.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Retrofit + Gson, JUnit 4 (no Robolectric, no MockWebServer).

**Spec:** Web repo `JohnBazty/StiOrmocLibrary`, branch `codex/library-revision-updates`:
- Pages: `apps/web/src/features/catalog/MyBooksPage.tsx`, `catalog/BookCart.tsx`, `clearance/StudentClearancePage.tsx`, `catalog/BookCatalog.tsx`, `catalog/ResearchCatalog.tsx`, `circulation/BorrowingHistory.tsx`.
- Server: `apps/api/src/modules/circulation/circulation.service.ts` (`myBooks`, `cartState`, `history`), `catalog/book-catalog.validation.ts`, `catalog/research-catalog.validation.ts`.

Server contracts (verbatim shapes):
- `GET /api/v1/catalog/books?q&page&limit` and `GET /api/v1/catalog/research?q&page&limit`: `data` = item list, `meta.pagination` = `{page, limit, total, totalPages}`. Server caps `limit` at 100.
- `GET /api/v1/borrowing/history?page&limit`: `data` = `{summary, items, pagination: {page, limit, total, totalPages}}`.
- `GET /api/v1/borrowing/my-books`: `data` = `{items: [{transactionId, titleId|null, title, author, coverImagePath, accessionNumber, barcode, borrowDate, dueDate, returnDate, status, lostReportStatus}]}`.
- `GET /api/v1/borrow/cart-state`: `data` = `{activeTitleIds: number[]}`, the titles with a Pending/Borrowed/Overdue loan or a pending/approved/ready_for_pickup reservation.
- `GET /api/v1/clearance/me`: adds `schedule` = `{termId|null, academicYear?, termName?, deadlineAt: ISO string|null, version}` or `null`.

## Global Constraints

- Items 3–5 (`my-books`, `cart-state`, `clearance/me.schedule`) exist only on `codex/library-revision-updates`; the live API (`sti-ormoc-smart-library.vercel.app`) does not serve them yet. The app must degrade gracefully, never crash or block, when they are missing (404 or absent field).
- Page size stays `50` (under the server cap of `100`).
- Dates and times display in campus time, `Asia/Manila` (`com.example.stismartlib.data.CampusZone`).
- Copy is taken verbatim from the web where it exists (quoted in each task).
- Tests: JUnit only, in `app/src/test/java/com/example/stismartlib/`.
- Verify every task with `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug` (expect `BUILD SUCCESSFUL`, no failures).
- UI tasks are visually checked with the `android-visual-verification` skill in light **and** dark. Always target the emulator with `adb -s emulator-5554`, because the user's phone may also be connected.
- One commit per task; message ends with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Do not push.

## Review Focus

1. **Server without the new endpoints (live API today):** My books shows the error card with "Try again", the cart stays exactly as it was, and Clearance shows "Your current library requirements are listed below." Tested in Task 2 (error mapping is UI), Task 3 (`cartAfterSync(…, null)`), and Task 4 (`clearanceScheduleLine(null)`).
2. **New search while more pages exist:** "Load more" must fetch the next page of the *current* query, never mix results from an older query. Tested in Task 1 (`PageCursor.next` keeps `query`; `loadBooks(query)` resets the cursor).
3. **The same record on two pages** (rows shift between requests): no duplicate rows. Tested in Task 1 (`mergePage` drops repeats by key).
4. **Deadline sent as a UTC timestamp:** it must show in Philippine time. Tested in Task 4 (`"2026-10-30T09:00:00.000Z"` shows as `October 30, 2026 at 5:00 PM`).
5. **A title borrowed several times, or with no `titleId`:** it shows once, with its most recent record; a row without a `titleId` is keyed by title. Tested in Task 2 (`myBooksCollection`).

---

### Task 1: Load more for catalog, research, and borrowing history

**Files:**
- Create: `app/src/main/java/data/Paging.kt`
- Modify: `app/src/main/java/network/dto/AuthDtos.kt` (`ApiEnvelope.meta`), `app/src/main/java/network/dto/LibraryDtos.kt` (`BorrowingHistoryDataDto`), `app/src/main/java/network/ApiClient.kt`, `app/src/main/java/data/LibraryRepository.kt` (`loadBooks`, `loadResearch`, `loadBorrowingHistory`, `clearLocalData`)
- Modify UI: `app/src/main/java/dashboard/catalog/BookCatalogScreen.kt` (grid and list modes), `app/src/main/java/dashboard/research/ResearchCatalogScreen.kt`, `app/src/main/java/dashboard/history/BorrowingHistoryScreen.kt`
- Test: `app/src/test/java/com/example/stismartlib/PagingTest.kt`

**Interfaces:**
- Produces, in `data/Paging.kt`:
  - `data class PageCursor(val page: Int = 0, val totalPages: Int = 0, val query: String? = null)` with `val hasMore: Boolean` (`page in 1 until totalPages`) and `fun next(): PageCursor` (page + 1, same `query`).
  - `fun <T, K> mergePage(existing: List<T>, next: List<T>, key: (T) -> K): List<T>`, which appends `next` and drops items whose key is already present.
- Produces, in `network/dto`: `data class PaginationDto(val page: Int? = null, val limit: Int? = null, val total: Int? = null, val totalPages: Int? = null)` and `data class PageMetaDto(val pagination: PaginationDto? = null)`. `ApiEnvelope.meta` changes from `Any?` to `PageMetaDto?` (nothing reads `meta` today). `BorrowingHistoryDataDto` gains `val pagination: PaginationDto? = null`.
- Produces, in `ApiClient`: `fun <T> unwrapPage(response: Response<ApiEnvelope<T>>): Pair<T, PaginationDto?>`, which unwraps like `unwrap` and also returns `meta?.pagination`.
- Produces, in `LibraryRepository`: state `booksCursor`, `researchCursor`, `historyCursor` (`PageCursor`, `private set`) and `suspend fun loadMoreBooks()`, `loadMoreResearch()`, `loadMoreBorrowingHistory()`. The existing `loadBooks(query)`, `loadResearch(query)` and `loadBorrowingHistory()` keep their signatures, load page 1, and reset their cursor. Callers in `DashboardOverviewScreen` and `GlobalSearchScreen` need no change.

- [ ] **Step 1: Write the failing tests**

```kotlin
class PagingTest {
    @Test fun hasMoreOnlyBeforeLastPage() {
        assertTrue(PageCursor(page = 1, totalPages = 3).hasMore)
        assertFalse(PageCursor(page = 3, totalPages = 3).hasMore)
        assertFalse(PageCursor().hasMore)
    }
    @Test fun nextKeepsQuery() {
        assertEquals(PageCursor(page = 2, totalPages = 3, query = "java"), PageCursor(1, 3, "java").next())
    }
    @Test fun mergeDropsRepeats() {
        assertEquals(listOf(1, 2, 3), mergePage(listOf(1, 2), listOf(2, 3)) { it })
    }
    @Test fun catalogMetaParses() {
        val json = """{"success":true,"data":[],"meta":{"pagination":{"page":1,"limit":50,"total":120,"totalPages":3}}}"""
        val env = Gson().fromJson<ApiEnvelope<List<BookCatalogItemDto>>>(json, object : TypeToken<ApiEnvelope<List<BookCatalogItemDto>>>() {}.type)
        assertEquals(3, ApiClient.unwrapPage(Response.success(env)).second?.totalPages)
    }
    @Test fun historyPaginationParses() {
        val json = """{"success":true,"data":{"items":[],"pagination":{"page":2,"limit":50,"total":51,"totalPages":2}}}"""
        val env = Gson().fromJson<ApiEnvelope<BorrowingHistoryDataDto>>(json, object : TypeToken<ApiEnvelope<BorrowingHistoryDataDto>>() {}.type)
        assertEquals(2, env.data?.pagination?.page)
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*PagingTest*"`
Expected: compile failure (`PageCursor` and `mergePage` are not defined).

- [ ] **Step 3: Implement `Paging.kt`, the DTO changes, and `ApiClient.unwrapPage`**

- [ ] **Step 4: Run the tests and confirm they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*PagingTest*"`
Expected: PASS.

- [ ] **Step 5: Wire the repository**

The `load…` functions call `unwrapPage` with `page = 1`, replace their list, and set the cursor to `PageCursor(1, totalPages ?: 1, query)`. The `loadMore…` functions return at once unless `cursor.hasMore`. Otherwise they fetch `cursor.next()`, append with `mergePage` keyed by `id` (catalog, research) or `transactionId` (history), and advance the cursor. `clearLocalData` resets all three cursors. History reads `data.pagination`; catalog and research read `meta.pagination`.

- [ ] **Step 6: Add the "Load more" footer to the three screens**

When `cursor.hasMore`, the last list item is a full-width `PortalButton` (`variant = Secondary`, text `"Load more"`; `"Loading…"` with `loading = true` while fetching). In the catalog grid, use `item(span = { GridItemSpan(maxLineSpan) })`. Errors go to that screen's existing error banner through `userFacingMessage`.

- [ ] **Step 7: Verify, visually check, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. Expected: `BUILD SUCCESSFUL`.
Visual check: catalog (grid and list), research, and history previews in light and dark with the footer showing. Seed `totalPages = 2` in `PreviewScaffold` through a preview-only setter, like `applyPreviewSchedule`.
Commit: `feat(paging): load more catalog, research, and history`

---

### Task 2: My books page

**Files:**
- Create: `app/src/main/java/data/MyBooks.kt`, `app/src/main/java/dashboard/mybooks/MyBooksScreen.kt`
- Modify: `app/src/main/java/network/dto/LibraryDtos.kt`, `app/src/main/java/network/LibraryApi.kt`, `app/src/main/java/data/LibraryRepository.kt`, `app/src/main/java/dashboard/DashboardRoute.kt`, `app/src/main/java/dashboard/DashboardScreen.kt`, `app/src/main/java/ui/preview/ScreenPreviews.kt` and `PreviewScaffold.kt`
- Test: `app/src/test/java/com/example/stismartlib/MyBooksTest.kt`

**Interfaces:**
- Produces: `data class MyBooksDto(val items: List<BorrowingItemDto>? = null)` (reuse the existing `BorrowingItemDto`; its fields match), and `@GET("api/v1/borrowing/my-books") suspend fun myBooks(): Response<ApiEnvelope<MyBooksDto>>`.
- Produces, in `data/MyBooks.kt`:
  - `data class MyBook(val key: String, val title: String, val author: String, val status: String, val borrowedOn: String, val dueOn: String?, val returnedOn: String?)`, with dates as `yyyy-MM-dd` (`take(10)`).
  - `fun myBooksCollection(items: List<BorrowingItemDto>): List<MyBook>`: keep only rows with a `borrowDate` and status in `Borrowed`, `Overdue`, `Returned`, `Lost`; one entry per `titleId` (or `title` when `titleId` is null), keeping the latest `borrowDate`; sorted by `borrowedOn` descending.
  - `fun filterMyBooks(books: List<MyBook>, query: String): List<MyBook>`: case-insensitive match on `"$title $author"`; a blank query returns everything.
- Produces, in `LibraryRepository`: `val myBooks = mutableStateListOf<MyBook>()`, `var myBooksError by mutableStateOf<String?>(null)` (`private set`), and `suspend fun loadMyBooks()`. On failure it sets `myBooksError = userFacingMessage(error, "Your book collection could not be loaded.")` and rethrows. Clear both in `clearLocalData`.
- Produces: `DashboardRoute.MyBooks` = `"my_books"`; drawer item `DrawerItem(DashboardRoute.MyBooks.route, "My books", Lucide.LibraryBig)` right after "Reservations", added to `stacksOverTabs`.

- [ ] **Step 1: Write the failing tests**

```kotlin
class MyBooksTest {
    private fun row(titleId: Int?, title: String, borrow: String?, status: String) =
        BorrowingItemDto(titleId = titleId, title = title, author = "A", borrowDate = borrow, status = status)

    @Test fun oneEntryPerTitleWithLatestBorrow() {
        val books = myBooksCollection(listOf(
            row(7, "Clean Code", "2026-09-01T02:00:00.000Z", "Returned"),
            row(7, "Clean Code", "2026-10-05T02:00:00.000Z", "Borrowed"),
        ))
        assertEquals(1, books.size); assertEquals("Borrowed", books[0].status); assertEquals("2026-10-05", books[0].borrowedOn)
    }
    @Test fun skipsPendingCancelledAndUnborrowed() {
        assertTrue(myBooksCollection(listOf(row(1, "X", null, "Borrowed"), row(2, "Y", "2026-10-01", "Pending"), row(3, "Z", "2026-10-01", "Cancelled"))).isEmpty())
    }
    @Test fun missingTitleIdKeysByTitle() {
        assertEquals(2, myBooksCollection(listOf(row(null, "A", "2026-10-01", "Lost"), row(null, "B", "2026-10-02", "Returned"))).size)
    }
    @Test fun newestFirstAndSearch() {
        val books = myBooksCollection(listOf(row(1, "Algorithms", "2026-10-01", "Returned"), row(2, "Databases", "2026-10-03", "Borrowed")))
        assertEquals(listOf("Databases", "Algorithms"), books.map { it.title })
        assertEquals(listOf("Algorithms"), filterMyBooks(books, "ALGO").map { it.title })
        assertEquals(2, filterMyBooks(books, " ").size)
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*MyBooksTest*"`
Expected: compile failure (`myBooksCollection` is not defined).

- [ ] **Step 3: Implement `data/MyBooks.kt`, the DTO, and the API call**

- [ ] **Step 4: Run the tests and confirm they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*MyBooksTest*"`
Expected: PASS.

- [ ] **Step 5: Build `MyBooksScreen()` and wire the route and drawer**

The layout is `ElmsPullRefresh` (refresh = `loadMyBooks()`) around a `LazyColumn`, which loads once in `LaunchedEffect(Unit)` when `myBooks` is empty:
- `PageHeader(eyebrow = "My library", title = "My books", description = "Your personal collection of books you have borrowed. Each title appears once, with your most recent borrowing record.")`.
- `PortalField(label = "Search my books", placeholder = "Book title or author")`.
- One `SectionCard` per book: title (bold), author, a `StatusBadge(status, warning = status is Overdue or Lost)`, `"Last borrowed ${formatDateMedium(borrowedOn)}"`, and `"Due ${formatDateMedium(dueOn)}"` when the status is `Borrowed` or `Overdue`. Rows are not clickable; the web drawer is out of scope.
- States:
  - error with an empty list: `ElmsEmptyState(title = "My books unavailable", description = "Your book collection is temporarily unavailable.", actionLabel = "Try again")`.
  - empty: `"You have not borrowed any books yet."`
  - no search match: `"No books match your search."`

- [ ] **Step 6: Verify, visually check, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. Expected: `BUILD SUCCESSFUL`.
Visual check: add `MyBooksScreenPreview` and `MyBooksScreenDarkPreview` (seed 2 books in `PreviewScaffold`), plus the error state, in light and dark.
Commit: `feat(my-books): personal collection page`

---

### Task 3: Cart sync with the server

**Files:**
- Create: `app/src/main/java/data/CartSync.kt`
- Modify: `app/src/main/java/network/dto/LibraryDtos.kt`, `app/src/main/java/network/LibraryApi.kt`, `app/src/main/java/data/LibraryRepository.kt`, `app/src/main/java/dashboard/catalog/BookCartScreen.kt`
- Test: `app/src/test/java/com/example/stismartlib/CartSyncTest.kt`

**Interfaces:**
- Produces: `data class CartStateDto(val activeTitleIds: List<Long>? = null)` and `@GET("api/v1/borrow/cart-state") suspend fun cartState(): Response<ApiEnvelope<CartStateDto>>`.
- Produces, in `data/CartSync.kt`: `fun cartAfterSync(cartTitleIds: List<String>, activeTitleIds: List<Long>?): Set<String>`, which returns the cart ids to remove. A null input (the server check was unavailable) returns an empty set.
- Produces, in `LibraryRepository`: `suspend fun syncCartWithServer(): Int`, which returns how many items it removed. It catches `ApiException` and `IOException` and treats them as `activeTitleIds = null`, so the cart stays unchanged.

- [ ] **Step 1: Write the failing tests**

```kotlin
class CartSyncTest {
    @Test fun removesTitlesAlreadyActive() = assertEquals(setOf("7"), cartAfterSync(listOf("7", "9"), listOf(7L, 12L)))
    @Test fun keepsCartWhenCheckUnavailable() = assertEquals(emptySet<String>(), cartAfterSync(listOf("7"), null))
    @Test fun ignoresNonNumericIds() = assertEquals(emptySet<String>(), cartAfterSync(listOf("preview-1"), listOf(1L)))
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*CartSyncTest*"`
Expected: compile failure.

- [ ] **Step 3: Implement `cartAfterSync`, the DTO, the API call, and `syncCartWithServer`**

- [ ] **Step 4: Run the tests and confirm they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*CartSyncTest*"`
Expected: PASS.

- [ ] **Step 5: Call the sync from `BookCartScreen`**

Use `LaunchedEffect(Unit)`: when the cart is not empty and no submit is in progress, call `syncCartWithServer()`. If it returns more than 0, show `ElmsBanner(tone = Info)` with the web copy verbatim: `"Removed books already requested or checked out. You can find them in My books or Borrowing history."`

- [ ] **Step 6: Verify, visually check, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. Expected: `BUILD SUCCESSFUL`.
Visual check: the cart preview with the notice shown, in light and dark. Seed the notice through a preview parameter (`initialSyncNotice: String? = null`).
Commit: `feat(cart): drop titles already requested or borrowed`

---

### Task 4: Clearance deadline

**Files:**
- Create: `app/src/main/java/data/ClearanceSchedule.kt`
- Modify: `app/src/main/java/network/dto/ClearanceDtos.kt`, `app/src/main/java/data/models/LibraryModels.kt` (`ClearanceStanding`), `app/src/main/java/data/LibraryRepository.kt` (`toClearanceStanding`), `app/src/main/java/dashboard/account/ClearanceScreen.kt`
- Test: `app/src/test/java/com/example/stismartlib/ClearanceScheduleTest.kt`

**Interfaces:**
- Produces: `data class ClearanceScheduleDto(val termId: Long? = null, val academicYear: String? = null, val termName: String? = null, val deadlineAt: String? = null)` and a `schedule: ClearanceScheduleDto? = null` field on `ClearanceRecordDto`. Gson can't tell an absent field from `null`; both mean "no schedule".
- Produces, in `data/ClearanceSchedule.kt`:
  - `fun clearanceScheduleLine(schedule: ClearanceScheduleDto?): String`, using the web copy:
    - `null` → `"Your current library requirements are listed below."`
    - a parseable `deadlineAt` → `"Clearance deadline: <MMMM d, yyyy 'at' h:mm a in Asia/Manila> (Philippine time)"`
    - otherwise → `"No schedule yet for clearance day."`
  - `fun isPastClearanceDeadline(schedule: ClearanceScheduleDto?, now: Instant): Boolean`.
- Produces: `ClearanceStanding` gains `val scheduleLine: String` and `val pastDeadline: Boolean` (computed in `toClearanceStanding` with `Instant.now()`).

- [ ] **Step 1: Write the failing tests**

```kotlin
class ClearanceScheduleTest {
    @Test fun noSchedule() = assertEquals("Your current library requirements are listed below.", clearanceScheduleLine(null))
    @Test fun deadlineInPhilippineTime() = assertEquals(
        "Clearance deadline: October 30, 2026 at 5:00 PM (Philippine time)",
        clearanceScheduleLine(ClearanceScheduleDto(termId = 3, deadlineAt = "2026-10-30T09:00:00.000Z")))
    @Test fun scheduleWithoutDeadline() = assertEquals("No schedule yet for clearance day.", clearanceScheduleLine(ClearanceScheduleDto(termId = 3)))
    @Test fun pastDeadline() {
        val s = ClearanceScheduleDto(deadlineAt = "2026-10-30T09:00:00.000Z")
        assertTrue(isPastClearanceDeadline(s, Instant.parse("2026-10-30T09:00:01Z")))
        assertFalse(isPastClearanceDeadline(s, Instant.parse("2026-10-30T08:59:59Z")))
        assertFalse(isPastClearanceDeadline(null, Instant.now()))
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*ClearanceScheduleTest*"`
Expected: compile failure.

- [ ] **Step 3: Implement `ClearanceSchedule.kt` and the DTO field**

Use `DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.US)` with `CampusZone`. An unparseable `deadlineAt` counts as no deadline.

- [ ] **Step 4: Run the tests and confirm they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*ClearanceScheduleTest*"`
Expected: PASS.

- [ ] **Step 5: Show the line on `ClearanceScreen`**

Right under the `PageHeader`, add `ElmsBanner(message = standing.scheduleLine, tone = if (standing.pastDeadline && !standing.isCleared) Error else Info)`.

- [ ] **Step 6: Verify, visually check, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. Expected: `BUILD SUCCESSFUL`.
Visual check: the Clearance preview in light and dark, with a deadline seeded in `PreviewScaffold`'s clearance sample.
Commit: `feat(clearance): show the clearance deadline`

---

## After all tasks

- Fast-forward the user's `main` checkout (`C:\Users\Ethan\AndroidStudioProjects\STI ILMS`) to the branch. It currently has no uncommitted app changes, only `.idea/`, `.cursor/`, `ponytail/` and an image, so `git merge --ff-only` is enough. Re-check `git status` first.
- Items 3–5 can only be checked end to end after John deploys `codex/library-revision-updates`. Until then, confirm against the live API that each one degrades as Review Focus #1 says.
