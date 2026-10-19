# Fines how-calculated Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Load `@.claude/skills/android-visual-verification/SKILL.md` after Compose changes, `@.claude/skills/api-route-deploy-gap/SKILL.md` if the policy route 404s, and `@.claude/skills/verification-before-completion/SKILL.md` before claiming done. Design: `docs/plans/2026-10-15-fines-how-calculated-design.md`.

**Goal:** Show students how library fines are calculated and what the rules are on the Fines screen, using the active fine policy from the API (with a soft unavailable state until the route is deployed).

**Architecture:** Add `GET /api/v1/fines/policy` on the SmartLib API (reads `fine_policy_versions` / calculator defaults). Mobile: DTO → `LibraryApi` → `LibraryRepository` → “How fines are calculated” card on `FinesScreen`. Do not hardcode production rates in the app.

**Tech Stack:** Kotlin, Retrofit, Jetpack Compose, JUnit DTO parsing tests, STI Portal/Elms components; backend Express/TS in `JohnBazty/StiOrmocLibrary`.

**Skills:** `@writing-plans` (this doc) · `@sti-design-system` · `@android-visual-verification` · `@api-route-deploy-gap` · `@verification-before-completion`

---

### Task 1: Backend — `GET /api/v1/fines/policy`

**Repo:** `JohnBazty/StiOrmocLibrary` (not this Android repo)

**Files (API):**
- Modify: `apps/api/src/modules/fines/fines.routes.ts` — add `userFinesV1Router.get('/policy', …)`
- Modify: `apps/api/src/modules/fines/fines.controller.ts` — `policy` handler
- Modify: `apps/api/src/modules/fines/fines.service.ts` — `policy()` using same source as `loadFineContext` / `DEFAULT_POLICY`
- Test: `apps/api/src/modules/fines/fines.service.test.ts` (or new focused test)

**Step 1: Implement `finesService.policy()`**

Return shape (camelCase JSON):

```ts
{
  currency: 'PHP',
  hourlyRate: number,
  dailyRate: number,
  maximumPenalty: number,
  effectiveFrom: string | null,
  rules: string[],
}
```

Load active row from `fine_policy_versions` (`is_active=1`, effective window). If none, use calculator defaults (`2` / `10` / `500`).

Generate `rules` strings from those numbers, e.g.:

- `Same-day overdue: ₱{hourlyRate} per hour (rounded up).`
- `Later days: ₱{dailyRate} per open library day.`
- `Closed days do not add to the fine.`
- `Maximum overdue penalty per loan: ₱{maximumPenalty}.`
- `Pay cash only at the library counter with your school ID.`

**Step 2: Wire route**

On `userFinesV1Router` (already behind Student/Faculty JWT on `/api/v1/fines`):

```ts
userFinesV1Router.get('/policy', finesController.policy)
```

**Step 3: Test**

- Active policy row → response matches DB rates  
- No row → defaults  
- Unauthenticated → `401 JWT_REQUIRED`

**Step 4: Deploy to Vercel `main`** (or note as blocker for mobile soft-fail)

**Step 5: Commit** (in API repo, only if asked)

```bash
git commit -m "feat(fines): expose active fine policy for students"
```

---

### Task 2: Mobile — DTO + API + unit parse test

**Files:**
- Create or modify: `app/src/main/java/network/dto/LibraryDtos.kt` (or `FineDtos.kt` if splitting)
- Modify: `app/src/main/java/network/LibraryApi.kt`
- Create: `app/src/test/java/com/example/stismartlib/FinePolicyDtoParsingTest.kt`

**Step 1: Add DTO**

```kotlin
data class FinePolicyDto(
    val currency: String? = "PHP",
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val maximumPenalty: Double? = null,
    val effectiveFrom: String? = null,
    val rules: List<String>? = null,
)
```

Support snake_case alternates with `@SerializedName` if the API ever emits them (`hourly_rate`, etc.).

**Step 2: Add API method** (near existing `fines`)

```kotlin
@GET("api/v1/fines/policy")
suspend fun finePolicy(): Response<ApiEnvelope<FinePolicyDto>>
```

**Step 3: Write failing parse test** with a JSON fixture matching the design envelope; assert rates + rules size.

**Step 4: Run**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.FinePolicyDtoParsingTest" --console=plain
```

Expected: PASS after DTO compiles.

**Step 5: Commit** (only if user asked)

```bash
git commit -m "feat(fines): add fine policy DTO and API"
```

---

### Task 3: Mobile — domain model + repository load

**Files:**
- Modify: `app/src/main/java/data/models/LibraryModels.kt`
- Modify: `app/src/main/java/data/LibraryRepository.kt`

**Step 1: Add model**

```kotlin
data class FinePolicy(
    val currency: String = "PHP",
    val hourlyRate: Double,
    val dailyRate: Double,
    val maximumPenalty: Double,
    val effectiveFrom: String? = null,
    val rules: List<String> = emptyList(),
)
```

**Step 2: Repository state**

```kotlin
var finePolicy by mutableStateOf<FinePolicy?>(null)
    private set
