# STI ILMS Mobile — My Profile Design

**Date:** 2026-10-12  
**Status:** Approved  
**App:** STI ILMS Mobile (`C:\Users\Ethan\AndroidStudioProjects\STI ILMS`)  
**Backend:** Existing hosted API (no new database work)

**Implementation plan:** [`2026-10-12-mobile-profile-implementation.md`](./2026-10-12-mobile-profile-implementation.md) (covers this design + the earlier plan draft)

## Goal

Let Student and Faculty (and staff who sign into this app) view and edit their own profile and submit a private avatar for librarian approval, matching web “My profile” behaviour via the same APIs.

## Decisions

| Topic | Choice |
|-------|--------|
| Architecture | Approach 1 — mirror Fines: `LibraryApi` → `LibraryRepository` → Compose screen (no ViewModel) |
| Screen location | `dashboard/account/ProfileScreen.kt`, package `com.example.stismartlib.ui.dashboard.account` |
| Navigation entry | Drawer item **My profile** under My account **and** drawer account card opens Profile |
| Avatar images | Coil; no long-lived disk cache for short-lived signed URLs |
| Roles | Student + Faculty primary; staff/librarian OK if they use this app; no Admin profile |
| Scope order | Read-only load → edit/save → photo upload + pending → drawer polish |

## Architecture & data flow

Auth remains Bearer JWT via existing `ApiClient` interceptor / `SessionStore`.

| Action | Endpoint |
|--------|----------|
| Load profile | `GET api/v1/profile/me` |
| Save fields | `PATCH api/v1/profile/me` with `first_name`, `last_name`, `program_strand`, `year_grade_level` |
| Load avatar | `GET api/v1/profile/avatar/me` |
| Upload photo | `POST api/v1/profile/avatar/me` multipart field `image` (PNG/JPEG/WebP ≤ 2 MB) |

Repository responsibilities:

- Hold profile + avatar status in repository state (same pattern as `fines`).
- Load on Profile screen enter; map errors like other screens (`ApiException` → user-visible message).
- After successful name save, refresh `SessionStore` + `LibrarySession.userName` so the drawer updates.
- After successful upload, refresh avatar meta so pending/approved/rejected matches the server.
- Multipart upload is new Retrofit surface (none exists today).

Images: Coil for signed URLs with memory-only / short TTL (do not cache signed URLs forever). Initials fallback when there is no approved photo.

## Screen UI

Reuse portal components (`PageHeader`, `SectionCard`, `ElmsBanner`, `ElmsLoadingBlock`, design-system fields/buttons). Follow `@sti-design-system` tokens; no raw colours.

Layout (top → bottom):

1. Header — eyebrow “My account”, title “My profile”
2. Summary — full name, role, status as provided by API
3. Photo — circular avatar; Photo Picker; pending/rejected notes; photo private until librarian approval
4. Editable — first/last name; Students also program/strand + year/grade
5. Read-only — school ID, email, role
6. Actions — Save for fields; separate upload / submit-for-approval for photo

Client validation before upload: PNG/JPEG/WebP, ≤ 2 MB.

## Navigation

- `DashboardRoute.Profile` → `"profile"`
- `composable` registered in `DashboardScreen` NavHost beside Fines/Clearance
- Drawer: **My profile** under My account (with Notifications / Fines / Clearance)
- Drawer account card (name/initials) navigates to Profile and closes drawer

## Errors & polish

- Load failure: banner/error state; prefer keeping last good profile on a failed refresh when possible
- Save/upload failure: inline banner from mapped server/`ApiException` message
- Busy: disable matching button during save/upload to prevent double submit
- Success: session display name updates; avatar status refreshes to pending after upload

## Out of scope

- New database / migrations
- Admin profile
- Long-lived caching of signed avatar URLs
- Rebuilding backend profile modules

## Verification

- Manual (emulator): open via drawer item + account card; load; edit/save name (+ student academic fields); pick photo → pending; confirm locked school ID/email; light + dark
- Optional `@Preview` in `ScreenPreviews.kt` like Fines/Clearance
- UI changes verified with `@android-visual-verification` when implementing

## Skills for implementation

| Skill | When |
|-------|------|
| `@sti-design-system` | Screen UI, tokens, components |
| `@android` / `@mobile-design` | Compose patterns, touch, Photo Picker |
| `@android-visual-verification` | After UI is wired |
| `@error-handling-patterns` | Load / save / upload states |
| `@verification-before-completion` | Before claiming done |
| `@writing-plans` / executing plan | Implementation task breakdown |

## Reference (do not rebuild)

Web behaviour: SmartLib `ProfileAvatarPage.tsx` and APIs above. Mobile patterns: `FinesScreen.kt`, `ClearanceScreen.kt`, `LibraryRepository.loadFines()`, `DashboardRoute` + drawer in `DashboardScreen.kt`.
