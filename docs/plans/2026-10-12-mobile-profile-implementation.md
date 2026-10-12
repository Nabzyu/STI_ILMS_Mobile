# Mobile My Profile Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Ship My profile on STI ILMS Mobile so users can view/edit their own profile and submit a private avatar for librarian approval, using the existing hosted `api/v1/profile/*` APIs (no new database work).

**Architecture:** Mirror Fines — Retrofit `LibraryApi` + DTOs → `LibraryRepository` Compose state (no ViewModel) → `ProfileScreen` → `DashboardRoute.Profile` with drawer item **and** account-card navigation. Auth is the existing Bearer JWT. Coil loads short-lived signed avatar URLs with disk cache disabled.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Retrofit 2.11 (`GET`/`PATCH`/`Multipart POST`), OkHttp, Gson `@SerializedName`, Coil Compose 2.7, Activity Result Photo Picker, `SessionStore`, Lucide icons, existing Portal/Elms components.

**Sources (both must be satisfied):**

| Document | Role |
|----------|------|
| [`2026-10-12-mobile-profile-design.md`](./2026-10-12-mobile-profile-design.md) | Approved product design — UX, decisions, out-of-scope, verification |
| This file | Executable engineering plan — files, code, commits, tests |

**Skills during implementation:**

| Skill | When |
|-------|------|
| `@sti-design-system` | Any ProfileScreen / drawer visual work |
| `@android` / `@mobile-design` | Compose, Photo Picker, touch targets |
| `@error-handling-patterns` | Load / save / upload states |
| `@android-visual-verification` | After UI is navigable |
| `@verification-before-completion` | Before claiming done |

---

## Design requirements traceability

Every approved design item maps to a task. Do not ship until all rows are done.

| # | Design requirement | Task |
|---|--------------------|------|
| D1 | Approach 1: Api → Repository → Screen, no ViewModel | 2–5 |
| D2 | `GET/PATCH api/v1/profile/me` | 2–4 |
| D3 | `GET/POST api/v1/profile/avatar/me` multipart `image` | 2–4, 6 |
| D4 | Coil; no long-lived disk cache of signed URLs | 1, 6 |
| D5 | Screen at `dashboard/account/ProfileScreen.kt` | 5–6 |
| D6 | Layout: header → summary → photo → editable → read-only → actions | 5–6 |
| D7 | Editable: first/last; Students also program + year | 5–6 |
| D8 | Read-only: school ID, email, role | 5 |
| D9 | Photo private until approval; pending/rejected notes | 6 |
| D10 | PNG/JPEG/WebP ≤ 2 MB client validation | 6 |
| D11 | After name save: refresh `SessionStore` + drawer display name | 4 |
| D12 | Keep last good profile on failed refresh when possible | 4 |
| D13 | Busy disable on Save / upload | 4–6 |
| D14 | `DashboardRoute.Profile` + NavHost composable | 7 |
| D15 | Drawer **My profile** under My account | 7 |
| D16 | Drawer account card opens Profile | 7 |
| D17 | Student + Faculty (+ staff who sign in); no Admin-only profile | 5–7 |
| D18 | No new DB; no Admin profile rebuild; no forever avatar cache | All (out of scope) |
| D19 | Emulator verify light + dark; optional previews | 8–9 |

**Build order (matches design scope):** deps → API/DTO → repository load → read-only UI → edit/save → photo upload → navigation polish → verify.

---

### Task 1: Add Coil dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

**Step 1: Catalog version + library**

In `[versions]`:

```toml
coil = "2.7.0"
```

In `[libraries]`:

```toml
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

**Step 2: App dependency**

In `app/build.gradle.kts`:

```kotlin
implementation(libs.coil.compose)
```

**Step 3: Confirm on classpath**

```powershell
.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath | Select-String "coil"
```

Expected: lines containing `coil-compose` and `coil`.

**Step 4: Commit**

```powershell
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build(deps): add Coil for profile avatars"
```

---

### Task 2: Profile DTOs

**Files:**
- Create: `app/src/main/java/network/dto/ProfileDtos.kt`

**Step 1: Write DTO file**

Field names follow existing register DTOs (`first_name`, `program_strand`, …). Avatar URL keys are flexible until live GET is probed; adjust `@SerializedName` if the API differs.

```kotlin
package com.example.stismartlib.network.dto

import com.google.gson.annotations.SerializedName

