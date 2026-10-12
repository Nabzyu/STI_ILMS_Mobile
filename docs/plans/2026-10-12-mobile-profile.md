# Mobile My Profile Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (or implement task-by-task in this session). Design source: `docs/plans/2026-10-12-mobile-profile-design.md`.

**Goal:** Add My profile to STI ILMS Mobile — load/edit own profile and submit avatar for approval via existing `api/v1/profile/*` APIs, wired like Fines/Clearance.

**Architecture:** `LibraryApi` + DTOs → `LibraryRepository` state/methods → `ProfileScreen` Compose UI → `DashboardRoute.Profile` + drawer item + account-card navigation. No ViewModel. Coil for signed avatar URLs (no long-lived disk cache).

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Retrofit 2.11 (incl. multipart), OkHttp, Gson `@SerializedName`, Coil Compose, Activity Result Photo Picker, existing `SessionStore` / Bearer JWT.

**Skills during implementation:**

| Skill | When |
|-------|------|
| `@sti-design-system` | ProfileScreen UI |
| `@android` / `@mobile-design` | Compose, Photo Picker, touch |
| `@android-visual-verification` | After UI tasks |
| `@error-handling-patterns` | Load/save/upload states |
| `@verification-before-completion` | Before claiming done |

---

### Task 1: Add Coil dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

**Step 1: Add version + library to catalog**

In `libs.versions.toml` under `[versions]`:

```toml
coil = "2.7.0"
```

Under `[libraries]`:

```toml
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

**Step 2: Wire app module**

In `app/build.gradle.kts` dependencies:

```kotlin
implementation(libs.coil.compose)
```

**Step 3: Sync Gradle**

Run (PowerShell, project root):

```powershell
.\gradlew.bat :app:dependencies --configuration debugRuntimeClasspath | Select-String "coil"
```

Expected: `coil-compose` and `coil` appear.

**Step 4: Commit**

```powershell
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build(deps): add Coil for profile avatars"
```

---

### Task 2: Profile DTOs

**Files:**
- Create: `app/src/main/java/network/dto/ProfileDtos.kt`

**Step 1: Add DTOs**

Mirror register field naming (`first_name`, etc.). Avatar fields should match live API; if GET returns different keys, adjust `@SerializedName` after first probe.

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

### Task 3: LibraryApi profile endpoints (+ multipart)

**Files:**
- Modify: `app/src/main/java/network/LibraryApi.kt`

**Step 1: Add imports**

```kotlin
import com.example.stismartlib.network.dto.ProfileAvatarDto
import com.example.stismartlib.network.dto.ProfileMeDto
import com.example.stismartlib.network.dto.ProfileUpdateRequest
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
```

**Step 2: Add methods** (after `fines` block is fine)

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

### Task 4: Domain models + repository state/methods

**Files:**
- Modify: `app/src/main/java/data/models/LibraryModels.kt`
- Modify: `app/src/main/java/data/LibraryRepository.kt`

**Step 1: Domain types in LibraryModels.kt**

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

Clear these in `clearLocalData()` alongside other lists/state.

**Step 3: Mapping helpers + load/save/upload**

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
        lastError = error.message
        throw error
    } finally {
        isLoading = false
    }
}

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
                listOf(updated.firstName, updated.lastName).filter { it.isNotBlank() }.joinToString(" ")
            }.ifBlank { stored.fullName }
            sessionStore.save(
                token = stored.token,
                accountId = stored.accountId,
                schoolId = updated.schoolId.ifBlank { stored.schoolId },
                fullName = displayName,
                role = updated.role.ifBlank { stored.role },
            )
            session = session.copy(userName = displayName, schoolId = updated.schoolId.ifBlank { session.schoolId })
        }
    } catch (error: ApiException) {
        profileActionError = error.message
        throw error
    } finally {
        isProfileSaving = false
    }
}

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

Add private mappers:

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
        raw.contains("reject", ignoreCase = true) || raw.contains("denied", ignoreCase = true) -> AvatarApprovalStatus.REJECTED
        raw.contains("approv", ignoreCase = true) || raw.equals("active", ignoreCase = true) -> AvatarApprovalStatus.APPROVED
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

Imports: `okhttp3.MediaType.Companion.toMediaType`, `toMediaTypeOrNull`, `toRequestBody`, `MultipartBody`, DTOs, domain types.

**Step 4: Compile check**

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

**Step 5: Commit**

```powershell
git add app/src/main/java/data/models/LibraryModels.kt app/src/main/java/data/LibraryRepository.kt
git commit -m "feat(profile): load, save, and upload via repository"
```

---

### Task 5: ProfileScreen (read + edit + photo)

**Files:**
- Create: `app/src/main/java/dashboard/account/ProfileScreen.kt`
- Skills: `@sti-design-system`, `@error-handling-patterns`

**Step 1: Implement screen**

Package: `com.example.stismartlib.ui.dashboard.account`

Behaviour:

1. `LaunchedEffect(Unit) { repository.refreshProfileScreen() }`
2. Local form state seeded from `repository.profile` when loaded (`LaunchedEffect(profile)`).
3. Loading: `ElmsLoadingBlock` when `isLoading && profile == null`.
4. Error: `ElmsBanner` for `lastError` / `profileActionError`.
5. Summary + avatar (Coil `AsyncImage` with `MemoryCachePolicy.ENABLED` / disk **DISABLED**; initials fallback).
6. Photo Picker: `rememberLauncherForActivityResult(PickVisualMedia())`; read bytes via `contentResolver`; validate MIME ∈ `{image/png, image/jpeg, image/webp}` and size ≤ 2_097_152; then `uploadProfileAvatar`.
7. Editable `PortalField`s: first/last; if `UserType.STUDENT` (or role student) also program + year.
8. Read-only school ID, email, role (`readOnly = true` / `enabled = false`).
9. `PortalButton("Save", loading = isProfileSaving)` → `saveProfile(...)`.
10. Secondary control for change photo / submit for approval.

Use `PageHeader(eyebrow = "My account", title = "My profile")`, `SectionCard`, no raw `Color(0xFF…)`.

Coil example:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(avatar.imageUrl)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .crossfade(true)
        .build(),
    contentDescription = "Profile photo",
    modifier = Modifier.size(96.dp).clip(CircleShape),
    contentScale = ContentScale.Crop,
)
```

