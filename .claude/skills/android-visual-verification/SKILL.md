---
name: android-visual-verification
description: Build, install, drive, and screenshot the STI ILMS Android app on an emulator so UI changes can be visually confirmed before reporting them as done. Use after any change to a Compose screen, theme, colour, layout, or component, or when asked to check how a screen actually looks in light mode, dark mode, or at large font scale.
---

# Visual Verification on Device

Compose previews are not proof. Before claiming a UI change works, render it on a device and look at the result.

The project has no screenshot-test harness, so verification runs against a live emulator.

> Google's `com.android.compose.screenshot` plugin (`0.0.1-alpha16`) was tried and **does not work on this project's AGP 9.0.1 / Gradle 9.1 setup**. It applies cleanly, registers its tasks, and compiles the `screenshotTest` source set, but discovers zero previews and renders zero images — a silent no-op. Verified not to be caused by preview placement (top-level and class-method forms both fail), class output paths (the generated config correctly points at AGP 9's `built_in_kotlinc` directory), or the JDK (fails on both 25 and 21). Do not re-attempt without first checking for a newer plugin release.

## Environment

All commands are PowerShell. Set `$adb` once per shell session — the SDK tools are not on `PATH`.

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$emu = "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe"
```

- Available AVD: `Pixel_5` (Android 16, 1080x2340)
- Package / activity: `com.example.stismartlib/.MainActivity`
- Screenshot scratch dir: `build/uishots/` (inside `build/`, so it is already git-ignored)

## Workflow

```
- [ ] 1. Boot emulator
- [ ] 2. Build and install
- [ ] 3. Launch and navigate to the screen
- [ ] 4. Capture and inspect
- [ ] 5. Re-check in dark mode and at large font scale
- [ ] 6. Reset device state
```

### 1. Boot emulator

```powershell
& $adb devices
```

If no device is listed, start one and wait for boot:

```powershell
Start-Process -FilePath $emu -ArgumentList "-avd","Pixel_5","-no-snapshot-load" -WindowStyle Minimized
# then poll until this prints 1
& $adb shell getprop sys.boot_completed
```

Cold boot takes roughly 30–60 seconds. Poll rather than using a single long sleep.

### 2. Build and install

```powershell
cd "C:\Users\Ethan\AndroidStudioProjects\STI ILMS"
.\gradlew.bat :app:installDebug --console=plain
```

Expect `BUILD SUCCESSFUL`. A clean incremental install takes about 50 seconds; a first build takes longer.

### 3a. Render any screen directly via PreviewActivity (preferred)

`ui-tooling` is a `debugImplementation` dependency, so debug builds ship `androidx.compose.ui.tooling.PreviewActivity`. It renders any `@Preview` function by reflection — including `private` ones — which means **every screen can be rendered without logging in**, using the seeded in-memory data from `PreviewApp`.

```powershell
$pkg = "com.example.stismartlib"
$cls = "com.example.stismartlib.ui.preview.ScreenPreviewsKt"
& $adb shell am force-stop $pkg
& $adb shell am start -n "$pkg/androidx.compose.ui.tooling.PreviewActivity" -e composable "$cls.FinesScreenPreview"
Start-Sleep -Seconds 6
& $adb shell screencap -p /sdcard/s.png
& $adb pull /sdcard/s.png "build\uishots\fines.png"
```

The composable name is `<package>.<FileName>Kt.<functionName>` for a top-level function. `ui/preview/ScreenPreviews.kt` lists every screen's preview function.

**Wait at least 6 seconds after `force-stop`.** A shorter delay captures the launch splash instead of the screen, which looks like a successful capture but is not. Sanity-check batch captures by file size — a cluster of identically sized PNGs usually means you captured the splash repeatedly.

`PreviewApp` takes `darkTheme` as a parameter rather than following the system, so `cmd uimode night yes` will **not** flip these previews. Dark coverage needs preview functions that pass `darkTheme = true`.

### 3b. Launch and navigate the real app

```powershell
& $adb shell am start -n com.example.stismartlib/.MainActivity
```

Give the splash and first composition ~5 seconds to settle before capturing.

To reach a screen behind navigation, dump the view hierarchy, read the `bounds` attribute of the target node, and tap its centre:

```powershell
& $adb shell uiautomator dump /sdcard/ui.xml
& $adb pull /sdcard/ui.xml "build\uishots\ui.xml"
```

Nodes look like `<node text="Register as Student" bounds="[248,838][397,866]" …>`. Tap the midpoint:

```powershell
& $adb shell input tap 322 852
& $adb shell input text "2024001234"      # no spaces; use %s for a space
& $adb shell input keyevent KEYCODE_BACK
```

Wait and re-dump after each tap rather than chaining blind taps. If two dumps in a row look identical after a tap, stop and investigate instead of tapping again.

### 4. Capture and inspect

```powershell
& $adb shell screencap -p /sdcard/shot.png
& $adb pull /sdcard/shot.png "build\uishots\login-light.png"
```

Then **read the PNG file** to actually look at it. Capturing without inspecting is not verification.

> Do not use `adb exec-out screencap -p > file.png`. PowerShell's redirection mangles the binary stream and produces a corrupt PNG. Always `screencap` to `/sdcard` then `pull`.

### 5. Dark mode and font scale

The app's `ThemeController` defaults to `SYSTEM`, so the system night-mode toggle flips it.

```powershell
& $adb shell cmd uimode night yes     # dark
& $adb shell cmd uimode night no      # light
& $adb shell cmd uimode night auto    # restore default
```

Large-text check — catches clipped labels and broken rows:

```powershell
& $adb shell settings put system font_scale 1.3
# capture, inspect, then restore
& $adb shell settings put system font_scale 1.0
```

Allow ~3 seconds after each toggle for recomposition before capturing.

### 6. Reset device state

Always restore what you changed, so the next run starts clean:

```powershell
& $adb shell settings put system font_scale 1.0
& $adb shell cmd uimode night auto
& $adb shell rm /sdcard/shot.png /sdcard/ui.xml
```

## What to look for in a capture

- The change you made is actually visible and correct.
- Nothing nearby regressed: spacing, alignment, overlap, clipped or truncated text.
- Dark mode: no invisible text, no light-only surface, borders still readable.
- Contrast: text legible against its background in both themes.
- Status bar and bottom gesture bar do not overlap content.
- At 1.3x font scale: labels wrap rather than clip, rows grow rather than overlap.

## Before / after comparisons

For a change to an existing screen, capture before you edit:

```
build/uishots/<screen>-before-light.png
build/uishots/<screen>-after-light.png
build/uishots/<screen>-after-dark.png
```

Read both and state concretely what differs. Embed the after shot in your summary with `![…](absolute/path.png)` so the result is visible without re-running anything.

## Done means

A UI task is finished only when:

1. `:app:installDebug` succeeded,
2. the affected screen was captured and inspected in light **and** dark mode, and
3. the capture confirms the intended change with no visible regression.

If the emulator cannot be reached or the build fails, say so plainly rather than reporting the change as verified.
