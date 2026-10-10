# Redesign Phase 0: Foundation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade Compose/navigation/activity to versions that support shared-element transitions, and make the app edge-to-edge, with no visible regressions.

**Architecture:** Version bumps go through `gradle/libs.versions.toml`. Edge-to-edge is enabled once in `MainActivity` via `enableEdgeToEdge()`; the theme stops painting bar colors and only sets icon contrast; each scaffold that owns a bar applies its own insets, and nested scaffolds opt out so insets are never applied twice.

**Tech Stack:** Kotlin 2.0.0, AGP 8.13.2, Compose BOM 2025.01.01 (Compose 1.7.x, Material3 1.3.x), navigation-compose 2.8.9, activity-compose 1.9.3.

**Spec:** `docs/superpowers/specs/2026-10-11-apple-neutral-redesign.md`

## Global Constraints

- Stay on Kotlin 2.0.0 and compileSdk/targetSdk 34 in this phase (only `platforms/android-34` is installed; Compose BOM ≥ 2025.04 needs compileSdk 35, and newer AndroidX artifacts need Kotlin ≥ 2.1).
- minSdk stays 26.
- Gradle needs `JAVA_HOME=%LOCALAPPDATA%\Programs\jdk-17.0.20.1+1` in this environment.
- After every task: `./gradlew assembleDebug testDebugUnitTest` must pass (AGENTS.md build-check rule).
- No visual redesign in this phase: colors, type and layouts stay as they are.

## Review Focus

1. Keyboard over text fields after edge-to-edge (AI Settings API key, Whitelist add field): the field must stay visible above the IME.
2. Bar icon contrast follows the **in-app** theme toggle, not the system theme: light theme → dark icons on both bars, dark theme → light icons.
3. 3-button navigation: the bottom tab bar must sit above the nav buttons; nothing hidden behind them.
4. Nested Scaffold on Home: status-bar inset must not be applied twice (no double gap above the search field); FAB stays above the tab bar.
5. Notification deep link (`destination = database_settings`) still lands on Database settings with its top bar below the status bar.

These are visual checks on a device; there are no Compose UI tests for these screens. Each is listed as an explicit verification step in Task 2.

---

### Task 1: Upgrade Compose, navigation and activity

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts` (navigation dependency line)

**Interfaces:**
- Produces: Compose 1.7 APIs (`SharedTransitionLayout`, `AnimatedContentScope` in `composable {}`), `enableEdgeToEdge()` from activity 1.9.

- [ ] **Step 1: Bump versions in the catalog**

In `gradle/libs.versions.toml`:

```toml
compose-bom = "2025.01.01"
activityCompose = "1.9.3"
navigation = "2.8.9"
```

and under `[libraries]`:

```toml
# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
```

Delete the unused `compose-compiler = "1.5.11"` entry (the Kotlin compose plugin replaced it).

- [ ] **Step 2: Use the catalog entry in the app module**

In `app/build.gradle.kts` replace

```kotlin
implementation("androidx.navigation:navigation-compose:2.7.7")
```

with

```kotlin
implementation(libs.androidx.navigation.compose)
```

- [ ] **Step 3: Build and run unit tests**

Run: `./gradlew assembleDebug testDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL. New deprecation warnings (e.g. `Icons.Filled.Sort`, `Icons.Filled.Message` → `AutoMirrored`) are acceptable; errors are not. If the build fails on an API change, fix the call site and note it in the commit message.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "Upgrade Compose BOM to 2025.01.01, navigation to 2.8.9, activity to 1.9.3"
```

### Task 2: Edge-to-edge

**Files:**
- Modify: `app/src/main/java/com/nimroddayan/clipit/MainActivity.kt`
- Modify: `app/src/main/java/com/nimroddayan/clipit/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/nimroddayan/clipit/App.kt` (MainScaffold bottom bar)
- Modify: `app/src/main/java/com/nimroddayan/clipit/ui/screen/HomeScreen.kt` (nested Scaffold insets)
- Modify: `app/src/main/java/com/nimroddayan/clipit/ui/screen/AiSettingsScreen.kt`, `WhitelistManagementScreen.kt` (IME padding)
- Modify: `app/src/main/AndroidManifest.xml` (`windowSoftInputMode`)

**Interfaces:**
- Consumes: `androidx.activity.enableEdgeToEdge` (Task 1).
- Produces: every later phase can draw behind system bars; screens own their insets.

- [ ] **Step 1: Enable edge-to-edge in the activity**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    ...
}
```

- [ ] **Step 2: Theme stops painting bars; sets icon contrast from the in-app theme**

In `ClipItTheme`, replace the `SideEffect` body with:

```kotlin
SideEffect {
    val window = (view.context as Activity).window
    WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
    }
}
```

Remove the now-unused `toArgb` import.

- [ ] **Step 3: Tab bar pads itself above the navigation bar**

In `MainScaffold`, the bottom bar `Row` gets `.navigationBarsPadding()` so the `Surface` color extends under the gesture bar while the tabs stay clear of it.

- [ ] **Step 4: Nested Home scaffold doesn't re-apply system insets**

`HomeScreen`'s `Scaffold` already sits inside `MainScaffold`'s padding. Pass `contentWindowInsets = WindowInsets(0, 0, 0, 0)`.

- [ ] **Step 5: Keyboard keeps text fields visible**

Manifest: add `android:windowSoftInputMode="adjustResize"` to `MainActivity`.
`AiSettingsScreen`: add `.imePadding()` to the scrolling `Column` (after `.padding(innerPadding)`).
`WhitelistManagementScreen`: add `.imePadding()` to the `LazyColumn` modifier.

- [ ] **Step 6: Build and run unit tests**

Run: `./gradlew assembleDebug testDebugUnitTest --console=plain`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Verify on device (Review Focus 1–5)**

With the user's OK, `./gradlew installDebug`, then screenshot (`adb exec-out screencap -p`) and check:
Home (light + dark via in-app toggle), Dashboard, Settings, AI Settings with keyboard open on the API key field,
Whitelist with keyboard open, Database settings. Pass criteria are the five Review Focus lines.

- [ ] **Step 8: Commit**

```bash
git add -A app/src/main
git commit -m "Go edge-to-edge: system bars drawn by the app, insets owned per screen"
```
