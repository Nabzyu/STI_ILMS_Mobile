---
name: api-route-deploy-gap
description: Diagnose STI ILMS mobile errors like "This feature is not available right now", "Route not found", or profile/reserve 404s by checking whether the hosted API (Vercel main) mounts the route the app calls. Use when Profile, reservations, or any LibraryApi path fails after a successful login JWT, or when comparing mobile endpoints to JohnBazty/StiOrmocLibrary.
---

# API route / deploy-gap diagnosis (STI ILMS)

Mobile shows calm copy; the real failure is often **backend not deployed**, not a Compose bug.

## When to use

- Banner/toast: **This feature is not available right now.**
- Raw/server: **Route not found.**
- Profile / avatar / other `LibraryApi` call fails **after** login works
- Need to know: app bug vs missing production route

## Known mapping (mobile)

| Server / transport | User-facing (`userFacingMessage`) |
|--------------------|-------------------------------------|
| Message contains `Route not found` | This feature is not available right now. |
| HTTP 404 empty body (ApiClient) | This feature is not available right now. |
| `JWT_REQUIRED` / Bearer messages | Your session expired. Please sign in again. |
| `INVALID_CREDENTIALS` | Invalid school ID or password. |

Display layer: `app/src/main/java/ui/feedback/UserFacingErrors.kt`  
API paths: `app/src/main/java/network/LibraryApi.kt`  
Base URL: `BuildConfig.API_BASE_URL` → `https://sti-ormoc-smart-library.vercel.app/`  
Backend repo: `JohnBazty/StiOrmocLibrary` (deployed branch: `main`)

## Diagnosis steps (do in order)

### 1. Identify the exact path the app calls

Read `LibraryApi.kt` for the failing screen (e.g. Profile → `GET api/v1/profile/me`, `GET api/v1/profile/avatar/me`).

### 2. Probe production without a token (PowerShell: use `curl.exe`)

```powershell
curl.exe -sS -w "`nHTTP:%{http_code}`n" `
  "https://sti-ormoc-smart-library.vercel.app/api/v1/PROFILE_OR_PATH" `
  -H "Accept: application/json"
```

| Response | Meaning |
|----------|---------|
| `401` + `JWT_REQUIRED` | Path is behind the global `/api/v1` JWT gate (route may still be missing after auth) |
| `404` + `Route not found` | Catch-all; router not mounted (or wrong path) |
| `404` HTML from Vercel | Wrong host/path entirely |

Unauthenticated `401 JWT_REQUIRED` does **not** prove the route exists. JWT middleware runs before route match for `/api/v1/*`.

### 3. Confirm mounts on `main` vs feature branch

```powershell
# main — list /api/v1 mounts; check for profile
gh api repos/JohnBazty/StiOrmocLibrary/contents/apps/api/src/app.js --jq .content
# decode base64, then grep app.use('/api/v1 and profile

# feature branch example that HAS profile:
gh api "repos/JohnBazty/StiOrmocLibrary/contents/apps/api/src/app.js?ref=codex/library-revision-updates" --jq .content
```

**Profile truth (as of 2026-10):**

| Branch | Profile mounts |
|--------|----------------|
| `main` (production) | **None** — no `profile` in `app.js` |
| `codex/library-revision-updates` | `app.use('/api/v1/profile/avatar', …)` and `app.use('/api/v1/profile', selfProfileRouter)` |

Catch-all on both: `{ success: false, message: 'Route not found.' }`

### 4. Authenticated check (optional)

With a Student/Faculty Bearer token:

- If JWT valid and path unmounted → **Route not found** → mobile maps to feature-unavailable
- If JWT valid and path mounted → `200` or business error codes (not catch-all)

### 5. Classify and stop

| Class | Fix owner | Action |
|-------|-----------|--------|
| **Deploy gap** | Backend | Merge/deploy branch that mounts the routers to `main` / Vercel |
| **Wrong path** | Mobile | Align `LibraryApi` to live OpenAPI / `app.js` mounts |
| **Auth/role** | Either | `JWT_*`, `JWT_ROLE_FORBIDDEN` — not a missing route |
| **Copy only** | Mobile | Do not "fix" Profile UI for deploy gaps |

## Do not

- Change Profile Compose screens to "fix" Route not found when mounts are missing on `main`
- Treat unauthenticated `401` as proof the endpoint is live
- Use PowerShell `curl` alias — use `curl.exe`

## Related docs in this repo

- `docs/bugs/2026-10-13-profile-reservation-route-not-found.md`
- `docs/plans/2026-10-12-mobile-profile.md`
- `docs/plans/2026-10-13-user-facing-errors.md`