var finePolicyUnavailable by mutableStateOf(false)
    private set
```

(Or a sealed `FinePolicyUi` — keep consistent with nearby patterns; prefer simple nullable + flag if that matches Profile/calendar soft-fail.)

**Step 3: `loadFinePolicy()`**

- Call `api.finePolicy()`  
- On success → map to `FinePolicy`, clear unavailable  
- On 404 / “Route not found” / feature-unavailable → `finePolicy = null`, `finePolicyUnavailable = true` (do **not** invent rates)  
- Other errors → set `lastError` or a dedicated message; leave previous policy if any  

Call from `refreshStudentData()` / whatever already loads fines (alongside `loadFines()`).

**Step 4: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

---

### Task 4: Mobile — Fines screen UI

**Files:**
- Modify: `app/src/main/java/dashboard/account/FinesScreen.kt`
- Modify: `app/src/main/java/ui/preview/PreviewScaffold.kt` (seed sample `FinePolicy`)
- Optional: `app/src/main/java/ui/preview/ScreenPreviews.kt` if a dedicated fines preview needs updating

**Step 1: Insert “How fines are calculated” card**

Placement: after the three `StatCard`s, **before** or immediately after the existing cash-payment `SectionCard` (prefer **before** cash card so rules are visible without scrolling past payment copy).

When `finePolicy != null`:

- Title: `How fines are calculated`
- Rows: Hourly (same day) · Daily (open days after due) · Maximum — format with `₱%.2f`
- Bullets: `policy.rules`
- Optional muted line: `Rates effective from {effectiveFrom}` if non-null

When `finePolicyUnavailable`:

```kotlin
ElmsBanner(
    message = "Fine rules aren’t available right now. Ask the library counter for the current rates.",
    tone = ElmsBannerTone.Info,
)
```

When still loading policy and fines are also loading: rely on existing loading block; or a short “Loading fine rules…” line — keep minimal.

**Step 2: Preview seed**

In `seedPreviewLibraryData`, set a sample policy (defaults are fine for previews only).

**Step 3: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

---

### Task 5: Visual verification

**Skill:** `@.claude/skills/android-visual-verification/SKILL.md`

**Step 1: Install**

```powershell
.\gradlew.bat :app:installDebug --console=plain
```

**Step 2: Capture Fines preview** (preview function name from `ScreenPreviews.kt`, typically `FinesScreenPreview`)

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$pkg = "com.example.stismartlib"
$cls = "com.example.stismartlib.ui.preview.ScreenPreviewsKt"
& $adb shell am force-stop $pkg
& $adb shell am start -n "$pkg/androidx.compose.ui.tooling.PreviewActivity" -e composable "$cls.FinesScreenPreview"
Start-Sleep -Seconds 12
& $adb shell screencap -p /sdcard/s.png
& $adb pull /sdcard/s.png "build\uishots\fines-how-calculated.png"
```

**Step 3: Inspect** with the Read tool — confirm rate rows + rules visible; dark preview if one exists.

**Step 4: Optional live-API check** after backend deploy — signed-in Fines screen should show live rates; before deploy, banner only.

---

### Task 6: Deploy-gap note (docs)

**Files:**
- Optional create: `docs/bugs/2026-10-15-fines-policy-route.md` **or** a short section in the design doc “Deploy checklist”

**Step 1:** Document probe:

```powershell
curl.exe -sS -w "`nHTTP:%{http_code}`n" `
  "https://sti-ormoc-smart-library.vercel.app/api/v1/fines/policy" `
  -H "Accept: application/json"
```

| Response | Meaning |
|----------|---------|
| `401` + `JWT_REQUIRED` | Path may be gated; after auth, confirm not `Route not found` |
| `404` + `Route not found` | Not mounted on production yet — mobile soft-fail expected |
| `200` + rates | Ready for student smoke |

---

## Done criteria

- [ ] Backend `GET /api/v1/fines/policy` implemented (or tracked as deploy blocker)  
- [ ] Mobile parses policy and shows rates + rules on Fines  
- [ ] Missing route → info banner, no fake ₱ amounts  
- [ ] Unit parse test passes  
- [ ] Emulator screenshot reviewed (light; dark if preview exists)  
- [ ] Cash payment + fine history still work  

## Out of scope

- Fine receipt PDF / download  
- Online payment  
- Admin policy editor  
- Local fine recalculation on device  
