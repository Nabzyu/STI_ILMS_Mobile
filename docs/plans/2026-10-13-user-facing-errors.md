# User-Facing Error Messages Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make every user-visible error calm, accurate, and free of login-only wording, Bearer/JWT jargon, and developer strings.

**Architecture:** Single mapping path: `ApiClient.unwrap` keeps codes + sanitized server text; UI and repository always display through `userFacingMessage` in `ui/feedback/UserFacingErrors.kt`. Expand code→copy there; stop mapping bare HTTP 401/403 to login/deactivated defaults.

**Tech Stack:** Kotlin, existing `ApiException` / `ApiClient`, JUnit unit tests under `app/src/test`.

**Context audit (already done):** Raw `error.message` / `lastError` on many screens; `ApiClient` 401→“Invalid student ID or password”; thin code map in `UserFacingErrors.kt`; dual sanitizers; BookDetail toasts raw API text.

---

### Task 1: Unit tests for `userFacingMessage`

**Files:**
- Create: `app/src/test/java/com/example/stismartlib/UserFacingErrorsTest.kt`
- Modify (later tasks): `app/src/main/java/ui/feedback/UserFacingErrors.kt`

**Step 1: Write failing tests for target codes**

```kotlin
package com.example.stismartlib

import com.example.stismartlib.network.ApiException
import com.example.stismartlib.ui.feedback.userFacingMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException

class UserFacingErrorsTest {
    @Test
    fun invalidCredentials() {
        val msg = userFacingMessage(ApiException("x", "INVALID_CREDENTIALS"))
        assertEquals("Invalid school ID or password.", msg)
    }

    @Test
    fun jwtRequired() {
        val msg = userFacingMessage(
            ApiException("A valid Bearer token is required.", "JWT_REQUIRED"),
            fallback = "Unable to load.",
        )
        assertFalse(msg.contains("Bearer", ignoreCase = true))
        assertEquals("Your session expired. Please sign in again.", msg)
    }

    @Test
    fun routeNotFound() {
        val msg = userFacingMessage(
            ApiException("Route not found.", "REQUEST_FAILED"),
            fallback = "Unable to load.",
        )
        assertEquals("This feature is not available right now.", msg)
    }

    @Test
    fun ioException() {
        assertEquals(
            "Unable to reach the library service. Check your internet connection and try again.",
            userFacingMessage(IOException("failed to connect")),
        )
    }
}
```

