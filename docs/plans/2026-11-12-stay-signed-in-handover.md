# Handover: stay signed in

Date: 2026-11-12. Branch: `feat/stay-signed-in` (built on `feat/password-reset`, which is built on `claude/fix-sign-in-flow`). Nothing is merged to `main`. PR #1 (`feat/activate-account` → `main`) covers the sign-in work without "stay signed in".

## Decisions from the web team (the user is on the web team)

- Students and faculty only for "stay signed in". Librarians and library desk staff can't sign in to the app in the first release (librarian sign-in parked on `archive/librarian-sign-in`, desk staff on `archive/desk-staff-support`; merged in from `feat/password-reset`).
- Refresh key lifetime: **30 days** from sign-in, not extended by use.
- **One website session and one phone session per account** (a website sign-in must not sign the phone out).
- `LOGIN_DEVICES_ENABLED` stays off. Wrong-portal sign-in stays as it is (counts as a wrong password).
- **Fingerprint unlock: archived** (built, then parked on `archive/fingerprint-unlock`).
- Do not push to the server repo (JohnBazty/stiormoclibrary) without the user's explicit permission. Hand over patches instead.

## Done in this branch (app)

- `network/SessionRefreshInterceptor.kt` + tests: refresh on `SESSION_EXPIRED`/`JWT_INVALID`, single-flight, retry once.
- `SessionStore` stores the refresh key (encrypted prefs); `ApiClient.requestRefresh` calls `/auth/refresh`.
- Sign-in sends `client: "mobile"`; repository saves the key, skips the local idle sign-out when a key exists, calls `/auth/logout` on sign-out, and opens offline with the saved identity when a key exists.
- 35 JVM unit tests pass (run outside Gradle in the original session; the Android SDK was not available there).

## Server patches (not applied anywhere yet)

`docs/plans/server-patches/` holds `git format-patch` output against StiOrmocLibrary `main` at `75b4032`:

1. `0001-…activity-401…`: `SESSION_EXPIRED` / `SESSION_UPGRADE_REQUIRED` codes on `/auth/activity` 401s, plus "used by the Android app" comments.
2. `0002-…mobile-refresh-sessions…`: migration `037_mobile_sessions.sql`, per-client session revocation (no `auth_version` bump on a normal sign-in; restoration still bumps), 30-day rotating refresh key with reuse detection, `/auth/refresh`, `/auth/logout`, schema-readiness columns, `login-security.service.test.ts`.

Verified there: `tsc --noEmit` clean, 430/430 API tests pass, and deliberate bugs in the new logic are caught by the tests. Apply with `git am docs/plans/server-patches/*.patch` in a clone of the server repo. The migration must run before the API deploys (schema readiness now requires the new columns).

## Fingerprint unlock: archived

Built and tested in the second session, then archived at the user's request (2026-11-12). The full version is on branch `archive/fingerprint-unlock` (commit `254ce07`; adds `androidx.biometric` 1.1.0 and makes `MainActivity` a `FragmentActivity`). It is not part of this branch.

## Verified (second session)

- Android SDK installed (platform 36, build-tools 36.0.0). `./gradlew :app:testDebugUnitTest :app:assembleDebug` passed with fingerprint unlock included (55/55 JVM tests) and again after merging `feat/password-reset` (56/56, APK builds). After archiving fingerprint unlock, 18 of those tests left with it; the remaining set has not been re-run with Gradle here.
- Note: `gradlew` is committed without the executable bit; run it with `sh ./gradlew` or `chmod +x`.

## Not verified yet

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Use the `STI_ILMS_Mobile` cloud environment, which allows `dl.google.com` for the Android SDK.
- Anything against a real server or device.
