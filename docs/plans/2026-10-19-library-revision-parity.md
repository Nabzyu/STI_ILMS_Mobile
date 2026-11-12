# Library Revision Parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep the Android app working against the API on `JohnBazty/StiOrmocLibrary` branch `codex/library-revision-updates`: sessions that survive normal use, a clean sign-out when they end, no calls to routes the branch removed, and password recovery.

**Architecture:** Same layers as today: `LibraryApi` (Retrofit) → `ApiClient.unwrap` → `LibraryRepository` → Compose screens. Session renewal lives at the app root (`MainActivity`), not per screen. Password recovery reuses `EmailCodeStep` from the P0 auth work.

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit + Gson, OkHttp, JUnit4 (JVM unit tests in `app/src/test`).

**Spec:** No separate spec. The source of truth is the branch's server code, cited per task (paths relative to `apps/api/src/` in the web repo).

**Prerequisite:** The P0 auth work (login OTP plus registration `school_email` and code verify) is still uncommitted. Commit it before Task 1 so each task diffs cleanly.

## Global Constraints

- Branch idle limit: every `/api/v1/*` call returns `401 SESSION_EXPIRED` once `last_activity_at` is more than 10 minutes old (`modules/auth/jwt-auth.middleware.ts:48`).
- Branch JWT lifetime: 900 s (`config/env.js:92`). Only `POST /api/v1/auth/activity` issues a fresh token: `{ data: { token, idleSeconds: 600 } }`.
- Tokens issued before the branch carry no `sessionId` and get `401 SESSION_UPGRADE_REQUIRED` (`jwt-auth.middleware.ts:46`).
- User-facing copy goes through `userFacingMessage`. Never show raw codes or Bearer text.
- No new dependencies.

## Review Focus

1. **App left idle in the foreground for more than 10 minutes:** the next action signs the user out to Login with the session-expired copy, not an error banner on a dead dashboard. Covered by the Task 1 test.
2. **Upgrading the app with an old stored token:** the first call after launch gets `SESSION_UPGRADE_REQUIRED`, the user lands on Login, and the app does not crash or loop. Covered by the Task 1 test.
3. **User actively tapping for more than 15 minutes:** the token renews before its JWT `exp`, with no surprise sign-out. Covered by the Task 2 test.
4. **Renewal call fails offline:** no sign-out on an `IOException`; sign out only on 401/403. Covered by the Task 2 test.
5. **Recovery with an unknown School ID:** the server still returns its neutral message and a `challengeId`, and the app shows the code step without revealing whether the account exists. Covered by the Task 4 test.

---

### Task 1: Treat session expiry as sign-out

**Files:**
- Modify: `app/src/main/java/network/ApiClient.kt` (the `shouldClear` set in `unwrap`)
- Modify: `app/src/main/java/ui/feedback/UserFacingErrors.kt` (the `"JWT_REQUIRED", …` line)
- Modify: `app/src/main/java/navigation/NavGraph.kt`
- Test: `app/src/test/java/com/example/stismartlib/SessionEndTest.kt`

**Interfaces:**
- Produces: `ApiClient.isSessionEndCode(code: String): Boolean`. Produces `ApiClient.sessionEnded: StateFlow<Boolean>`, which is `true` after a session-ending error and reset to `false` by `ApiClient.acknowledgeSessionEnded()`.

