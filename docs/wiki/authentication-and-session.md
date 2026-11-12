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

Librarians (`isStaff`) land on `staff_home`. **Library desk staff (`Staff`, `Library Staff`) are not supported in the app yet** (decision by the web team): `UserType.isDeskStaffRole` catches them, sign-in stops with "Library desk staff accounts can't use the app yet. Please use the library website." (`DESK_STAFF_NOT_SUPPORTED`), and a restored desk-staff session is dropped. Because the server has already created a session by then, a desk-staff sign-in attempt from the app still signs that account out of the website. Desk-staff support is parked on branch `archive/desk-staff-support`. Covered by `UserTypeTest`.

## Sign in — two portals, one attempt per press

The server has a **user** portal (Student, Faculty) and a **staff** portal (Admin, Librarian, and desk Staff, which the app does not support yet). A role/portal mismatch is answered with the same `INVALID_CREDENTIALS` as a wrong password and counts against the account's budget of **8 wrong attempts per 10 minutes** (then `429 LOGIN_PAUSED`). Sending no `portal` field doesn't help: the server then defaults to `user` (`validateRoleLogin` in `account-auth.validation.ts`). So the app no longer tries both portals. Like the web app's separate `/login` and `/staff` pages, the login screen has a **"Use librarian sign in" / "Use student sign in"** switch. The portal last used for a school ID (`SessionStore.preferredLoginPortal`) is pre-selected while the user types that ID, unless they used the switch.

