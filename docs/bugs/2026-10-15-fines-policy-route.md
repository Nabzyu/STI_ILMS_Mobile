# Fine policy route — deploy checklist

**Date:** 2026-10-15  
**Mobile:** `GET /api/v1/fines/policy` via `LibraryApi.finePolicy()`  
**Design:** `docs/plans/2026-10-15-fines-how-calculated-design.md`

## Status

Mobile soft-fails when the route is missing (Fines screen shows an info banner; no invented rates). Backend still needs `GET /api/v1/fines/policy` on production `main` (see implementation plan Task 1 in `JohnBazty/StiOrmocLibrary`).

## Probe (unauthenticated)

```powershell
curl.exe -sS -w "`nHTTP:%{http_code}`n" `
  "https://sti-ormoc-smart-library.vercel.app/api/v1/fines/policy" `
  -H "Accept: application/json"
```

| Response | Meaning |
|----------|---------|
| `401` + `JWT_REQUIRED` | Path is behind global JWT gate — confirm after login it is not `Route not found` |
| `404` + `Route not found` | Not mounted — mobile banner expected |
| `200` + rates | Ready for student smoke on Fines |

## Expected payload (success)

```json
{
  "success": true,
  "data": {
    "currency": "PHP",
    "hourlyRate": 2,
    "dailyRate": 10,
    "maximumPenalty": 500,
    "effectiveFrom": "2026-01-01",
    "rules": ["…"]
  }
}
```

Source of truth: active `fine_policy_versions` row (same as `fine-calculator.ts`), with calculator defaults if none.