Constants:

```kotlin
private const val MAX_AVATAR_BYTES = 2 * 1024 * 1024
private val ALLOWED_AVATAR_MIME = setOf("image/png", "image/jpeg", "image/webp")
```

**Step 2: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

**Step 3: Commit**

```powershell
git add app/src/main/java/dashboard/account/ProfileScreen.kt
git commit -m "feat(profile): add My profile screen"
```

---

### Task 6: Navigation + drawer + account card

**Files:**
- Modify: `app/src/main/java/dashboard/DashboardRoute.kt`
- Modify: `app/src/main/java/dashboard/DashboardScreen.kt`

**Step 1: Route**

```kotlin
object Profile : DashboardRoute("profile")
```

**Step 2: NavHost**

```kotlin
composable(DashboardRoute.Profile.route) {
    ProfileScreen()
}
```

Import `ProfileScreen`.

**Step 3: Drawer item** (My account group — before Notifications or right after section header)

```kotlin
items += DrawerItem(DashboardRoute.Profile.route, "My profile", Lucide.User, "My account")
items += DrawerItem(DashboardRoute.Notifications.route, "Notifications", Lucide.Bell)
items += DrawerItem(DashboardRoute.Fines.route, "Fines", Lucide.CircleDollarSign)
items += DrawerItem(DashboardRoute.Clearance.route, "Clearance status", Lucide.BadgeCheck)
```

Remove the old `"My account"` section string from Notifications so Profile owns the section header.

**Step 4: Account card clickable**

On the footer `Row` that shows initials + name, add:

```kotlin
.clickable {
    scope.launch { drawerState.close() }
    dashboardNavController.navigate(DashboardRoute.Profile.route) {
        launchSingleTop = true
    }
}
```

Ensure `scope` / `drawerState` / nav controller are in scope (same as other drawer item clicks).

**Step 5: Compile + commit**

```powershell
.\gradlew.bat :app:compileDebugKotlin
git add app/src/main/java/dashboard/DashboardRoute.kt app/src/main/java/dashboard/DashboardScreen.kt
git commit -m "feat(profile): wire My profile route and drawer entry"
```

---

### Task 7: Previews (optional but recommended)

**Files:**
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt`

Add light/dark previews for `ProfileScreen` following Fines/Clearance entries (may need repository fake/seed if previews require auth — match existing pattern; if Profile needs live repo state, preview a private content composable with sample `UserProfile` instead).

```powershell
git add app/src/main/java/ui/preview/ScreenPreviews.kt
git commit -m "chore(profile): add profile screen previews"
```

---

### Task 8: Visual verification

**Skills:** `@android-visual-verification`, `@verification-before-completion`

**Step 1:** Boot `Pixel_5`, install debug APK, sign in as Student.

**Step 2:** Open drawer → **My profile**; also open via account card.

**Step 3:** Confirm load of name/role/school ID/email; edit + Save; drawer name updates.

**Step 4:** Pick photo ≤ 2 MB → pending note; reject oversize with client message.

**Step 5:** Capture light + dark screenshots under `build/uishots/`.

**Step 6:** Spot-check Faculty (no program/year fields) if a faculty account is available.

Do not claim done without looking at the screenshots.

---

### Task 9: Final commit hygiene (if needed)

If Task 8 only produced untracked docs/screenshots under `build/`, do not commit `build/`. Fix any DTO `@SerializedName` mismatches discovered against the live API in a small follow-up commit:

```powershell
git commit -m "fix(profile): align DTOs with live profile API fields"
```

---

## Testing notes

- Unit tests: optional pure mapper tests for `AvatarApprovalStatus` parsing if extracted to a top-level function; project has little repository test infra — do not invent a large mock stack unless asked.
- Primary verification is device/emulator + compile.

## Risks

| Risk | Mitigation |
|------|------------|
| Avatar JSON field names differ | Flexible DTO + `resolvedUrl()`; adjust after first GET |
| Multipart part name not `image` | Match web (`image`); change `@Part` name if API rejects |
| Signed URL reuse after expiry | Disk cache disabled; reload avatar on screen enter |
| Gson omits null PATCH fields | Send only non-blank academic fields for students; nulls omitted by Gson by default for null refs |

## Done criteria

- [ ] Coil on classpath
- [ ] GET/PATCH profile + GET/POST avatar wired
- [ ] ProfileScreen matches design (summary, photo states, edit, locked fields)
- [ ] Drawer **My profile** + account card navigate to Profile
- [ ] Name save updates session display name
- [ ] Emulator screenshots reviewed light + dark
