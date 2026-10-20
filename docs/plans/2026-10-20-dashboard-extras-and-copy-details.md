# Dashboard Extras and Copy Details Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close two more web-to-app gaps. The dashboard shows how full the library is and the current library announcement. Book detail shows the physical copy's condition, accession number, shelf, and barcode.

**Architecture:** Same shape as the earlier parity work: a Gson DTO, a Retrofit call, a `load…` function or repository extension, and Compose UI. Decision logic (percentages, data-URI decoding, mapping) goes in pure top-level functions with JVM tests. Both features use endpoints the **live API already serves** (`sti-ormoc-smart-library.vercel.app` web main build calls them).

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Retrofit + Gson, Coil 2.7.0, JUnit 4 (no Robolectric, no MockWebServer).

**Spec:** Web repo `JohnBazty/StiOrmocLibrary`. The shapes below are identical on `main` (live) and `codex/library-revision-updates`:
- `apps/api/src/modules/dashboard/dashboard.repository.ts` (`user()`), mounted as `GET /api/v1/dashboard` for Student and Faculty.
- `apps/api/src/modules/catalog/asset-code.service.ts` (`catalogAsset`), mounted as `GET /api/v1/catalog/copies/:barcode`.
- `apps/api/src/modules/catalog/book-label.renderer.ts`: `barcodeImageData` is `data:image/svg+xml;base64,<svg>`.
- Web UI: `apps/web/src/features/dashboard/UserDashboardPage.tsx` and `apps/web/src/features/catalog/BookDetailDrawer.tsx`.

Server contracts (only the fields this plan uses; Gson ignores the rest):
- `GET /api/v1/dashboard` → `data.occupancy = {current: number, capacity: number}` (capacity is at least 1 on the server). `data.announcement = {id, title, message, priority, publishedAt} | null`. `priority` is free text such as `Normal`, `Important` or `Urgent`. `publishedAt` is preformatted, e.g. `2026-10-20 09:15 AM`.
- `GET /api/v1/catalog/copies/{barcode}` → `data = {physicalCopyId, titleId, title, author, accessionNumber, barcode, shelfLocation, conditionStatus|null, barcodeImageData}`. An unknown barcode returns `404 BOOK_ASSET_NOT_FOUND`.
- The book's barcode is `previewBarcode` (string or null) on `GET /api/v1/catalog/books` and `GET /api/v1/catalog/books/{titleId}`. `BookCatalogItemDto.previewBarcode` already parses it.

## Global Constraints

- Web copy, verbatim:
  - occupancy label `"Library occupancy"`
  - announcement eyebrow `"Library announcement · <priority>"`
  - copy section heading `"Barcode"`
  - field labels `"Book location"` and `"Current condition"`
  - missing value `"Not recorded"`
- A failure of either new call never blocks or crashes its screen. The dashboard simply hides the extras; book detail shows `"Copy details are unavailable right now."` in that section.
- New dependency allowed: `io.coil-kt:coil-svg` at the same version as `coil-compose` (`coil = "2.7.0"` in `gradle/libs.versions.toml`). No other new dependency.
- Tests: JUnit only, in `app/src/test/java/com/example/stismartlib/`.
- Verify each task with `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug` (`BUILD SUCCESSFUL`, no failures).
- Visual check with the `android-visual-verification` skill, light **and** dark, always `adb -s emulator-5554`.
- This project has a graphify graph: run `graphify query`/`explain` before reading source, per `CLAUDE.md`.
- One commit per task; message ends with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Do not push.

## Review Focus

1. **Occupancy over capacity, or capacity 0** (bad data, someone not logged out): the bar must stay within 0–100% and nothing divides by zero. Tested in Task 1 (`occupancyPercent`).
2. **No announcement published** (`announcement: null`): no banner, and no empty yellow card. Tested in Task 1 (`DashboardDtoTest.nullAnnouncement`).
3. **A book with no physical copy** (`previewBarcode` null or blank): no copy request is made and the section is hidden, not shown as an error. Tested in Task 2 (`toBook` maps blank to null).
4. **Unknown barcode (404) or server failure** for the copy: the book detail still works and the section reads "Copy details are unavailable right now." This is UI wiring; check it with a preview.
5. **A barcode image that is not an SVG data URI, or is malformed base64:** decoding returns null and the section shows the barcode as text only. Tested in Task 2 (`svgBytesFromDataUri`).