- [ ] **Step 1: Write the failing test.** `SessionEndTest.sessionEndCodes()` asserts `isSessionEndCode` is true for `"SESSION_EXPIRED"`, `"SESSION_UPGRADE_REQUIRED"`, `"JWT_REQUIRED"`, `"JWT_INVALID"`, `"ACCOUNT_ACCESS_REVOKED"`, `"ACCOUNT_DEACTIVATED"`, and false for `"INVALID_CREDENTIALS"` and `"REQUEST_FAILED"`.
- [ ] **Step 2: Run `.\gradlew.bat :app:testDebugUnitTest --tests "com.example.stismartlib.SessionEndTest"`.** Expected: FAIL, unresolved `isSessionEndCode`.
- [ ] **Step 3: Implement.** Replace the inline `setOf(...)` in `unwrap` with `isSessionEndCode(code)`. On a match, clear `sessionStore` and set `sessionEnded` to true. Do not trigger for calls under `/auth/` (a wrong login code must not count as a session end); `unwrap` gets the path from `response.raw().request.url.encodedPath`. In `UserFacingErrors.kt`, add `"SESSION_EXPIRED"` and `"SESSION_UPGRADE_REQUIRED"` to the `SESSION_EXPIRED` copy branch. In `AppNavGraph`, collect `ApiClient.sessionEnded`. When it is true: call `repository.logout()`, navigate to `Screen.Login.route` with `popUpTo(0) { inclusive = true }`, then call `acknowledgeSessionEnded()`.
- [ ] **Step 4: Run the test again.** Expected: PASS.
- [ ] **Step 5: Commit** `fix(auth): sign out on branch session expiry codes`.

### Task 2: Keep active sessions alive with `auth/activity`

Mirrors the web's `features/auth/SessionActivity.tsx`: renew only while the user is interacting, so an idle phone still expires after 10 minutes, as the web does.

**Files:**
- Modify: `app/src/main/java/network/LibraryApi.kt` (add `@POST("api/v1/auth/activity") suspend fun authActivity(): Response<ApiEnvelope<SessionActivityDto>>`)
- Modify: `app/src/main/java/network/dto/AuthDtos.kt` (add `data class SessionActivityDto(val token: String? = null, @SerializedName(value = "idle_seconds", alternate = ["idleSeconds"]) val idleSeconds: Int? = null)`)
- Modify: `app/src/main/java/session/SessionStore.kt` (add `fun updateToken(token: String)`, which rewrites `KEY_TOKEN` only)
- Modify: `app/src/main/java/data/LibraryRepository.kt` (add `suspend fun renewSession(): Boolean`)
- Create: `app/src/main/java/session/SessionKeepAlive.kt`
- Modify: `app/src/main/java/com/example/stismartlib/MainActivity.kt` (root content)
- Test: `app/src/test/java/com/example/stismartlib/SessionKeepAliveTest.kt`

**Interfaces:**
- Consumes: `ApiClient.sessionEnded` (Task 1) through `unwrap`.
- Produces: `fun shouldRenew(nowMs: Long, lastInteractionMs: Long, lastRenewMs: Long): Boolean` in `SessionKeepAlive.kt`. Produces `@Composable fun SessionKeepAlive(repository: LibraryRepository, content: @Composable () -> Unit)`.

- [ ] **Step 1: Write the failing test.** `SessionKeepAliveTest` asserts:
  - `shouldRenew(now = 200_000, lastInteraction = 190_000, lastRenew = 0)` is true (active, never renewed).
  - `shouldRenew(200_000, 190_000, 150_000)` is false (renewed under 2 minutes ago).
  - `shouldRenew(200_000, 100_000, 0)` is false (no interaction in the last 60 s).
- [ ] **Step 2: Run the test.** Expected: FAIL, unresolved `shouldRenew`.
- [ ] **Step 3: Implement.**
  - `shouldRenew`: `now - lastInteraction < 60_000 && now - lastRenew >= 120_000`.
  - `SessionKeepAlive`: wraps `content` in a `Box` whose `Modifier.pointerInput(Unit)` uses `awaitPointerEventScope` with `PointerEventPass.Initial` to record `lastInteraction`. It must observe without consuming.
  - A `LaunchedEffect` loop runs every 30 s while `repository.session.isAuthenticated` and calls `repository.renewSession()` when `shouldRenew` is true.
  - `renewSession`: unwraps `authActivity()`, calls `sessionStore.updateToken(token)`, returns `true`, and returns `false` on `IOException`. A 401 flows through `unwrap` into Task 1's sign-out. In `MainActivity`, wrap the existing `AppNavGraph(...)` call in `SessionKeepAlive(repository)`.
