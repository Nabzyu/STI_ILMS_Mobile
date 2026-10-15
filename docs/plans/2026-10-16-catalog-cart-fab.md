# Catalog book cart FAB Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add an always-visible floating cart button with count badge on the book catalog, and remove the results-bar Cart button.

**Architecture:** Wrap catalog list/grid in a `Box`, overlay a Material3 `FloatingActionButton` bottom-end with `ElmsCountBadge` from cart size. Reuse existing `onCartClick`. No new files, routes, or repository APIs.

**Tech Stack:** Jetpack Compose, Material3, Lucide (`ShoppingCart`), existing `ElmsCountBadge` / `LibraryRepository.cart`

**Design:** `docs/plans/2026-10-16-catalog-cart-fab-design.md`  
**Skills:** `@sti-design-system`, `@android` (touch targets / a11y)

---

### Task 1: Strip results-bar Cart button

**Files:**
- Modify: `app/src/main/java/dashboard/catalog/BookCatalogScreen.kt` (`BookCatalogResultsBar`)

**Step 1: Remove Cart from results bar**

In `BookCatalogResultsBar`, drop `onCartClick` from the signature and remove the ghost `PortalButton` for Cart. Keep the count text + `CatalogViewToggle` row.

Update both call sites (grid + list) to stop passing `onCartClick` into `BookCatalogResultsBar`. Keep `onCartClick` on `BookCatalogScreen` itself — the FAB will use it.

**Step 2: Commit**

```bash
git add app/src/main/java/dashboard/catalog/BookCatalogScreen.kt
git commit -m "refactor(catalog): remove results-bar cart button"
```

---

### Task 2: Add FAB overlay + badge + bottom padding

**Files:**
- Modify: `app/src/main/java/dashboard/catalog/BookCatalogScreen.kt`

**Step 1: Imports**

Add as needed:

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.stismartlib.ui.components.ElmsCountBadge
```

**Step 2: Bottom padding**

Change `listContentPadding` so bottom clears the FAB:

```kotlin
val listContentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp)
```

**Step 3: Wrap content + FAB**

Wrap the existing `if (viewMode == …) { LazyVerticalGrid … } else { LazyColumn … }` in:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // existing grid / list branch unchanged aside from padding above

    val cartCount = repository.cart.size
    val fabDescription = if (cartCount > 0) "Book cart, $cartCount items" else "Book cart"
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        FloatingActionButton(
            onClick = onCartClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.semantics { contentDescription = fabDescription }
        ) {
            Icon(Lucide.ShoppingCart, contentDescription = null)
        }
        ElmsCountBadge(
            count = cartCount,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
        )
    }
}
```

Do **not** introduce a nested `Scaffold`. Do **not** create a shared `CartFab` file.

**Step 4: Sanity compile**

Run (from project root):

```bash
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

**Step 5: Commit**

```bash
git add app/src/main/java/dashboard/catalog/BookCatalogScreen.kt
git commit -m "feat(catalog): add floating cart button with count badge"
```

---

### Task 3: Manual / visual check

**Files:** none (verification only)

**Step 1: Verify on device or emulator** (prefer `@android-visual-verification` if available)

Checklist from design:

- [ ] FAB always visible on catalog (list + grid)
- [ ] Empty cart → no badge; opens empty cart
- [ ] After add-to-cart from detail → badge shows correct count
- [ ] Last book row not covered by FAB
- [ ] Light + dark readable
- [ ] Drawer / detail cart entry points still work

**Step 2: Commit docs if any notes were added** — skip if nothing changed.

---

## Done when

Catalog shows one floating cart control with badge; results-bar Cart is gone; cart navigation and limits unchanged.
