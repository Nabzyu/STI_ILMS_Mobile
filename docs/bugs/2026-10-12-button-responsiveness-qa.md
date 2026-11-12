# QA Report: Button / touch-target responsiveness

## Scope

Theme concern across interactive controls: touch targets ≥48dp, tap affordance, and double-submit guards.

**Covered**

- Shared: `PortalButton`, `ElmsPillTab`, `ElmsListRow`, `ElmsEmptyState` action
- Auth: Login, Register (eye / show-password / Sign in / Create account)
- Shell: drawer items, theme toggle, log out, account card, header icon buttons
- Dashboard: quick actions, quick stats, notification rows, book cart remove / borrow
- Profile preview (Save / Change photo)

**How checked**

- Code audit against Material / `@android` R6.5 (≥48×48dp)
- Emulator `Pixel_5` (density 440 → 48dp = 132px): PreviewActivity dumps + tap
- Screenshots: `build/uishots/qa-login-buttons.png`, `qa-profile-buttons.png`

**Roles / themes:** Student preview path; light mode hierarchy dump. Dark not re-dumped for every control (shared components are theme-agnostic for size).

## Summary

Most primary actions already used `PortalButton` with a 48dp minimum. Gaps were drawer rows and several cards/icon actions that relied on padding alone, plus cart borrow lacking a loading/disable guard. Shared and shell controls were tightened; login Sign in measured ~52dp tall and full width after the change.

## Findings

### High — Drawer rows lacked an explicit 48dp minimum
**Where:** `dashboard/DashboardScreen.kt` — nav items, My QR, theme, log out, account card  
**Problem:** Rows used vertical padding only; short labels could fall under 48dp on some densities.  
**Fix applied:** `.sizeIn(minHeight = 48.dp)` on each clickable drawer row.  
**Effort:** S

### High — `PortalButton` minimum height was only 48dp with tight padding
**Where:** `components/PortalUi.kt` — `PortalButton`  
**Problem:** Primary CTAs felt cramped; empty-state actions were not full-width.  
**Fix applied:** Default `minHeight` 52dp, vertical padding 14dp; empty-state action uses `Modifier.fillMaxWidth()`.  
**Effort:** S

### High — Quick action / notification cards had no min height on the clickable card
**Where:** `dashboard/components/DashboardComponents.kt` — `FeaturedQuickActionCard`, `QuickActionTile`, `NotificationListItem`  
**Problem:** Compact layouts risked undersized hit areas.  
**Fix applied:** `.sizeIn(minHeight = 48.dp)` on clickable cards.  
**Effort:** S

### Medium — Register / cart icon buttons inconsistent with Login eye button
**Where:** `auth/RegisterScreen.kt`, `dashboard/catalog/BookCartScreen.kt`  
**Problem:** Login already set `sizeIn(48)`; Register and cart remove did not.  
**Fix applied:** Matching `sizeIn(minWidth = 48.dp, minHeight = 48.dp)` on those `IconButton`s.  
**Effort:** S

### Medium — Cart “Borrow books” could double-submit
**Where:** `dashboard/catalog/BookCartScreen.kt`  
**Problem:** No `enabled` / `loading` while submitting → taps could fire twice.  
**Fix applied:** `enabled = !isSubmitting`, `loading = isSubmitting`, early return if submitting.  
**Effort:** S

### Low — Overview quick stats
**Where:** `dashboard/overview/DashboardOverviewScreen.kt` — `QuickStat`  
**Fix applied:** `.sizeIn(minHeight = 48.dp)` on clickable column.  
**Effort:** S

## Device measurements (Pixel_5, 440dpi)

| Control | Measured | Result |
|---------|----------|--------|
| Login **Sign in** | ~992×144px (~361×52dp) | Pass |
| Login show-password eye area | 132×132px (48dp) | Pass |
| Login show-password row | height 132px (48dp) | Pass |
| Login Register link | height 132px (48dp) | Pass |
| Profile clickable actions (dump) | heights 48–52dp | Pass |

## Recommended order (done)

1. PortalButton + empty-state width  
2. Drawer rows  
3. Dashboard cards / quick stats  
4. Register + cart IconButtons + borrow loading  

## Out of scope / not fully exercised

- Live signed-in drawer navigation on a real account (login may still be rate-limited)
- Every TextButton in AlertDialogs (Material3 defaults to 48dp)
- Bottom NavigationBar items (Material3 defaults)
- 1.3× font scale re-measure of every screen

## Verdict

Interactive controls in the audited shared paths now enforce ≥48dp (primary buttons ~52dp). Re-install the debug build and spot-check Sign in, drawer rows, and Profile Save / Change photo on a device.