`LibraryRepository.login(schoolId, password, staffPortal)`:
1. Normalises the school ID (trim, upper-case) and clears any previous session, so no stale `Authorization` header is sent.
2. `POST api/v1/auth/login` with `{school_id, password, portal}`.
3. `interpretLoginData` (`data/LoginFlow.kt`) decides what came back:
   - **Staff roles:** a token payload → `completeSignIn` saves token + identity, sets `session`, remembers the portal, starts idle tracking. A non-staff role from the staff portal is rejected with `ADMIN_ROLE_REQUIRED`.
   - **Student / Faculty:** `{otpRequired, challengeId, emailHint, expiresIn: 600, resendAfter: 60}` → `LoginOutcome.CodeRequired`. The login screen switches to the code step (`auth/LoginCodeStep.kt`).
   - `deviceSwitchRequired` (only when the server's device sign-in feature is on) → `DEVICE_SWITCH_REQUIRED` error; the app can't finish that flow.
4. Code step: `verifyLoginCode` → `POST api/v1/auth/login/verify` `{challenge_id, code}` (6 digits) returns the normal token payload. "Resend code" → `POST api/v1/auth/login/resend` `{challenge_id}`, disabled for the 60-second cooldown (countdown on the button). "Back to sign in" or the system back button returns to the password form. A wrong code (`LOGIN_CODE_INVALID`) shows under the field; `LOGIN_CODE_PAUSED` (3 wrong codes) and `LOGIN_CHANGED` send the user back to the password form.

Client-side validation in `LoginScreen` (`auth/LoginRules.kt`): school ID must match the server rule `^[A-Z0-9][A-Z0-9._-]{2,49}$` after upper-casing (e.g. `02000123456`, `ADMIN-PORTAL-001`); password required. Server field errors (`school_id`, `password`) are shown under the fields. On success the user goes to `staff_home` or `dashboard/{type}` and the login screen is removed from the back stack.

## Password reset

"Forgot password?" under the Sign in button (student sign-in only) opens `ForgotPasswordScreen` (`forgot_password`). It mirrors the web `PasswordRecoveryPage`:

1. **Request:** school ID or school email → `POST api/v1/auth/password-recovery/request` `{identifier}`. The server always returns a `challengeId` and a neutral "if an account matches…" message, so the app can't tell whether an email was sent.
2. **Verify:** 6-digit code (reuses `LoginCodeStep`) → `/password-recovery/verify` `{challenge_id, code}` → `resetToken` (valid 5 minutes, kept in memory only). "Resend code" is a new request (new challenge), enabled after 60 seconds. Three wrong codes → `RECOVERY_LOCKED` for 15 minutes, back to step 1.
3. **Reset:** new password + confirmation (`newPasswordError`: 8+ characters, at most 72 UTF-8 bytes, must match) → `/password-recovery/reset` `{challenge_id, reset_token, password, confirm_password}`. `PASSWORD_UNCHANGED` shows under the field; `RECOVERY_TOKEN_INVALID` restarts. Success signs the account out everywhere (server bumps `auth_version`).

**Who can reset:** only **Student** accounts in the app. The server also resets desk **Staff** accounts (`portal: "staff"`), but desk staff aren't supported in the app yet, so that option is parked on `archive/desk-staff-support`. Faculty, Librarian and Admin accounts never receive a code; the screen tells them to contact the library, and the link is hidden on librarian sign-in. These endpoints are treated as sign-in requests (`isAuthHandshakePath`): no bearer token, and their errors never end a session.

## Registration (students only)

`RegisterScreen` → `LibraryRepository.registerStudent` → `POST api/v1/auth/register`.

Client checks (unchanged, still the old rules — see B3/M5 in [Web vs mobile](web-vs-mobile-comparison.md)): 11-digit student ID; first/last name; email matching the STI student domain (`name@stu.sti.edu.ph`); program/strand; year level; password ≥ 8 chars; confirmation matches. Program options and year levels come from `auth/ProgramOptions.kt`:
- Senior High strands STEM, ABM, TVL → Grade 11–12
- College BSIT, BSTM, BSHM → 1st–4th Year

Registration does **not** sign the user in; on success it returns to `login`. Server field errors map back onto the form.

## Session storage — `session/SessionStore.kt`

- Stores `token`, `account_id`, `school_id`, `full_name`, `role` in `EncryptedSharedPreferences` (`smartlib_session`, AES-256 GCM/SIV via `MasterKey`).
- **Fallback:** if the encrypted store cannot be created, it logs a warning and uses plain `smartlib_session_fallback` prefs. The token is then stored unencrypted on that device.
- Writes use `commit()` so the token is readable before the very next request.
- `getSession()` returns null unless token, account id ≥ 0, school ID and role are all present.
- Login-portal hints live in a separate plain prefs file `smartlib_login_hints` (school ID → `user`/`staff`).

## Idle limit and session renewal

The server ends a session after **10 minutes without activity**; only `POST api/v1/auth/activity` (Bearer) refreshes it, returning `{token, idleSeconds}`. The app follows the web client's `SessionActivity.tsx`:

- **What counts as activity:** any touch or key press (`MainActivity.onUserInteraction` → `LibraryRepository.onUserInteraction`). Normal API calls do not count, on the server or in the app.
- **When it pings:** at most once every 30 seconds, always following the latest interaction (a throttled touch schedules one ping for when the 30 seconds are up). The renewed token replaces the stored one (`SessionStore.updateToken`). Offline or failed pings are retried on the next touch.
- **Local expiry:** if 10 minutes pass with no interaction, the next touch, or the app returning to the foreground (`MainActivity.onStart` → `checkIdleTimeout`), signs the user out with "You were signed out after 10 minutes of inactivity." The logic is in `session/SessionActivityTracker.kt` (unit-tested).
- **After a restore** the app pings once, because `/auth/me` does not refresh the server's idle window.
- Unlike the web app, there is no 60-second countdown warning.

## Stay signed in (30 days, Student/Faculty)

Requires the server change in `docs/plans/server-patches/` (migration `037_mobile_sessions.sql`, `/auth/refresh`, `/auth/logout`). Until it is deployed, the server ignores `client` and sends no key, so the app behaves as before.

- Every sign-in sends `client: "mobile"`. The server keeps **one website session and one phone session** per account: a website sign-in no longer signs the phone out, and vice versa. A second phone sign-in replaces the first.
- Student/Faculty sign-ins get a `refreshToken` valid for **30 days from sign-in** (rotating it does not extend this). It is stored only in the encrypted prefs (`SessionStore.getRefreshToken`). Librarians get none and keep the 10-minute idle sign-out.
- `network/SessionRefreshInterceptor.kt`: when a request fails with `SESSION_EXPIRED` or `JWT_INVALID`, it calls `/auth/refresh` once (one refresh at a time), saves the new token and key, and retries the request. A rejected key passes the 401 through, so the normal sign-out applies. If the server can't be reached, the request fails as offline and the key is kept.
- The server rotates the key on every refresh. Reusing a replaced key ends the session (treated as stolen). Password reset, deactivation, role change and semester end also stop it.
- With a key, the local 10-minute idle rule does not sign out; the next request renews the session. Opening the app offline with a key shows the saved account and renews once online.
- Sign out calls `/auth/logout` with the key, so it stops working immediately.

## Restore, expiry and logout

- **Restore:** at launch, `restoreSession()` calls `/auth/me`, then `/auth/activity`. On success it refreshes stored identity. On any `ApiException` it clears everything and returns false; a session-ending code is shown on the login screen.
- **Server-side invalidation:** `SESSION_EXPIRED`, `SESSION_UPGRADE_REQUIRED`, `JWT_REQUIRED`, `JWT_INVALID`, `ACCOUNT_ACCESS_REVOKED` and `ACCOUNT_DEACTIVATED` (`network/SessionErrors.kt`), or a bare 401 from any non-sign-in endpoint, end the session. `ApiClient.unwrap` clears the stored session and calls `ApiClient.onSessionEnded` (wired in `STIOrmocLibraryApp`) → `LibraryRepository.onServerSessionEnded` clears in-memory data and `session`. `AppNavGraph` watches `session.isAuthenticated` and returns to `login`, which shows the reason (`sessionEndedMessage`). Errors from the sign-in endpoints never end a session. `JWT_ROLE_FORBIDDEN` (a 403 for one resource) does not sign out.
- **One session per account:** a sign-in on the web (or another phone) revokes this one; the app sees `ACCOUNT_ACCESS_REVOKED` or `SESSION_EXPIRED` and returns to login.
- **Logout:** clears stored session, all in-memory lists and the cart; navigation pops to `login`.
