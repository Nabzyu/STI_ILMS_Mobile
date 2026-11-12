---
title: Authentication and session
tags: [auth, security, session]
---

# Authentication and session

Back to [index](README.md). Related: [Networking and API](networking-and-api.md), [Error handling](error-handling.md).

## Roles

`auth/UserType.kt` maps the server's role string:

| Server role | `UserType` | Label | `isStaff` |
|---|---|---|---|
| `Admin`, `Librarian`, `System Administrator` | `ADMIN` | Librarian | yes |
| `Faculty` | `FACULTY` | Faculty | no |
| anything else / null | `STUDENT` | Student | no |

**Librarian, Admin and desk Staff accounts can't use the app yet** (web team decision): the staff side of the app is still placeholders, so they use the website. The sign-in screen says "Librarians and library staff: please use the library website." `UserType.isStaffAccountRole` guards sign-in (`STAFF_ACCOUNT_NOT_SUPPORTED`) and drops a staff session saved by an earlier app version. The staff home screen and `ADMIN` type stay in the code for later. Parked work: librarian sign-in on `archive/librarian-sign-in`, desk staff on `archive/desk-staff-support`. Covered by `UserTypeTest`.

## Sign in — students and faculty only

The server has a **user** portal (Student, Faculty) and a **staff** portal (Admin, Librarian, Staff). A role/portal mismatch is answered with the same `INVALID_CREDENTIALS` as a wrong password and counts against the account's budget of **8 wrong attempts per 10 minutes** (then `429 LOGIN_PAUSED`). The app only uses the user portal, so each press costs at most one attempt. A librarian who tries the app anyway sees "Invalid school ID or password" (the server can't say it's the wrong portal) and uses one attempt.

