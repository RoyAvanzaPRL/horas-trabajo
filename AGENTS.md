# HourFlow — Design Decisions & Architecture

This document records the key architectural and design decisions made during the development of **HourFlow** (formerly TrackTime), a personal work-hour tracking app for Android.

Its purpose is to provide context for future development, onboarding, or code reviews — whether done by humans or AI-assisted tooling.

---

## Project Scope

**HourFlow** is a single-user, local-first Android application for tracking personal work hours. It is **not** a general finance app, a multi-user platform, or a cross-platform solution.

---

## Tech Stack

| Layer | Choice | Rationale |
|-------|--------|-----------|
| **Language** | Kotlin | Modern, concise, null-safe, official Android language |
| **UI** | Jetpack Compose + Material 3 | Declarative UI, modern Android standard — replaces legacy XML/View system |
| **Persistence** | Room (SQLite) | Type-safe SQLite wrapper, official Android ORM. Single source of truth |
| **Navigation** | Navigation Compose | Type-safe navigation with compile-time route checking |
| **Preferences** | DataStore Preferences | Replaced SharedPreferences — coroutine-based, type-safe |
| **Serialization** | kotlinx.serialization | Kotlin-native JSON serialization (for backup/restore) |
| **Architecture** | Repository + ViewModel + StateFlow | Clean separation: data layer → domain models → UI state |
| **DI** | Manual (no Hilt/DI framework) | Intentional — project size doesn't warrant the overhead |

---

## Architecture Overview

```
UI Layer (Compose Screens)
    ↕ StateFlow
ViewModel Layer
    ↕ suspend / Flow
Repository Layer
    ↕ DAO
Room Database (SQLite)
```

Each layer is testable in isolation. ViewModels use `StateFlow` for lifecycle-aware UI state, and repositories abstract the data source so the domain logic never touches Room directly.

---

## Data Model Decisions

### Trabajo (Job) — Top-Level Entity
- Flat hierarchy — no nested categories or sub-projects
- Each `Trabajo` is **completely independent**: no cross-job data aggregation
- Fields include: name, person name, currency symbol, optional photo URI

### EntradaHoras (Time Entry)
- One entry per shift with start/end time
- **Night shift support**: manual checkbox `esDiaSiguiente` for shifts crossing midnight
- Multiple entries allowed per day (e.g., morning + evening shifts)
- Cross-month shifts are assigned **entirely to the starting month** — no pro-rata split
- Optional: notes field, custom hourly rate override

### TarifaMensual (Monthly Rate)
- Rate is stored **per month, per job** — not a single global rate
- Each month inherits the previous month's rate by default (overridable)
- Rationale: changing a rate should never retroactively recalculate past months

### DineroExtra (Extra Money)
- Per-month, per-job additional income entries
- Each entry: amount + short description + date
- Amount can be **negative** (for discounts/advances)
- Positive amounts are the common case (e.g., tips, delivery bonuses)

---

## Export System

| Format | Purpose | Reimportable |
|--------|---------|-------------|
| **JSON** | Full backup & restore (round-trip safe) | ✅ Yes |
| **PDF** | Formal printable document | ❌ No |
| **JPEG** | Quick WhatsApp sharing (instant preview) | ❌ No |

All exports are **per-job** — they never combine data from different jobs.

---

## UI / UX Principles

- **Monthly grid view**: All days of the month visible at once, tap any day to log hours — no "create entry" wizard flow
- **Second entry only shown on demand**: The UI stays clean for the common case (one shift/day)
- **Notes field hidden by default**: Same pattern — only appears when the user opts in
- **Week markers**: Visual Monday→Sunday separators with weekly totals at week end
- **Cross-month weeks**: Partial week totals shown at the month boundary (no data duplication)
- **Year navigation (AnioScreen)**: 3×4 grid showing all 12 months, current month highlighted

---

## Theme System

- **3 modes**: System (default) / Light / Dark
- Persisted via DataStore Preferences
- Separate Settings screen with theme selector + JSON backup/restore

---

## Build & Run

This project uses the **Gradle Wrapper** (`gradlew`), so no Gradle installation required.

```bash
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Requirements
- Android Studio (2023+ recommended)
- JDK 17 (bundled with Android Studio)
- Android SDK (API 34) — downloaded automatically on first sync

---

## Planned / Future Considerations

- **Editing past entries**: Standard CRUD assumed but not a closed design decision
- **Notifications/reminders**: Not in MVP — reconsidered based on real usage
- **Offline-first**: Already the current model (local-only)