---

### Task 1: Dashboard occupancy and announcement

**Files:**
- Create: `app/src/main/java/data/DashboardSummary.kt`, `app/src/main/java/dashboard/overview/LibraryPulseCards.kt`
- Modify: `app/src/main/java/network/dto/LibraryDtos.kt`, `app/src/main/java/network/LibraryApi.kt`, `app/src/main/java/data/LibraryRepository.kt` (state, `refreshStudentData`, `clearLocalData`, preview setter), `app/src/main/java/dashboard/overview/DashboardOverviewScreen.kt` (student overview, `FacultyOverview`, pull-to-refresh), `app/src/main/java/ui/preview/PreviewScaffold.kt`
- Test: `app/src/test/java/com/example/stismartlib/DashboardSummaryTest.kt`

**Interfaces:**
- Produces, in `network/dto`:
  - `UserDashboardDto(val occupancy: OccupancyDto? = null, val announcement: AnnouncementDto? = null)`
  - `OccupancyDto(val current: Int? = null, val capacity: Int? = null)`
  - `AnnouncementDto(val id: Long? = null, val title: String? = null, val message: String? = null, val priority: String? = null, val publishedAt: String? = null)`
  - `@GET("api/v1/dashboard") suspend fun userDashboard(): Response<ApiEnvelope<UserDashboardDto>>`
- Produces, in `data/DashboardSummary.kt`:
  - `data class DashboardSummary(val occupancyCurrent: Int, val occupancyCapacity: Int, val announcement: LibraryAnnouncement?)`
  - `data class LibraryAnnouncement(val title: String, val message: String, val priority: String, val publishedAt: String)`
  - `fun UserDashboardDto.toSummary(): DashboardSummary?`: null when `occupancy` is null; capacity `coerceAtLeast(1)`; an announcement whose title and message are both blank becomes null; priority defaults to `"Normal"`.
  - `fun occupancyPercent(current: Int, capacity: Int): Int`: rounded `current * 100 / capacity`, clamped to 0..100; a capacity of 0 or less gives 0.
- Produces, in `LibraryRepository`:
  - `var dashboardSummary by mutableStateOf<DashboardSummary?>(null)` (`private set`)
  - `suspend fun loadDashboardSummary()`
  - `fun applyPreviewDashboardSummary(summary: DashboardSummary)`
  - Called as `runCatching { loadDashboardSummary() }` from `refreshStudentData` and from the overview's pull-to-refresh. Cleared in `clearLocalData`.
- Produces, in `LibraryPulseCards.kt`: `@Composable fun LibraryPulseCards(summary: DashboardSummary)`.

- [ ] **Step 1: Write the failing tests**

```kotlin
class DashboardSummaryTest {
    private fun parse(json: String) = Gson().fromJson<ApiEnvelope<UserDashboardDto>>(
        json, object : TypeToken<ApiEnvelope<UserDashboardDto>>() {}.type).data!!

    @Test fun mapsOccupancyAndAnnouncement() {
        val s = parse("""{"success":true,"data":{"occupancy":{"current":12,"capacity":80},
            "announcement":{"id":4,"title":"Inventory week","message":"Closed Friday.","priority":"Important","publishedAt":"2026-10-20 09:15 AM"},
            "summary":{"activeLoans":1}}}""").toSummary()!!
        assertEquals(12, s.occupancyCurrent); assertEquals(80, s.occupancyCapacity)
        assertEquals(LibraryAnnouncement("Inventory week", "Closed Friday.", "Important", "2026-10-20 09:15 AM"), s.announcement)
    }
    @Test fun nullAnnouncement() {
        assertNull(parse("""{"success":true,"data":{"occupancy":{"current":0,"capacity":80},"announcement":null}}""").toSummary()!!.announcement)
    }
    @Test fun missingOccupancyMeansNoSummary() = assertNull(UserDashboardDto().toSummary())
    @Test fun percentIsClamped() {
        assertEquals(15, occupancyPercent(12, 80))
        assertEquals(100, occupancyPercent(120, 80))
        assertEquals(0, occupancyPercent(-3, 80))
        assertEquals(0, occupancyPercent(5, 0))
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DashboardSummaryTest*"`
Expected: compile failure (`UserDashboardDto` and `toSummary` are not defined).

