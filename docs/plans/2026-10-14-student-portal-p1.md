# Student portal P1 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Load `.claude/skills/student-portal-p0/SKILL.md` patterns + `api-route-deploy-gap` for Profile. Design: `docs/plans/2026-10-14-student-portal-p1-design.md`.

**Goal:** Finish Student/Faculty P0+P1 gaps — Profile post-deploy smoke, notification actions, cancel pending borrow, print receipts, report lost book, shelf location text.

**Architecture:** Extend `LibraryApi` / DTOs / `LibraryRepository`; wire existing Compose screens; `userFacingMessage` everywhere.

**Tech Stack:** Kotlin, Retrofit, Compose, JUnit, STI design system.

---

### Task 0: Profile deploy gate (no feature rewrite)

**Files:**
- Read: `docs/bugs/2026-10-14-profile-deploy-checklist.md`
- Skill: `.claude/skills/api-route-deploy-gap/SKILL.md`

**Step 1:** Confirm production still lacks mounts (or re-probe after deploy).

**Step 2:** When mounted — smoke My Profile load/save/avatar on emulator.

**Step 3:** Only then fix DTO/mapping bugs if smoke fails. Do not rebuild Profile UI otherwise.

**Step 4:** Mark Profile done in gap canvas notes when smoke passes.

---

### Task 1: Notification actions — API + repository

**Files:**
- Modify: `app/src/main/java/network/LibraryApi.kt`
- Modify: `app/src/main/java/data/LibraryRepository.kt`
- Test: optional thin test if response parsing is non-trivial

**Step 1: Add API**

```kotlin
@PATCH("api/v1/notifications/{id}/read")
suspend fun markNotificationRead(@Path("id") id: Int): Response<ApiEnvelope<Any>>

@PATCH("api/v1/notifications/read-all")
suspend fun markAllNotificationsRead(): Response<ApiEnvelope<Any>>

@DELETE("api/v1/notifications/{id}")
suspend fun deleteNotification(@Path("id") id: Int): Response<ApiEnvelope<Any>>

@DELETE("api/v1/notifications")
suspend fun deleteAllNotifications(): Response<ApiEnvelope<Any>>
```

**Step 2: Repository**

- `markNotificationRead(id)`, `markAllNotificationsRead()`, `deleteNotification(id)`, `clearAllNotifications()`
- Optimistic local update or reload via `loadNotifications()`
- Errors → `userFacingMessage`

**Step 3:** Compile.

---

### Task 2: Notification actions — UI

**Files:**
- Modify: `app/src/main/java/dashboard/notifications/NotificationsScreen.kt`

**Step 1:** “Mark all read” / “Clear all” when list non-empty (confirm for clear all).

**Step 2:** Per-row: mark read (if unread), delete (confirm).

**Step 3:** Visual verification.

---

### Task 3: Cancel pending borrow — API + repository

**Files:**
- Modify: `LibraryApi.kt`, `LibraryRepository.kt`
- Ensure `BorrowingRecord.id` is transaction id usable for cancel

**Step 1: API**

```kotlin
@PUT("api/v1/circulation/requests/{transactionId}/cancel")
suspend fun cancelBorrowRequest(
    @Path("transactionId") transactionId: Int,
    @Body body: CancelBorrowRequestDto? = null,
): Response<ApiEnvelope<Any>>
```

**Step 2:** `cancelBorrowRequest(transactionId, reason?)` → reload history.

**Step 3:** Compile.

---

### Task 4: Cancel pending borrow — UI

**Files:**
- Modify: `app/src/main/java/dashboard/history/BorrowingHistoryScreen.kt` (and Return/Borrow screens only if they list PENDING)

**Step 1:** Cancel button when `borrowStatus == PENDING`.

**Step 2:** Confirm dialog (+ optional reason field).

**Step 3:** Visual verification.

---

### Task 5: Print receipts — DTOs + API + repository

**Files:**
- Create/modify printing DTOs for receipts
- Modify: `LibraryApi.kt`, `LibraryRepository.kt`
- Test: `PrintingReceiptDtoParsingTest.kt`

**Step 1: Endpoints**

- `GET api/v1/printing/receipts`
- `GET api/v1/printing/receipts/{id}`
- `GET api/v1/printing/receipts/{id}/pdf` → `ResponseBody`

**Step 2:** Repository state `printReceipts`, `loadPrintReceipts()`, `loadPrintReceiptPdf(id): ByteArray`.

**Step 3:** Unit test parse; compile.

---

### Task 6: Print receipts — UI

**Files:**
- Modify: `PrintingHistoryScreen.kt` and/or new `PrintingReceiptsSection`
- Optional detail dialog/sheet

**Step 1:** List receipts under history (or tab/section).

**Step 2:** View details; Download/Share PDF via Intent.

**Step 3:** Visual verification.

---

### Task 7: Report lost book — API + repository + UI

**Files:**
- Modify: `LibraryApi.kt`, `LibraryRepository.kt`
- Modify: `ClearanceScreen.kt` and/or `BorrowingHistoryScreen.kt`

**Step 1: API**

```kotlin
@POST("api/v1/clearance/lost-books/{transactionId}/report")
suspend fun reportLostBook(@Path("transactionId") transactionId: Int): Response<ApiEnvelope<Any>>
```

**Step 2:** `reportLostBook(transactionId)` → `loadClearance()` (+ history).

**Step 3:** Confirm UI on active loan rows; banner on success/error.

**Step 4:** Visual verification.

---

### Task 8: Shelf location (light) — polish only

**Files:**
- Modify: `BookDetailScreen.kt`, `ResearchDetailScreen.kt`, book mapper if call number not on model
- Note: Shelf line already exists on both detail screens

**Step 1:** Confirm shelf shows real catalog values (not stuck on "—").

**Step 2:** Add call number on book detail when DTO/`Book` has it.

**Step 3:** No floor-plan API. Visual check empty vs populated.

---

### Task 9: Verification + gap canvas

**Step 1:**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.*Receipt*" --tests "com.example.stismartlib.*Notification*" --console=plain
```

**Step 2: Manual checklist**

- [ ] Notifications mark/delete
- [ ] Cancel pending borrow
- [ ] Print receipt list + PDF share
- [ ] Report lost book updates clearance
- [ ] Book/research detail shows shelf text
- [ ] Profile only if deploy completed

**Step 3:** Update `canvases/web-vs-app-feature-gaps.canvas.tsx` statuses when features ship.

---

## Done when

- [ ] All six scope items addressed (Profile = deploy/smoke or still gated with checklist)
- [ ] No staff/admin work
- [ ] No interactive floor map
- [ ] Compile + relevant unit tests pass
- [ ] userFacingMessage on new error paths

## Out of scope

Fine receipts/terms, `/dashboard` aggregate, attendance visit history, floor-plan map, notification schedule, staff desk.