data class ProfileMeDto(
    val id: Int? = null,
    @SerializedName("account_id") val accountId: Int? = null,
    @SerializedName("school_id") val schoolId: String? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    val role: String? = null,
    @SerializedName("program_strand") val programStrand: String? = null,
    @SerializedName("year_grade_level") val yearGradeLevel: String? = null,
    val status: String? = null,
)

data class ProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("program_strand") val programStrand: String? = null,
    @SerializedName("year_grade_level") val yearGradeLevel: String? = null,
)

data class ProfileAvatarDto(
    val status: String? = null,
    val url: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("signed_url") val signedUrl: String? = null,
    @SerializedName("rejection_reason") val rejectionReason: String? = null,
    val message: String? = null,
) {
    fun resolvedUrl(): String? =
        url?.takeIf { it.isNotBlank() }
            ?: avatarUrl?.takeIf { it.isNotBlank() }
            ?: signedUrl?.takeIf { it.isNotBlank() }
}
```

**Step 2: Commit**

```powershell
git add app/src/main/java/network/dto/ProfileDtos.kt
git commit -m "feat(profile): add profile and avatar DTOs"
```

---

### Task 3: LibraryApi endpoints (incl. first multipart)

**Files:**
- Modify: `app/src/main/java/network/LibraryApi.kt`

**Step 1: Imports**

```kotlin
import com.example.stismartlib.network.dto.ProfileAvatarDto
import com.example.stismartlib.network.dto.ProfileMeDto
import com.example.stismartlib.network.dto.ProfileUpdateRequest
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
```

**Step 2: Methods** (place after `fines`)

```kotlin
    @GET("api/v1/profile/me")
    suspend fun profileMe(): Response<ApiEnvelope<ProfileMeDto>>

    @PATCH("api/v1/profile/me")
    suspend fun updateProfileMe(@Body body: ProfileUpdateRequest): Response<ApiEnvelope<ProfileMeDto>>

    @GET("api/v1/profile/avatar/me")
    suspend fun profileAvatarMe(): Response<ApiEnvelope<ProfileAvatarDto>>

    @Multipart
    @POST("api/v1/profile/avatar/me")
    suspend fun uploadProfileAvatarMe(
        @Part image: MultipartBody.Part,
    ): Response<ApiEnvelope<ProfileAvatarDto>>
