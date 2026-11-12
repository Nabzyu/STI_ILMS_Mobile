# Student portal P0 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Also load `.claude/skills/student-portal-p0/SKILL.md`.

**Goal:** Ship real Student/Faculty printing (quote/submit/cancel/history) and official clearance status; document Profile deploy gate without Profile feature coding.

**Architecture:** Extend `LibraryApi` + DTOs + `LibraryRepository`; replace fake print submit and heuristic clearance UI; reuse Portal/Elms + `userFacingMessage`.

**Tech Stack:** Kotlin, Retrofit multipart, Jetpack Compose, JUnit, existing STI design system.

**Skill pack:** `.claude/skills/student-portal-p0/` (+ `api-route-deploy-gap` for Profile).

---

### Task 0: Confirm skill pack loaded

**Files:**
- Read: `.claude/skills/student-portal-p0/SKILL.md`
- Read: `.claude/skills/student-portal-p0/product-anchors.md`
- Read: `docs/plans/2026-10-14-student-portal-p0-design.md`

**Step 1:** Confirm scope matches design (no staff, no receipts, no Profile code).

---

### Task 1: Printing DTOs + API

**Files:**
- Modify: `app/src/main/java/network/dto/LibraryDtos.kt` (or new `PrintingDtos.kt`)
- Modify: `app/src/main/java/network/LibraryApi.kt`
- Create: `app/src/test/java/com/example/stismartlib/PrintingDtoParsingTest.kt`

**Step 1: Write failing tests** for camelCase/snake_case parse of quote + request + service status.

**Step 2: Add DTOs**

- `PrintServiceStatusDto` — `accepting_requests` / `acceptingRequests`, `unavailable_reason`, `docx_auto_count_available`
- `PrintQuoteDto` — `page_count`, `total_sheets`, `calculated_cost`, `document_sha256`, `printable_file_name`
- Extend `PrintRequestDto` if needed for cancel eligibility fields
- Use `@SerializedName` + `alternate` for camelCase

**Step 3: LibraryApi**

```kotlin
@GET("api/v1/printing/service-status")
suspend fun printServiceStatus(): Response<ApiEnvelope<PrintServiceStatusDto>>

@Multipart
@POST("api/v1/printing/quote")
suspend fun quotePrint(...): Response<ApiEnvelope<PrintQuoteDto>>

@Multipart
@POST("api/v1/printing/requests")
suspend fun submitPrintRequest(...): Response<ApiEnvelope<PrintRequestDto>>

@PUT("api/v1/printing/requests/{requestId}/cancel")
suspend fun cancelPrintRequest(@Path("requestId") requestId: Int): Response<ApiEnvelope<Any>>
```

Existing `printRequests()` stays.

**Step 4:** Run `PrintingDtoParsingTest` — expect PASS.

---

### Task 2: Repository printing actions

**Files:**
- Modify: `app/src/main/java/data/LibraryRepository.kt`
- Modify: `app/src/main/java/data/models/` if PrintRequest model needs requestId for cancel

**Step 1:** Add state: `printServiceAccepting`, `printUnavailableReason`, quote cache optional.

**Step 2:** Implement:

- `loadPrintServiceStatus()`
- `quotePrint(uri/bytes, copies, type, paper)`
- `submitPrint(...)` using quote fields
- `cancelPrint(requestId)`
- Keep `loadPrintHistory()`; refresh after submit/cancel

**Step 3:** Map all errors with `userFacingMessage`.

**Step 4:** `.\gradlew.bat :app:compileDebugKotlin --console=plain`

---

### Task 3: Printing UI

**Files:**
- Modify: `app/src/main/java/dashboard/printing/PrintingServiceScreen.kt`
- Modify: `app/src/main/java/dashboard/printing/PrintingHistoryScreen.kt` (cancel action)

**Step 1:** File picker via `GetContent` / `OpenDocument` (PDF + DOCX MIME).

**Step 2:** Debounced quote on file/options change (mirror web ~250ms).

**Step 3:** Submit enabled only when accepting + quote ready; remove local fake success.

**Step 4:** History: Cancel for Pending + Unpaid.

**Step 5:** Visual verification skill after UI changes.

---

### Task 4: Clearance DTOs + API + repository

**Files:**
- Create/modify DTOs for clearance record
- Modify: `LibraryApi.kt` — `GET api/v1/clearance/me`
- Modify: `LibraryRepository.kt` — `loadClearance()`, `clearance` state
- Create: `ClearanceDtoParsingTest.kt`

**Step 1:** Failing parse test from sample JSON shaped like web `ClearanceRecord`.

**Step 2:** Implement API + repository.

**Step 3:** Compile.

---

### Task 5: Clearance UI

**Files:**
- Modify: `app/src/main/java/dashboard/account/ClearanceScreen.kt`

**Step 1:** Load on enter; show loading/error via ElmsBanner.

**Step 2:** Render API status/reason/summary/lists; delete local cleared heuristic.

**Step 3:** Visual verification.

---

### Task 6: Profile deploy checklist (docs only)

**Files:**
- Create: `docs/bugs/2026-10-14-profile-deploy-checklist.md` (or section already in design — keep one short checklist file)

**Step 1:** Write merge/deploy/probe/smoke steps (no Profile Kotlin changes).

**Step 2:** Link `api-route-deploy-gap` skill.

---

### Task 7: Verification

**Step 1:**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.PrintingDtoParsingTest" --tests "com.example.stismartlib.ClearanceDtoParsingTest" --console=plain
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

**Step 2: Manual**

- [ ] Service closed → cannot submit
- [ ] Quote → submit → row in history
- [ ] Cancel pending unpaid
- [ ] Clearance shows API Cleared/Not Cleared
- [ ] Profile still documented as deploy-blocked (no false “fixed”)

---

## Done when

- [ ] Fake print submit gone; real multipart path works
- [ ] Clearance no longer guesses from local lists alone
- [ ] Profile checklist exists; no premature Profile “done”
- [ ] Unit tests + compile pass
- [ ] Staff code untouched except accidental shared helpers

## Out of scope

Staff/admin, print receipts, lost-book report, floor plan, notification mark-read, Profile UI until deploy.