**Step 2: Run tests (expect FAIL until Task 2)**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.UserFacingErrorsTest" --console=plain
```

**Step 3: Commit when implementing** (only if user asks to commit)

```bash
git add app/src/test/java/com/example/stismartlib/UserFacingErrorsTest.kt
git commit -m "test(errors): add userFacingMessage cases for jwt and route errors"
```

---

### Task 2: Expand and unify `UserFacingErrors.kt`

**Files:**
- Modify: `app/src/main/java/ui/feedback/UserFacingErrors.kt`

**Step 1: Expand code map**

Replace / extend the `when (error.code)` branch so it covers at least:

| Code | User copy |
|------|-----------|
| `INVALID_CREDENTIALS` | `Invalid school ID or password.` (not “student” only) |
| `ACCOUNT_DEACTIVATED` | keep librarian coordination copy |
| `TOO_MANY_LOGIN_ATTEMPTS` | keep 15-minute wait copy |
| `ADMIN_ROLE_REQUIRED` | keep staff-only copy |
| `JWT_REQUIRED`, `JWT_INVALID`, `ACCOUNT_ACCESS_REVOKED` | `Your session expired. Please sign in again.` |
| `JWT_ROLE_FORBIDDEN` | `You do not have access to this action.` |
| `AUTH_VALIDATION_FAILED` | prefer field errors upstream; fallback `Please check the highlighted fields.` |
| `REQUEST_FAILED` / blank | use sanitized message or fallback |

Also map **message** heuristics when code is generic:

- message equals / contains `Route not found` (ignore case) → `This feature is not available right now.`
- message contains Bearer / token / Authorization (if not already mapped by code) → session-expired copy

**Step 2: Single sanitizer**

Make `sanitizeApiMessage` the only policy (Bearer, unauthorized, exception, sql, `{`, `</`, 5xx). Export nothing from ApiClient sanitizer in Task 3—ApiClient should call shared logic or stay minimal and let `userFacingMessage` be the display layer.

Preferred shape:

```kotlin
fun userFacingMessage(
    error: Throwable,
    fallback: String = "Something went wrong. Please try again.",
): String { /* code map + sanitize + fallback */ }

/** For repository storage before UI: same as userFacingMessage. */
fun userFacingMessage(error: ApiException, fallback: String = "Something went wrong. Please try again."): String =
    userFacingMessage(error as Throwable, fallback)
```

**Step 3: Re-run tests — expect PASS**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.UserFacingErrorsTest" --console=plain
```

---

### Task 3: Fix `ApiClient.unwrap` HTTP defaults

**Files:**
- Modify: `app/src/main/java/network/ApiClient.kt` (unwrap ~66–98, sanitize ~111–124)

**Step 1: Stop login/deactivated as global HTTP defaults**

When building `ApiException.message` / code:

1. Prefer envelope `code` + envelope `message` (light sanitize only: strip HTML/stack; do **not** rewrite meaning yet).
2. If message empty, use **neutral** HTTP fallbacks:
   - 401 → message `"Sign-in is required."`, code `JWT_REQUIRED` (or keep server code if present)
   - 403 → message `"You do not have access to this action."`, code `JWT_ROLE_FORBIDDEN` (or server code)
   - 404 → `"This feature is not available right now."`
   - 429 → keep rate-limit message + `TOO_MANY_LOGIN_ATTEMPTS` if that is the login limiter; for other 429 use `"Too many requests. Please wait and try again."`
   - else → `"The request could not be completed."`
3. **Do not** set message to `"Invalid student ID or password."` unless envelope code is `INVALID_CREDENTIALS` (or login path only—prefer code from body).

**Step 2: Align sanitizer with UserFacingErrors**

Either:

- Move shared `sanitizeApiMessage` to `UserFacingErrors.kt` as `internal`/`fun` and call from ApiClient, **or**
- Keep ApiClient sanitizer thin (HTML/stack only) and rely on `userFacingMessage` at every display site (required by Task 4).

**Step 3: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

Expected: `BUILD SUCCESSFUL`

---

### Task 4: Repository always stores user-facing copy

**Files:**
- Modify: `app/src/main/java/data/LibraryRepository.kt`

**Step 1: Import and wrap assignments**

```kotlin
import com.example.stismartlib.ui.feedback.userFacingMessage
```

Replace every:

```kotlin
lastError = error.message
profileActionError = error.message
```

with context-specific fallbacks, e.g.:

```kotlin
lastError = userFacingMessage(error, "Unable to refresh your library data.")
// profile:
profileActionError = userFacingMessage(error, "Unable to update your profile.")
// avatar:
profileActionError = userFacingMessage(error, "Unable to upload your photo.")
```

**Step 2: Soften developer throws** (message users may see)

| Current | Better user message (keep a code if useful) |
|---------|-----------------------------------------------|
| `Missing account id.` | `Sign-in could not be completed. Please try again.` |
| `The server returned an incomplete login response.` | same |
| `The server returned an invalid access token.` | same |
| `Invalid book id.` | `This book could not be reserved.` |
| `Invalid reservation id.` | `This reservation could not be updated.` |

**Step 3: Compile**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

---

### Task 5: Screens — always use `userFacingMessage`

**Files:**
- Modify: `app/src/main/java/dashboard/catalog/BookDetailScreen.kt` (Toast on reserve)
- Modify: `app/src/main/java/dashboard/search/GlobalSearchScreen.kt` (remove ad-hoc Bearer string checks; use `userFacingMessage`)
- Modify: `app/src/main/java/auth/LoginScreen.kt` — banner fallback already OK; ensure label “Student ID” stays (product) but invalid-credentials copy says **school ID** via Task 2
- Verify already-good: `LoginScreen`, `RegisterScreen`, `BookCatalogScreen`, `ResearchCatalogScreen`, `BookCartScreen`

**Step 1: BookDetailScreen**

```kotlin
} catch (error: ApiException) {
    Toast.makeText(
        context,
        userFacingMessage(error, "Unable to reserve this title."),
        Toast.LENGTH_LONG,
    ).show()
} catch (error: Exception) {
    Toast.makeText(
        context,
        userFacingMessage(error, "Unable to reserve this title."),
        Toast.LENGTH_LONG,
    ).show()
}
```

**Step 2: GlobalSearchScreen**

On refresh failure:

```kotlin
val err = bookResult.exceptionOrNull() ?: researchResult.exceptionOrNull()
apiError = err?.let {
    userFacingMessage(it, "Unable to refresh search results.")
}?.takeUnless { predictions.isNotEmpty() && it.contains("session expired", ignoreCase = true) }
```

Prefer: if local predictions non-empty and error is session/JWT, **set `apiError = null`** (silent keep local). If empty predictions, show session or generic message.

**Step 3: List screens using `lastError`**

No change required if Task 4 stores friendly text. Spot-check banners still bind `loadError` / `lastError`.

---

### Task 6: Login copy polish (small)

**Files:**
- Modify: `app/src/main/java/auth/LoginScreen.kt` (optional placeholder/banner consistency)
- Modify: `app/src/main/java/ui/feedback/UserFacingErrors.kt` (already “school ID” in Task 2)
- Modify: `app/src/main/java/network/ApiClient.kt` — ensure login `INVALID_CREDENTIALS` from API still surfaces via code, not HTTP 401 generic

**Step 1:** Login catch fallbacks use `"Invalid school ID or password."` to match map.

**Step 2:** Register field-error path unchanged; ensure `AUTH_VALIDATION_FAILED` banner uses `userFacingMessage` (already) + field errors.

---

### Task 7: Verification

**Step 1: Unit tests**

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.UserFacingErrorsTest" --console=plain
```

Expected: all PASS.

**Step 2: Manual / emulator**

- Sign in with wrong password → school ID/password wording (not stack/Bearer).
- Open Search while signed out in preview → no “Bearer token” banner; local predictions OK.
- Force a profile/reserve failure if possible → banner/toast is plain language.
- After session clear (`JWT_REQUIRED`), next action should say session expired, not invalid password.

**Step 3: Compile full debug**

```powershell
.\gradlew.bat :app:compileDebugKotlin --console=plain
```

---

## Done when

- [ ] No UI path shows “Bearer”, “JWT”, “Route not found”, or “Missing account id”
- [ ] Non-login 401/403 never say “Invalid student ID or password” / deactivated unless that is the real code
- [ ] `lastError` / `profileActionError` / BookDetail toast / Search use `userFacingMessage`
- [ ] `UserFacingErrorsTest` passes
- [ ] Login invalid credentials still clear and correct

## Out of scope

- Backend message rewrites on Vercel
- New error UI components (keep `ElmsBanner` / Toast)
- Per-screen `lastError` isolation (nice follow-up; not required here)