- [ ] **Step 3: Implement the DTOs, `userDashboard()`, and `DashboardSummary.kt`**

- [ ] **Step 4: Run the tests and confirm they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*DashboardSummaryTest*"`
Expected: PASS.

- [ ] **Step 5: Repository wiring and UI**

`LibraryPulseCards(summary)` has two parts:
1. A `SectionCard` labelled `"Library occupancy"`:
   - the value `"${current} / ${capacity}"` in `titleLarge` bold
   - a `LinearProgressIndicator(progress = { occupancyPercent(...) / 100f })` in `MaterialTheme.colorScheme.primary`
   - `contentDescription` `"Library occupancy: $current of $capacity seats in use"` on the card
2. When the announcement isn't null, a `SectionCard` with a `Lucide.Bell` icon:
   - the eyebrow `"Library announcement · ${priority}"` (EyebrowStyle)
   - the title (bold)
   - the message
   - `publishedAt` (bodySmall, `onSurfaceVariant`)

   Use the `onSurface` and `onSurfaceVariant` text colors so it works in dark mode. Don't use hard-coded yellow text.

Placement:
- **Student overview:** a new `item` right after the existing pill-tab row: `repository.dashboardSummary?.let { item { LibraryPulseCards(it) } }`.
- **`FacultyOverview`:** add a `summary: DashboardSummary?` parameter and render the cards under the welcome card.

- [ ] **Step 6: Verify, visually check, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. Expected: `BUILD SUCCESSFUL`.
Visual check: seed `applyPreviewDashboardSummary(DashboardSummary(42, 80, LibraryAnnouncement("Inventory week", "The library closes at 3:00 PM on Friday for inventory.", "Important", "2026-10-20 09:15 AM")))` in `PreviewScaffold`. Capture `DashboardOverviewStudentPreview` and `DashboardOverviewStudentDarkPreview`, plus one Faculty overview preview.
Commit: `feat(dashboard): library occupancy and announcement`

---

### Task 2: Copy details on book detail

**Files:**
- Create: `app/src/main/java/data/CopyDetails.kt`, `app/src/main/java/dashboard/catalog/CopyDetailsSection.kt`
- Modify:
  - `gradle/libs.versions.toml` (`coil-svg = { group = "io.coil-kt", name = "coil-svg", version.ref = "coil" }`) and `app/build.gradle.kts` (`implementation(libs.coil.svg)`)
  - `app/src/main/java/data/models/LibraryModels.kt` (`Book.previewBarcode: String? = null`)
  - `app/src/main/java/data/LibraryRepository.kt` (`toBook()` maps it)
  - `app/src/main/java/network/dto/LibraryDtos.kt`, `app/src/main/java/network/LibraryApi.kt`
  - `app/src/main/java/dashboard/catalog/BookDetailScreen.kt`
  - `app/src/main/java/ui/preview/ScreenPreviews.kt`
- Test: `app/src/test/java/com/example/stismartlib/CopyDetailsTest.kt`

**Interfaces:**
- Produces, in the network layer:
  - `CatalogCopyDto(val accessionNumber: String? = null, val barcode: String? = null, val shelfLocation: String? = null, val conditionStatus: String? = null, val barcodeImageData: String? = null)`
  - `@GET("api/v1/catalog/copies/{barcode}") suspend fun catalogCopy(@Path("barcode") barcode: String): Response<ApiEnvelope<CatalogCopyDto>>`
- Produces, in `data/CopyDetails.kt`:
  - `suspend fun LibraryRepository.copyDetails(barcode: String): CatalogCopyDto`, run on `Dispatchers.IO` through `ApiClient.unwrap`; errors propagate to the caller.
  - `fun svgBytesFromDataUri(uri: String?): ByteArray?`: decoded bytes only for the prefix `data:image/svg+xml;base64,`. Uses `java.util.Base64.getDecoder()`. Returns null for any other input or for invalid base64.
  - `fun displayValue(value: String?): String`: a blank value becomes `"Not recorded"`.
