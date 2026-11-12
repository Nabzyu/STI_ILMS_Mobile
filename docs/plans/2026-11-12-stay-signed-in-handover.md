# Handover: stay signed in + fingerprint unlock

Date: 2026-11-12. Branch: `feat/stay-signed-in` (built on `feat/password-reset`, which is built on `claude/fix-sign-in-flow`). Nothing is merged to `main`; no pull requests are open.

## Decisions from the web team (the user is on the web team)

- Students and faculty only for "stay signed in". Library desk staff are not in the first release (support parked on `archive/desk-staff-support`).
- Refresh key lifetime: **30 days** from sign-in, not extended by use.
- **One website session and one phone session per account** (a website sign-in must not sign the phone out).
- `LOGIN_DEVICES_ENABLED` stays off. Wrong-portal sign-in stays as it is (counts as a wrong password).
- **Fingerprint unlock: wanted (not started).**
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

## Not verified yet

- `./gradlew :app:testDebugUnitTest :app:assembleDebug`. Use the `STI_ILMS_Mobile` cloud environment, which allows `dl.google.com` for the Android SDK.
- Anything against a real server or device.

## Next: fingerprint unlock

Suggested shape (confirm with the user before building):
- `androidx.biometric:biometric` (new dependency, needed for BiometricPrompt).
- Only when the phone holds a refresh key (Student/Faculty). Ask once after sign-in: "Use fingerprint to unlock?"; store the choice per account.
- Lock when the app opens or returns after a short time in the background (e.g. 1 minute); show a lock screen with "Unlock with fingerprint" and "Sign in with password instead" (signs out).
- Allow device PIN as fallback (`BIOMETRIC_STRONG or DEVICE_CREDENTIAL`). If no biometrics are enrolled, don't offer it.
- Optional hardening: bind the refresh key to a Keystore key that requires user authentication.
- Follow `.claude/skills/sti-design-system`; check the lock screen with `android-visual-verification` if an emulator is available.
