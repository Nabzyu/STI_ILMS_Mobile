---
name: student-portal-p0
description: Implement STI ILMS Student/Faculty P0 portal parity — real printing (quote/submit/cancel/history), official clearance status via GET /clearance/me, and Profile only after backend deploy. Use when working on printing service, clearance, student portal gaps, or user-side P0 from the web-vs-app comparison.
---

# Student portal P0 (Printing + Clearance + Profile gate)

Staff/admin is **out of scope**. This skill covers the approved user-side pass only.

## Hard gates

1. Read this skill + `skill-stack.md` + `product-anchors.md` before coding.
2. Follow design: `docs/plans/2026-10-14-student-portal-p0-design.md`
3. Follow plan: `docs/plans/2026-10-14-student-portal-p0.md`
4. Do **not** invent print/clearance payloads — mirror web + live API validation.
5. Profile feature code waits until production mounts `/api/v1/profile` (see deploy checklist in design doc). Use `api-route-deploy-gap` to re-verify.

## Scope (approved)

| Area | In | Out |
|------|----|-----|
| Printing | Service status, file pick (PDF/DOCX ≤4 MB), quote, multipart submit, cancel pending unpaid, history refresh | Digital receipts, PDF download, admin queue |
| Clearance | `GET /clearance/me` → Cleared/Not Cleared, reason, summary, loans/fines/lost lists | Report lost book, overrides UI |
| Profile | Deploy checklist only this pass | Profile UI rewrite until APIs live |
| Staff | — | All `/api/v1/admin/*` |

## Architecture (match existing app)

```
LibraryApi (Retrofit)
  → LibraryRepository (state + userFacingMessage)
    → Compose screens (sti-design-system: Portal*, Elms*, PageHeader)
```

- Errors: always `userFacingMessage` / `ElmsBanner` / Toast — never raw Bearer/stack.
- Multipart: same pattern as avatar upload (`MultipartBody.Part` + form fields).
- File pick: `ActivityResultContracts.GetContent` / `OpenDocument` for `application/pdf` and DOCX MIME (not PickVisualMedia).
- PowerShell: use `curl.exe`, not `curl`.

## Printing contract (web parity)

**Quote** `POST /api/v1/printing/quote` multipart:

- `document` (file)
- `number_of_copies` (1–50)
- `print_type`: `Monochrome` | `Colored`
- `paper_size`: `Short` | `A4` | `Long`

**Submit** `POST /api/v1/printing/requests` multipart:

- same as quote, plus `page_count`, `document_sha256`, `quoted_cost` from quote
- `optional_notes` optional

**Also:** `GET /api/v1/printing/service-status`, `GET /api/v1/printing/requests`, `PUT /api/v1/printing/requests/{id}/cancel`

Respect `accepting_requests`; block submit when closed. Prefer server quote cost over local estimates.

## Clearance contract

**GET** `/api/v1/clearance/me` → record with:

- `status` / `computedStatus`: `Cleared` | `Not Cleared`
- `reason`, `checkedAt`, `summary`, `loans`, `fines`, `lostBooks`, `activeOverride`

Replace local loan/fine guessing in `ClearanceScreen`.

## Verification

- Unit tests for DTO mapping / mappers where logic is non-trivial.
- `:app:compileDebugKotlin` + targeted unit tests before claiming done.
- Emulator: print happy path + cancel; clearance cleared vs blocked if accounts exist.
- Visual: `android-visual-verification` after Compose screen changes.
- Profile: only mark done after `api-route-deploy-gap` confirms mounts on `main`.

## Related

- Gap canvas: workspace `canvases/web-vs-app-feature-gaps.canvas.tsx`
- Deploy diagnosis: `.claude/skills/api-route-deploy-gap/SKILL.md`
- Bug note: `docs/bugs/2026-10-13-profile-reservation-route-not-found.md`