- Produces: `@Composable fun CopyDetailsSection(barcode: String)`. It loads with `LaunchedEffect(barcode)` and holds states loading, loaded (`CatalogCopyDto`) and failed. It renders `CopyDetailsContent(copy: CatalogCopyDto?, loading: Boolean, failed: Boolean)`, which is internal so previews can use it.

- [ ] **Step 1: Write the failing tests**

```kotlin
class CopyDetailsTest {
    private val svg = "<svg xmlns='http://www.w3.org/2000/svg'/>"
    private val uri = "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.toByteArray())

    @Test fun decodesSvgDataUri() = assertEquals(svg, String(svgBytesFromDataUri(uri)!!))
    @Test fun rejectsOtherUris() {
        assertNull(svgBytesFromDataUri("data:image/png;base64,AAAA"))
        assertNull(svgBytesFromDataUri("data:image/svg+xml;base64,%%%"))
        assertNull(svgBytesFromDataUri(null))
    }
    @Test fun blankValuesReadNotRecorded() {
        assertEquals("Not recorded", displayValue(" ")); assertEquals("Good", displayValue("Good"))
    }
    @Test fun copyReplyParses() {
        val json = """{"success":true,"data":{"physicalCopyId":3,"titleId":7,"accessionNumber":"10042","barcode":"STI-000123",
            "shelfLocation":"Circulation A-3","conditionStatus":null,"barcodeImageData":"$uri"}}"""
        val dto = Gson().fromJson<ApiEnvelope<CatalogCopyDto>>(json, object : TypeToken<ApiEnvelope<CatalogCopyDto>>() {}.type).data!!
        assertEquals("STI-000123", dto.barcode); assertNull(dto.conditionStatus)
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*CopyDetailsTest*"`
Expected: compile failure.

- [ ] **Step 3: Implement the dependency, DTO, API call, `CopyDetails.kt`, and the `Book.previewBarcode` mapping**

In `toBook()`, map `previewBarcode = previewBarcode?.takeIf { it.isNotBlank() }`.

- [ ] **Step 4: Run the tests and confirm they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "*CopyDetailsTest*"`
Expected: PASS.

- [ ] **Step 5: Build the section and add it to `BookDetailScreen`**

`CopyDetailsContent` is a `SectionCard` with:
- the heading `"Barcode"` and a `Lucide.ScanBarcode` icon
- rows `"Book location"`: `displayValue(shelfLocation)`, `"Current condition"`: `displayValue(conditionStatus)`, and `"Accession"`: `displayValue(accessionNumber)`
- the barcode image: Coil `AsyncImage` with `ImageRequest.Builder(context).data(svgBytes).decoderFactory(SvgDecoder.Factory())`, `contentDescription = "Barcode ${barcode}"`, full width, height 96dp, `ContentScale.Fit`, on a white `Box`, since a barcode needs a light background to scan in dark mode. Show it only when `svgBytesFromDataUri` isn't null.
- the barcode text in monospace bold, centred

States:
- loading: `ElmsLoadingBlock()`
- failed: `"Copy details are unavailable right now."` (`onSurfaceVariant`)

In `BookDetailScreen`, add the item after the existing SectionCard item: `book.previewBarcode?.let { item { Spacer(12.dp); CopyDetailsSection(it) } }`.

- [ ] **Step 6: Verify, visually check, and commit**

Run: `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`. Expected: `BUILD SUCCESSFUL`.
Visual check:
- Previews `CopyDetailsPreview` and `CopyDetailsDarkPreview`, using `CopyDetailsContent` with a sample DTO whose `barcodeImageData` holds a small valid barcode-like SVG.
- `CopyDetailsUnavailablePreview` (`failed = true`).
- Capture light and dark.

Commit: `feat(catalog): physical copy details and barcode on book detail`

---

## After all tasks

- Fast-forward the user's `main` checkout (`C:\Users\Ethan\AndroidStudioProjects\STI ILMS`). It holds only uncommitted `.idea/` changes and untracked files, so `git merge --ff-only` is enough. Re-check `git status` first. The graphify merge hook rebuilds the graph by itself.
