# Fines — how calculated / rules (design)

**Status:** Approved approach (Option 1)  
**Date:** 2026-10-15  
**Related:** Deferred “fine receipts/terms” in P1; backend `fine-calculator.ts` + `fine_policy_versions`

## Goal

On the mobile **Library fines** screen, show clear student-facing rules for how overdue fines are calculated, sourced from the **active fine policy** on the API — not hardcoded copy that drifts when admins change rates.

## Decision

| Option | Choice |
|--------|--------|
| Content source | Backend active fine policy |
| Delivery | New student read: `GET /api/v1/fines/policy` |
| Mobile until deploy | Soft “rules unavailable” (same pattern as Profile / calendar) |
| Hardcoded rates in app | **No** (except preview seed for Compose previews) |

## Backend truth (today)

Calculator (`apps/api/src/modules/fines/fine-calculator.ts`):

- Same calendar day overdue → **hourly** rate × ceil hours  
- Later calendar days → **daily** rate × **operating** days only (closed days / closed schedule do not count)  
- Cap at **maximum penalty**  
- Defaults if no DB row: `hourlyRate=2`, `dailyRate=10`, `maximumPenalty=500` (PHP)

Note: existing `GET /api/v1/fines/terms` returns **academic terms** for filters — **not** fine rates. Do not overload it.

## Proposed API

```
GET /api/v1/fines/policy
Auth: JWT Student | Faculty (same gate as /fines/me)
```

Example success body:

```json
{
  "success": true,
  "data": {
    "currency": "PHP",
    "hourlyRate": 2,
    "dailyRate": 10,
    "maximumPenalty": 500,
    "effectiveFrom": "2026-01-01",
    "rules": [
      "Same-day overdue uses the hourly rate (rounded up by the hour).",
      "After the due date, fines accrue per open library day at the daily rate.",
      "Closed days and closed dates do not add to the fine.",
      "The total overdue fine for a loan cannot exceed the maximum penalty.",
      "Pay cash only at the library counter with your school ID."
    ]
  }
}
```

`rules` may be server-generated from the numeric rates so wording always matches numbers. Mobile displays rates + bullet list; does not recompute fines locally.

## Mobile UX (Fines screen)

Place a **“How fines are calculated”** `SectionCard` above or just below the existing cash-payment card:

1. Headline: How fines are calculated  
2. Three rate rows: Hourly (same day) · Daily (later days) · Maximum  
3. Bullet rules from API  
4. Loading / error / unavailable states (banner or muted text — no fake numbers)

Keep existing balance stats, history list, and cash-payment guidance.

## Out of scope (this slice)

- Fine receipt PDF download (still deferred)  
- Student payment online  
- Admin fine policy editor UI  
- Replacing `GET /fines/me` list shape  

## Success

- Student sees live rates and rules when policy route is mounted  
- When route missing: calm unavailable copy; rest of Fines still works  
- Preview shows sample policy without network  