```

**Step 3: Commit**

```powershell
git add app/src/main/java/network/LibraryApi.kt
git commit -m "feat(profile): add profile and avatar API methods"
```

---

### Task 4: Domain models + repository (load, save, upload, session refresh)

**Files:**
- Modify: `app/src/main/java/data/models/LibraryModels.kt`
- Modify: `app/src/main/java/data/LibraryRepository.kt`

**Step 1: Domain types in `LibraryModels.kt`**

```kotlin
enum class AvatarApprovalStatus(val label: String) {
    NONE("No photo"),
    PENDING("Pending approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    UNKNOWN("Unknown"),
}

data class UserProfile(
    val accountId: Int = 0,
    val schoolId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val programStrand: String = "",
    val yearGradeLevel: String = "",
    val status: String = "",
)

data class ProfileAvatar(
    val status: AvatarApprovalStatus = AvatarApprovalStatus.NONE,
    val imageUrl: String? = null,
    val rejectionReason: String? = null,
    val note: String? = null,
)
```

**Step 2: Repository state** (near `fines`)

```kotlin
var profile by mutableStateOf<UserProfile?>(null)
    private set
var profileAvatar by mutableStateOf<ProfileAvatar?>(null)
    private set
var isProfileSaving by mutableStateOf(false)
    private set
var isAvatarUploading by mutableStateOf(false)
    private set
var profileActionError by mutableStateOf<String?>(null)
    private set
```

In `clearLocalData()` reset: `profile = null`, `profileAvatar = null`, `isProfileSaving = false`, `isAvatarUploading = false`, `profileActionError = null`.

**Step 3: Load — keep last good profile on refresh failure (D12)**

```kotlin
suspend fun loadProfile() = withContext(Dispatchers.IO) {
    val dto = ApiClient.unwrap(ApiClient.api.profileMe())
    profile = dto.toUserProfile()
}

suspend fun loadProfileAvatar() = withContext(Dispatchers.IO) {
    val dto = ApiClient.unwrap(ApiClient.api.profileAvatarMe())
    profileAvatar = dto.toProfileAvatar()
}

suspend fun refreshProfileScreen() = withContext(Dispatchers.IO) {
    isLoading = true
    lastError = null
    try {
        loadProfile()
        loadProfileAvatar()
    } catch (error: ApiException) {
        // Keep existing profile/avatar in memory if this refresh fails.
        lastError = error.message
        throw error
    } finally {
        isLoading = false
    }
}
```

**Step 4: Save + session display name (D11)**

```kotlin
suspend fun saveProfile(
    firstName: String,
    lastName: String,
    programStrand: String?,
    yearGradeLevel: String?,
) = withContext(Dispatchers.IO) {
    isProfileSaving = true
    profileActionError = null
    try {
        val dto = ApiClient.unwrap(
            ApiClient.api.updateProfileMe(
                ProfileUpdateRequest(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    programStrand = programStrand?.trim()?.takeIf { it.isNotEmpty() },
                    yearGradeLevel = yearGradeLevel?.trim()?.takeIf { it.isNotEmpty() },
                ),
            ),
        )
        val updated = dto.toUserProfile()
        profile = updated
        val stored = sessionStore.getSession()
        if (stored != null) {
            val displayName = updated.fullName.ifBlank {
                listOf(updated.firstName, updated.lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
            }.ifBlank { stored.fullName }
            sessionStore.save(
                token = stored.token,
                accountId = stored.accountId,
                schoolId = updated.schoolId.ifBlank { stored.schoolId },
                fullName = displayName,
                role = updated.role.ifBlank { stored.role },
            )
            session = session.copy(
                userName = displayName,
                schoolId = updated.schoolId.ifBlank { session.schoolId },
            )
        }
    } catch (error: ApiException) {
        profileActionError = error.message
        throw error
    } finally {
        isProfileSaving = false
    }
}
```

**Step 5: Multipart upload**

```kotlin
suspend fun uploadProfileAvatar(imageBytes: ByteArray, mimeType: String, fileName: String) =
    withContext(Dispatchers.IO) {
        isAvatarUploading = true
        profileActionError = null
        try {
            val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaType()
            val body = imageBytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("image", fileName, body)
            val dto = ApiClient.unwrap(ApiClient.api.uploadProfileAvatarMe(part))
            profileAvatar = dto.toProfileAvatar()
        } catch (error: ApiException) {
            profileActionError = error.message
            throw error
        } finally {
            isAvatarUploading = false
        }
    }
```

**Step 6: Private mappers**

```kotlin
private fun ProfileMeDto.toUserProfile(): UserProfile {
    val composed = listOf(firstName, lastName).filter { !it.isNullOrBlank() }.joinToString(" ")
    return UserProfile(
        accountId = accountId ?: id ?: 0,
        schoolId = schoolId.orEmpty(),
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        fullName = fullName?.takeIf { it.isNotBlank() } ?: composed,
        email = email.orEmpty(),
        role = role.orEmpty(),
        programStrand = programStrand.orEmpty(),
        yearGradeLevel = yearGradeLevel.orEmpty(),
        status = status.orEmpty(),
    )
}

private fun ProfileAvatarDto.toProfileAvatar(): ProfileAvatar {
    val raw = status.orEmpty()
    val mapped = when {
        raw.isBlank() && resolvedUrl().isNullOrBlank() -> AvatarApprovalStatus.NONE
        raw.contains("pend", ignoreCase = true) -> AvatarApprovalStatus.PENDING
        raw.contains("reject", ignoreCase = true) ||
            raw.contains("denied", ignoreCase = true) -> AvatarApprovalStatus.REJECTED
        raw.contains("approv", ignoreCase = true) ||
            raw.equals("active", ignoreCase = true) -> AvatarApprovalStatus.APPROVED
        !resolvedUrl().isNullOrBlank() -> AvatarApprovalStatus.APPROVED
        else -> AvatarApprovalStatus.UNKNOWN
    }
    return ProfileAvatar(
        status = mapped,
        imageUrl = resolvedUrl(),
        rejectionReason = rejectionReason,
        note = message,
    )
}
```

Imports needed: OkHttp `toMediaType` / `toMediaTypeOrNull` / `toRequestBody` / `MultipartBody`, profile DTOs, `AvatarApprovalStatus`, `UserProfile`, `ProfileAvatar`.

**Step 7: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`.

**Step 8: Commit**

```powershell
git add app/src/main/java/data/models/LibraryModels.kt app/src/main/java/data/LibraryRepository.kt
git commit -m "feat(profile): load, save, and upload via repository"
```

---

### Task 5: ProfileScreen — read-only + editable fields (no photo upload yet)

**Files:**
- Create: `app/src/main/java/dashboard/account/ProfileScreen.kt`
- Skills: `@sti-design-system`, `@error-handling-patterns`

**Step 1: Scaffold screen (D5–D8, D13 partial)**

Package: `com.example.stismartlib.ui.dashboard.account`

Must include:

1. `val repository = rememberLibraryRepository()`
2. `LaunchedEffect(Unit) { runCatching { repository.refreshProfileScreen() } }`
3. Local `firstName` / `lastName` / `programStrand` / `yearGradeLevel` seeded via `LaunchedEffect(repository.profile)`
4. `PageHeader(eyebrow = "My account", title = "My profile")`
5. Summary `SectionCard`: full name, role (`UserType.fromServerRole` displayName), optional API `status`
6. Loading: `ElmsLoadingBlock` when `repository.isLoading && repository.profile == null`
7. Errors: `ElmsBanner` for `lastError` and `profileActionError`
8. Editable `PortalField`s for first/last; if `repository.session.userType == UserType.STUDENT` also program/strand + year/grade
9. Read-only school ID, email, role (`enabled = false` or `readOnly = true`)
10. `PortalButton(text = "Save", loading = repository.isProfileSaving, enabled = !repository.isProfileSaving)` calling `repository.saveProfile(...)` in a coroutine scope
11. Placeholder section for photo (“Photo coming in next task” is OK only if committing mid-feature; prefer empty photo card with initials — Task 6 fills Coil + picker)

Use design-system components only; no raw `Color(0xFF…)`.

**Step 2: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

**Step 3: Commit**

```powershell
git add app/src/main/java/dashboard/account/ProfileScreen.kt
git commit -m "feat(profile): add My profile screen with load and save"
```

---

### Task 6: Photo picker, Coil avatar, pending/rejected UX

**Files:**
- Modify: `app/src/main/java/dashboard/account/ProfileScreen.kt`
- Skills: `@sti-design-system`, `@android`, `@mobile-design`

**Step 1: Constants**

```kotlin
private const val MAX_AVATAR_BYTES = 2 * 1024 * 1024
private val ALLOWED_AVATAR_MIME = setOf("image/png", "image/jpeg", "image/webp")
```

**Step 2: Coil avatar (D4, D9)**

```kotlin
val avatar = repository.profileAvatar
val imageUrl = avatar?.imageUrl
if (!imageUrl.isNullOrBlank() && avatar.status != AvatarApprovalStatus.REJECTED) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build(),
        contentDescription = "Profile photo",
        modifier = Modifier.size(96.dp).clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
} else {
    // Initials fallback Box (StiYellow / StiMarkBlue like drawer card)
}
```

Show `ElmsBanner` / helper text:

- PENDING: photo submitted; private until librarian approval
- REJECTED: show `rejectionReason` or generic rejected message; allow re-upload
- APPROVED: show photo
- NONE: initials + prompt to add photo

**Step 3: Photo Picker + validation (D10)**

```kotlin
val context = LocalContext.current
val scope = rememberCoroutineScope()
val picker = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia(),
) { uri ->
    if (uri == null) return@rememberLauncherForActivityResult
    scope.launch {
        val mime = context.contentResolver.getType(uri) ?: ""
        if (mime !in ALLOWED_AVATAR_MIME) {
            // set local validation error: PNG, JPEG, or WebP only
            return@launch
        }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) return@launch
        if (bytes.size > MAX_AVATAR_BYTES) {
            // set local validation error: max 2 MB
            return@launch
        }
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        runCatching {
            repository.uploadProfileAvatar(bytes, mime, "avatar.$ext")
        }
    }
}
```

Button: “Change photo” / “Submit for approval” →

```kotlin
picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
```

Disable while `repository.isAvatarUploading`; use `PortalButton(..., loading = repository.isAvatarUploading)`.

**Step 4: Compile + commit**

```powershell
.\gradlew.bat :app:compileDebugKotlin
git add app/src/main/java/dashboard/account/ProfileScreen.kt
git commit -m "feat(profile): add avatar upload and approval status UI"
```

---

### Task 7: Navigation — route, drawer item, account card (D14–D16)

**Files:**
- Modify: `app/src/main/java/dashboard/DashboardRoute.kt`
- Modify: `app/src/main/java/dashboard/DashboardScreen.kt`

**Step 1: Route**

```kotlin
object Profile : DashboardRoute("profile")
```

**Step 2: NavHost** (beside Fines/Clearance)

```kotlin
composable(DashboardRoute.Profile.route) {
    ProfileScreen()
}
```

**Step 3: Drawer My account group**

```kotlin
items += DrawerItem(DashboardRoute.Profile.route, "My profile", Lucide.User, "My account")
items += DrawerItem(DashboardRoute.Notifications.route, "Notifications", Lucide.Bell)
items += DrawerItem(DashboardRoute.Fines.route, "Fines", Lucide.CircleDollarSign)
items += DrawerItem(DashboardRoute.Clearance.route, "Clearance status", Lucide.BadgeCheck)
```

Remove `"My account"` from the Notifications `DrawerItem` so Profile owns the section header.

**Step 4: Account card → Profile**

On the footer identity `Row` (initials + `repository.session.userName`), add clickable navigation matching QrLogin pattern:

```kotlin
.clickable {
    dashboardNavController.navigate(DashboardRoute.Profile.route) {
        launchSingleTop = true
    }
    scope.launch { drawerState.close() }
}
```

**Step 5: Compile + commit**

```powershell
.\gradlew.bat :app:compileDebugKotlin
git add app/src/main/java/dashboard/DashboardRoute.kt app/src/main/java/dashboard/DashboardScreen.kt
git commit -m "feat(profile): wire My profile route and drawer entry"
```

---

### Task 8: Previews (optional, design D19)

**Files:**
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt`

**Step 1:** Add light/dark preview entries following Fines/Clearance. If full `ProfileScreen` needs a live repository, extract a previewable content composable that accepts sample `UserProfile` / `ProfileAvatar`.

**Step 2: Commit**

```powershell
git add app/src/main/java/ui/preview/ScreenPreviews.kt
git commit -m "chore(profile): add profile screen previews"
```

---

### Task 9: Visual verification (design Verification section)

**Skills:** `@android-visual-verification`, `@verification-before-completion`

**Environment:** PowerShell; AVD `Pixel_5`; package `com.example.stismartlib`.

**Checklist:**

1. Boot emulator, install debug build, sign in (Student).
2. Drawer → **My profile** opens Profile.
3. Account card opens the same screen and closes drawer.
4. Profile loads: name, role, locked school ID/email.
5. Edit first/last (+ program/year for Student) → Save → drawer name updates.
6. Pick valid image → pending note; oversize/wrong type → client error, no upload.
7. Faculty (if available): no program/year fields.
8. Screenshots light + dark under `build/uishots/`; **look at the images** before claiming done.
9. Do not commit `build/`.

If live API JSON keys differ, fix DTOs and commit:

```powershell
git commit -m "fix(profile): align DTOs with live profile API fields"
```

---

## Out of scope (do not implement)

- New database / migrations
- Admin-only profile (removed on web)
- Rebuilding backend profile modules
- Long-lived disk caching of signed avatar URLs
- Putting Profile behind a separate ViewModel or new repository class

## Risks

| Risk | Mitigation |
|------|------------|
| Avatar JSON field names differ | Flexible DTO + `resolvedUrl()`; probe live GET |
| Multipart part name ≠ `image` | Match web; rename `@Part` if rejected |
| Signed URL expiry | Disk cache off; reload on screen enter |
| Gson null PATCH fields | Omit blank academic fields via `takeIf` |

## Done criteria

- [ ] All design rows D1–D19 satisfied
- [ ] Coil on classpath; disk cache disabled for avatars
- [ ] GET/PATCH profile + GET/POST avatar working against hosted API
- [ ] ProfileScreen matches design layout and portal components
- [ ] Drawer **My profile** + account card navigate to Profile
- [ ] Name save updates session display name
- [ ] Emulator screenshots reviewed in light and dark

---

## Document roles after this plan

| File | Keep as |
|------|---------|
| `2026-10-12-mobile-profile-design.md` | Approved design (source of truth for UX decisions) |
| `2026-10-12-mobile-profile-implementation.md` | **Canonical implementation plan — execute this** |
| `2026-10-12-mobile-profile.md` | Superseded draft; redirect note only |