`LibraryRepository.login(schoolId, password)`:
1. Normalises the school ID (trim, upper-case) and clears any previous session, so no stale `Authorization` header is sent.
2. `POST api/v1/auth/login` with `{school_id, password, portal: "user"}`.
3. `interpretLoginData` (`data/LoginFlow.kt`) decides what came back:
   - **Student / Faculty:** `{otpRequired, challengeId, emailHint, expiresIn: 600, resendAfter: 60}` → `LoginOutcome.CodeRequired`. The login screen switches to the code step (`auth/LoginCodeStep.kt`).
   - A token payload (not expected from the user portal) → `completeSignIn` saves token + identity, sets `session` and starts idle tracking, unless the role is a staff account.
   - `deviceSwitchRequired` (only when the server's device sign-in feature is on) → `DEVICE_SWITCH_REQUIRED` error; the app can't finish that flow.
4. Code step: `verifyLoginCode` → `POST api/v1/auth/login/verify` `{challenge_id, code}` (6 digits) returns the normal token payload. "Resend code" → `POST api/v1/auth/login/resend` `{challenge_id}`, disabled for the 60-second cooldown (countdown on the button). "Back to sign in" or the system back button returns to the password form. A wrong code (`LOGIN_CODE_INVALID`) shows under the field; `LOGIN_CODE_PAUSED` (3 wrong codes) and `LOGIN_CHANGED` send the user back to the password form.

Client-side validation in `LoginScreen` (`auth/LoginRules.kt`): school ID must match the server rule `^[A-Z0-9][A-Z0-9._-]{2,49}$` after upper-casing (e.g. `02000123456`); password required. Server field errors (`school_id`, `password`) are shown under the fields. On success the user goes to `dashboard/{type}` and the login screen is removed from the back stack.

## Password reset

"Forgot password?" under the Sign in button opens `ForgotPasswordScreen` (`forgot_password`). It mirrors the web `PasswordRecoveryPage`:

1. **Request:** school ID or school email → `POST api/v1/auth/password-recovery/request` `{identifier}`. The server always returns a `challengeId` and a neutral "if an account matches…" message, so the app can't tell whether an email was sent.
2. **Verify:** 6-digit code (reuses `LoginCodeStep`) → `/password-recovery/verify` `{challenge_id, code}` → `resetToken` (valid 5 minutes, kept in memory only). "Resend code" is a new request (new challenge), enabled after 60 seconds. Three wrong codes → `RECOVERY_LOCKED` for 15 minutes, back to step 1.
3. **Reset:** new password + confirmation (`newPasswordError`: 8+ characters, at most 72 UTF-8 bytes, must match) → `/password-recovery/reset` `{challenge_id, reset_token, password, confirm_password}`. `PASSWORD_UNCHANGED` shows under the field; `RECOVERY_TOKEN_INVALID` restarts. Success signs the account out everywhere (server bumps `auth_version`).

**Who can reset:** only **Student** accounts in the app. The server also resets desk **Staff** accounts (`portal: "staff"`), but desk staff aren't supported in the app yet, so that option is parked on `archive/desk-staff-support`. Faculty, Librarian and Admin accounts never receive a code; the screen tells them to contact the library. These endpoints are treated as sign-in requests (`isAuthHandshakePath`): no bearer token, and their errors never end a session.

## Activate account (students only)

The server retired `POST /auth/register` (it always returns `410 REGISTRATION_FLOW_REPLACED`). New students now **activate** an account that is already on the semester enrollment roster, like the website's roster sign-up. "New student? Activate your account" on the sign-in screen opens `ActivateAccountScreen` (`activate_account`):

1. `GET api/v1/auth/roster-registration` → `{enabled, open}`. Not enabled → "temporarily unavailable" with Try again; enabled but no open semester → "opens when the library starts the semester" (`activationAvailability` in `data/LoginFlow.kt`).
2. **Student ID** → `POST …/roster-registration/begin` `{school_id}`. The server emails a code to the **school email on the roster** (so no email field, and the old `@stu.sti.edu.ph` rule is gone). It answers the same whether or not the ID is eligible or already has an account.
3. **Code** (reuses `LoginCodeStep`) → `…/verify` `{challenge_id, code}` → a short-lived `grant`, kept in memory only. Resend is a new `begin`, enabled after 60 seconds.
4. **Password** (twice; `newPasswordError`) → `…/complete` `{challenge_id, grant, password, confirm_password}`. The account is created **active**; the app returns to sign-in with the Student ID filled in and "Your account is active…".

Errors: `STUDENT_ID_INVALID`, `ROSTER_CODE_INVALID` (back to step 2 if it happens at the password step), `ROSTER_PASSWORD_INVALID`, `ROSTER_REGISTRATION_CLOSED`. Faculty and staff accounts are set up by the library. These endpoints are treated as sign-in requests (no bearer token).

## Session storage — `session/SessionStore.kt`

- Stores `token`, `account_id`, `school_id`, `full_name`, `role` in `EncryptedSharedPreferences` (`smartlib_session`, AES-256 GCM/SIV via `MasterKey`).
- **Fallback:** if the encrypted store cannot be created, it logs a warning and uses plain `smartlib_session_fallback` prefs. The token is then stored unencrypted on that device.
- Writes use `commit()` so the token is readable before the very next request.
- `getSession()` returns null unless token, account id ≥ 0, school ID and role are all present.

## Idle limit and session renewal

The server ends a session after **10 minutes without activity**; only `POST api/v1/auth/activity` (Bearer) refreshes it, returning `{token, idleSeconds}`. The app follows the web client's `SessionActivity.tsx`:

- **What counts as activity:** any touch or key press (`MainActivity.onUserInteraction` → `LibraryRepository.onUserInteraction`). Normal API calls do not count, on the server or in the app.
- **When it pings:** at most once every 30 seconds, always following the latest interaction (a throttled touch schedules one ping for when the 30 seconds are up). The renewed token replaces the stored one (`SessionStore.updateToken`). Offline or failed pings are retried on the next touch.
- **Local expiry:** if 10 minutes pass with no interaction, the next touch, or the app returning to the foreground (`MainActivity.onStart` → `checkIdleTimeout`), signs the user out with "You were signed out after 10 minutes of inactivity." The logic is in `session/SessionActivityTracker.kt` (unit-tested).
- **After a restore** the app pings once, because `/auth/me` does not refresh the server's idle window.
- Unlike the web app, there is no 60-second countdown warning.

## Restore, expiry and logout

- **Restore:** at launch, `restoreSession()` calls `/auth/me`, then `/auth/activity`. On success it refreshes stored identity. On any `ApiException` it clears everything and returns false; a session-ending code is shown on the login screen.
- **Server-side invalidation:** `SESSION_EXPIRED`, `SESSION_UPGRADE_REQUIRED`, `JWT_REQUIRED`, `JWT_INVALID`, `ACCOUNT_ACCESS_REVOKED` and `ACCOUNT_DEACTIVATED` (`network/SessionErrors.kt`), or a bare 401 from any non-sign-in endpoint, end the session. `ApiClient.unwrap` clears the stored session and calls `ApiClient.onSessionEnded` (wired in `STIOrmocLibraryApp`) → `LibraryRepository.onServerSessionEnded` clears in-memory data and `session`. `AppNavGraph` watches `session.isAuthenticated` and returns to `login`, which shows the reason (`sessionEndedMessage`). Errors from the sign-in endpoints never end a session. `JWT_ROLE_FORBIDDEN` (a 403 for one resource) does not sign out.
- **One session per account:** a sign-in on the web (or another phone) revokes this one; the app sees `ACCOUNT_ACCESS_REVOKED` or `SESSION_EXPIRED` and returns to login.
- **Logout:** clears stored session, all in-memory lists and the cart; navigation pops to `login`.