- [ ] **Step 4: Run the test.** Expected: PASS.
- [ ] **Step 5: Manual check.** Sign in against a branch deploy, use the app for more than 15 minutes, and confirm there is no sign-out. Then leave it idle for 11 minutes, tap once, and confirm you land on Login.
- [ ] **Step 6: Commit** `feat(auth): renew branch session while the user is active`.

### Task 3: Remove calls to routes the branch dropped

The branch has no `GET fines/policy` (its `fines/terms` returns academic terms, not rates). It has no `library-calendar/day`. Its reservation create ignores `preferredPickupDate` (`modules/reservations/book-request.service.ts:170-177` reads only `bookTitleId`/`materialId`/`requestId`). The web shows neither feature to students.

**Decided (2026-10-19):** delete the fine-rate card and the preferred-day sheet. They can never show real data on this branch.

**Files:**
- Modify: `app/src/main/java/network/LibraryApi.kt` (remove `fines/policy` and `library-calendar/day`)
- Modify: `app/src/main/java/network/dto/LibraryDtos.kt` (remove `FinePolicyDto` and `LibraryDayDto`; drop `preferredPickupDate` from `ReserveRequestDto`)
- Modify: `app/src/main/java/data/LibraryRepository.kt` (remove `finePolicy`, `finePolicyUnavailable`, `loadFinePolicy`, `applyPreviewFinePolicy`, `checkLibraryDay`, and the `preferredPickupDate` param of `reserveBook`)
- Modify: `app/src/main/java/dashboard/account/FinesScreen.kt` (remove the policy card and its `loadFinePolicy` calls)
- Delete: `app/src/main/java/dashboard/reserve/ReservePreferredDaySheet.kt`
- Modify: `app/src/main/java/dashboard/catalog/BookDetailScreen.kt` (the reserve button calls `reserveBook(book)` directly)
- Modify: `app/src/main/java/dashboard/reserve/ReserveBookScreen.kt:187-194` and `dashboard/overview/DashboardOverviewScreen.kt:334` (drop the preferred-day rows)
- Modify: `app/src/main/java/data/models/LibraryModels.kt` (remove `FinePolicy`, `LibraryDayStatus`, and `Reservation.preferredPickupDate`)
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt` and `PreviewScaffold.kt` (remove fine-policy preview data)
- Delete: `app/src/test/java/com/example/stismartlib/FinePolicyDtoParsingTest.kt`

- [ ] **Step 1: Delete the items listed above.** Then run `grep -rn "FinePolicy\|LibraryDay\|preferredPickupDate\|checkLibraryDay" app/src`. Expected: no matches.
- [ ] **Step 2: Run `.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug`.** Expected: BUILD SUCCESSFUL.
- [ ] **Step 3: Visual check** with `android-visual-verification`: `FinesScreenPreview` and the book detail preview in light and dark. There must be no empty gap where the removed card was.
- [ ] **Step 4: Commit** `refactor: drop fine-policy and preferred-day calls the API no longer serves`.

### Task 4: Forgot-password flow

Server: `POST /api/v1/auth/password-recovery/{request,verify,reset}` (`modules/auth/password-recovery.service.ts`). It is mounted before the JWT gate.

| Step | Body | `data` |
|---|---|---|
| request | `{ identifier }` (School ID or school email) | `{ challengeId, message }`. Always neutral, even for unknown accounts. |
| verify | `{ challenge_id, code }` | `{ resetToken, expiresIn }` |
| reset | `{ challenge_id, reset_token, password, confirm_password }` | `{ message }` |

Errors arrive as `RECOVERY_CODE_INVALID`, `RECOVERY_LOCKED`, `RECOVERY_TOKEN_INVALID`, `PASSWORD_UNCHANGED` and `AUTH_VALIDATION_FAILED`. Their server messages are safe to show through `userFacingMessage`.

**Files:**
- Modify: `app/src/main/java/network/dto/AuthDtos.kt`
- Modify: `app/src/main/java/network/LibraryApi.kt`
- Modify: `app/src/main/java/data/LibraryRepository.kt`
- Create: `app/src/main/java/auth/ForgotPasswordScreen.kt`
- Modify: `app/src/main/java/navigation/Screen.kt` (`object ForgotPassword : Screen("forgot_password")`)
- Modify: `app/src/main/java/navigation/NavGraph.kt` (add a `composable(Screen.ForgotPassword.route)`)
- Modify: `app/src/main/java/auth/LoginScreen.kt` (add a "Forgot password?" link under the password field)
- Modify: `app/src/main/java/ui/preview/ScreenPreviews.kt` (add light and dark previews for each step)
- Test: `app/src/test/java/com/example/stismartlib/AuthDtoParsingTest.kt`

**Interfaces:**
- Consumes: `EmailCodeStep` (`auth/EmailCodeStep.kt`, from P0).
- Produces the DTOs:
  - `RecoveryRequest(val identifier: String)`
  - `RecoveryStartDto(challengeId: String?, message: String?)`
  - `RecoveryVerifyRequest(@SerializedName("challenge_id") challengeId: String, code: String)`
  - `RecoveryVerifyDto(@SerializedName(value = "reset_token", alternate = ["resetToken"]) resetToken: String?)`
  - `RecoveryResetRequest(@SerializedName("challenge_id") challengeId, @SerializedName("reset_token") resetToken, password, @SerializedName("confirm_password") confirmPassword)`
  - `RecoveryDoneDto(message: String?)`
- Produces the repository functions:
  - `suspend fun startPasswordRecovery(identifier: String): RecoveryStartDto`
  - `suspend fun verifyRecoveryCode(challengeId: String, code: String): String` (returns the reset token)
  - `suspend fun resetPassword(challengeId: String, resetToken: String, password: String, confirm: String): String` (returns the message)
- Produces the route `Screen.ForgotPassword`.

- [ ] **Step 1: Write the failing tests** in `AuthDtoParsingTest`:
  - `recoveryStartParses`: JSON `{"success":true,"data":{"challengeId":"6f1c…","message":"If an active student account matches…"}}` parses to a non-null `challengeId`.
  - `recoveryVerifyParsesResetToken`: `{"data":{"resetToken":"<64 hex>","expiresIn":600}}` gives `resetToken` equal to the 64-hex string.
  - `recoveryResetSerializesSnakeCase`: `gson.toJson(RecoveryResetRequest(...))` contains `"reset_token"` and `"confirm_password"`.
- [ ] **Step 2: Run `--tests "com.example.stismartlib.AuthDtoParsingTest"`.** Expected: FAIL, unresolved DTOs.
- [ ] **Step 3: Implement** the DTOs, the 3 `@POST` endpoints and the 3 repository functions. Each repository function follows the `withContext(Dispatchers.IO) { ApiClient.unwrap(...) }` pattern of `verifyRegistrationCode`.
- [ ] **Step 4: Run the tests.** Expected: PASS.
- [ ] **Step 5: Build `ForgotPasswordScreen(navController)`.** It has three local steps in one screen, the same shape as `RegisterScreen`:
  1. A `PortalField` labelled "School ID or school email".
  2. `EmailCodeStep`, whose instructions are the server's neutral `message`. Resend calls `startPasswordRecovery` again with the same identifier and cooldown 60.
  3. New password and confirm fields, client-checked for at least 8 characters and a match.

  On success, show an `ElmsBanner(Success)` with the server message and a "Back to Login" button. Add the link in `LoginScreen`, the route, and the previews.
- [ ] **Step 6: Visual check** with `android-visual-verification`: all three steps in light and dark, plus the code step at 1.3× font.
- [ ] **Step 7: Commit** `feat(auth): password recovery via school email code`.

---

## Not in this plan (checked, deliberately skipped)

- **My books** (`borrowing/my-books`): this is `borrowing/history` with one row per title (`modules/circulation/circulation.service.ts:214-234`). The history screen already covers it.
- **Server-side cart** (`borrow/cart-state`), **dashboard / workspace tasks**, **floor plan**, **invoices**, **print pricing**, **categories**, **clearance and notification schedules**, **avatar change requests**, **copy lookup by barcode**, **claim pickup**: these are not needed for students to keep working. Revisit one at a time if users ask.
