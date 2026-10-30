# Fetal Kick Counter & Maternal Health Activity Tracker

A secure, private, and intuitive maternal companion app designed to monitor fetal movement sessions with **fully customizable session durations** (e.g., 30 minutes, 1 hour, 2 hours, custom minutes, or open-ended untimed sessions) and **customizable kick goals**, visualize activity trends over time, inspect historical logs via an interactive calendar, export formatted PDF reports for OB/GYN doctors, and protect sensitive maternal health data through biometrics, custom PIN fallback, and encrypted local storage.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and user experience decisions have been updated based on your feedback:

- **Confirmed Decision 1 (Customizable Session Duration & Kick Target)**: 
  - Sessions are **not fixed to 2 hours**. Users can freely configure the maximum session duration for each session (quick presets: 30 minutes, 1 hour, 2 hours, custom duration up to 4 hours, or open-ended / untimed mode where users stop manually).
  - Kick target is also customizable (presets: 5, 10, 15, 20 kicks, or freeform continuous tracking).
- **Confirmed Decision 2 (Doctor Reports)**: Clean, professionally styled PDF report generated on-device containing mother & baby metadata, session duration, kick interval timelines, charts, and clinical summary notes for OB/GYN visits.
- **Confirmed Decision 3 (Security & Biometric Fallback)**: Multi-layer privacy architecture utilizing Android `BiometricPrompt` (fingerprint/face unlock) paired with an encrypted custom 4-digit PIN fallback, auto-lock timeouts, and hardware-backed KeyStore encryption for local storage.
- **Confirmed Decision 4 (Reminders & Notifications)**: Configurable daily tracking reminder notifications utilizing Android `AlarmManager` and `NotificationManager` with user-scheduled morning, afternoon, or evening reminder prompts.

---

## 1. Overview & Core Concept

- **What It Does**: Expectant mothers can customize their session time and kick goal before starting, tap a responsive haptic-enabled kick button for each fetal movement, record movement intensity/notes, monitor a live count and timer, save sessions automatically when target is reached (or whenever concluded), analyze long-term kick patterns on an interactive calendar and trend graphs, and export shareable PDF reports.
- **Target Audience / Persona**: Expectant parents (typically 28+ weeks into pregnancy) advised by doctors or midwives to monitor fetal movement patterns across diverse clinical guidelines (e.g., 10 kicks in 2 hours, 10 kicks in 1 hour, or 30-minute kick velocity tracking).
- **Key Value**: Delivers absolute flexibility to match any doctor's specific recommendations, clinical clarity through structured reports for prenatal appointments, and privacy through on-device zero-cloud encryption.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **App Unlock Flow**:
   - Launching the app presents the biometric authentication dialog. If biometrics are unconfigured, cancelled, or fail, a secure PIN keypad allows immediate access with lockout prevention.
2. **Session Setup & Active Tracking Flow**:
   - User opens the **Active Tracker** tab.
   - **Session Configuration Card** (before starting):
     - Select Kick Target: 5, 10 (default), 15, 20, or Continuous.
     - Select Duration Limit: 30 min, 1 hour, 2 hours (default), Custom (slider/input), or Untimed.
   - When started, shows a large circular interactive tap target with ripple wave animations and subtle haptic vibration upon tapping.
   - Live displays: Current Kicks / Target, Elapsed Time & Remaining Time (or elapsed for untimed), Average Time Between Kicks, and recent movement timestamp badges.
   - Action controls: Add Kick (+1), Undo Last Kick, Pause/Resume, Finish Early, or Cancel.
   - When target is hit or time completes, a celebratory gentle completion sheet displays with session duration and kick stats, prompting optional notes (e.g. baby hiccup, active kicking, after cold water/meal).
3. **Trends & Charts Flow**:
   - Visual trend line showing "Time taken to reach target" across sessions over 7, 14, or 30 days.
   - Bar chart showing "Kicks per hour" across morning, afternoon, and night to identify circadian activity peaks.
   - Filter by session duration or target type to compare like-for-like sessions.
4. **Visual Calendar View Flow**:
   - Monthly calendar grid highlighting tracked days with colored activity dots (green for completed target, amber for incomplete sessions).
   - Selecting a date reveals the detailed session cards for that day with duration, kick timestamps list, movement intensity, and notes.
5. **PDF Export Flow**:
   - User selects date range (Last 7 Days, Last 30 Days, Full Trimester, or Custom).
   - Generates a PDF with clinical summary tables, kick interval charts, and a note section for OB/GYN or midwife review, openable in any PDF reader or shareable via WhatsApp, Gmail, or Print.
6. **Settings & Reminders Flow**:
   - Dark/Light/System theme toggle.
   - Default session duration & kick target preference.
   - Daily push notification reminder scheduler (e.g., 8:00 PM evening active window).
   - Biometric toggle and PIN reset flow.

### Visual Identity & Theme

- **Aesthetic Direction**: Warm, serene, and medical-grade elegance. Soft organic geometry, rounded corners, and gentle typography that calms prenatal anxiety.
- **Color Palette**:
  - *Primary*: Rose Coral (`#E06D75` / Dark: `#F28D95`) — warm, vital, and human.
  - *Secondary / Accent*: Soft Sage Teal (`#4E878C` / Dark: `#72B1B7`) — soothing, restorative.
  - *Backgrounds*: Pure off-white (`#FDFBF7`) for light mode, deep slate-charcoal (`#14171A`) with surface cards (`#1E2328`) for dark mode.
  - *Status*: Warm Amber (`#E5A93B`) for warnings, Soft Emerald (`#43A047`) for completed sessions.
- **Typography**: Clean humanist hierarchy with bold tabular figures for timers and kick counters to prevent layout jumping during high-frequency taps.
- **Feedback & Micro-interactions**: Pulsing ripple waves on kick button press, instant tactile haptic feedback, smooth animated progress rings, and spring transitions.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Flexible Session Durations vs. Hardcoded 2-Hour Limit**:
  - *Chosen Approach*: Fully user-configurable session duration with intuitive presets (30 min, 1 hr, 2 hr, custom minutes, and untimed).
  - *Why*: Clinical protocols vary internationally (some providers recommend counting for 1 hour after meals; others recommend 2 hours; others recommend tracking 10 movements without time caps). Customizability accommodates every mother and provider.
- **Decision 2: Native Canvas PDF Generation**:
  - *Chosen Approach*: Android native `android.graphics.pdf.PdfDocument` with direct vector drawing.
  - *Why*: Instant offline generation without network dependencies, crisp vector typography, standard Letter/A4 format compatible with medical hospital printers and doctor email portals.
- **Decision 3: Local Encrypted Room Database**:
  - *Chosen Approach*: Room Database with encrypted preferences and AES-GCM data encryption via Android Keystore.
  - *Why*: Maternal medical data is strictly private. Keeping storage local and encrypted guarantees zero cloud data leakage while fulfilling the user's strict privacy requirement.
- **Decision 4: Resilient Session Timer State**:
  - *Chosen Approach*: Session state tracks `startTimestamp`, `pausedDurationMs`, and individual kick event timestamps rather than relying on a naive tick-counter.
  - *Why*: If the mother switches to another app or the screen locks during the session, the elapsed session time and kick logs remain accurate upon returning.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Fetal Kick Counter App                          │
├────────────────────────────────────────────────────────────────────────┤
│                           UI Layer (M3 Compose)                        │
│ ┌───────────────┐ ┌──────────────────────┐ ┌─────────────┐ ┌─────────┐ │
│ │ Biometric/PIN │ │ Customizable Tracker │ │   Trends &  │ │Calendar │ │
│ │  Lock Screen  │ │ (Custom Time/Target) │ │   Charts    │ │ History │ │
│ └───────┬───────┘ └──────────┬───────────┘ └──────┬──────┘ └────┬────┘ │
│         │                    │                    │             │      │
├─────────┴────────────────────┴────────────────────┴─────────────┴──────┤
│                        State & ViewModel Layer                         │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │   SessionViewModel    •    HistoryViewModel    •    AuthViewModel   │ │
│ └──────────────────────────────────┬─────────────────────────────────┘ │
├────────────────────────────────────┼───────────────────────────────────┤
│                        Domain / Service Layer                          │
│ ┌─────────────────┐ ┌──────────────┴────┐ ┌──────────────────────────┐ │
│ │  ReportExporter │ │ ReminderScheduler │ │   SecurityAuthManager    │ │
│ │ (PdfDocument)   │ │  (AlarmManager)   │ │(BiometricPrompt + KeySt.)│ │
│ └─────────┬───────┘ └──────┬────────────┘ └──────────┬───────────────┘ │
├───────────┴────────────────┴─────────────────────────┴─────────────────┤
│                         Data Persistence Layer                         │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │      Room Database (KickSessionDao, KickEventDao, AppDatabase)      │ │
│ │      Encrypted Preferences (User PIN, Notification Schedule)       │ │
│ └────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Entities & Schema

1. **`KickSessionEntity`**:
   - `id`: Long (Auto-increment primary key)
   - `startTimeMs`: Long
   - `endTimeMs`: Long
   - `durationSeconds`: Long
   - `targetDurationMinutes`: Int? (Null if untimed, or 30, 60, 120, etc.)
   - `targetKicks`: Int (e.g. 10, or 0 for continuous)
   - `totalKicks`: Int
   - `isTargetReached`: Boolean
   - `notes`: String
   - `fetalMovementStrength`: Int (1 = gentle flutter, 2 = moderate, 3 = vigorous)
2. **`KickEventEntity`**:
   - `id`: Long
   - `sessionId`: Long (Foreign key linking to session)
   - `timestampMs`: Long
   - `intervalSincePreviousMs`: Long
3. **`UserPreferences`**:
   - `defaultSessionDurationMinutes`: Int (e.g., 60, 120, or -1 for untimed)
   - `defaultKickTarget`: Int (5–20)
   - `isBiometricEnabled`: Boolean
   - `pinHash`: String
   - `pinSalt`: String
   - `isDarkMode`: Boolean
   - `dailyReminderHour`: Int
   - `dailyReminderMinute`: Int
   - `isReminderEnabled`: Boolean

---

## 5. Implementation Roadmap & Steps

1. **Configuration & Dependencies**:
   - Configure Room KSP and Android dependencies (`androidx.biometric:biometric-ktx`, navigation, icons, notifications).
   - Ensure all permissions (`POST_NOTIFICATIONS`, `USE_BIOMETRIC`) are declared in `AndroidManifest.xml`.
2. **Security & Cryptography Layer**:
   - Implement `SecurityAuthManager` supporting `BiometricPrompt` and SHA-256 salted PIN hash storage with hardware KeyStore encryption.
   - Build PinPad Composable for unlock, setup, and verification.
3. **Room Database Layer**:
   - Define `KickSessionEntity` (with flexible `targetDurationMinutes`), `KickEventEntity`, DAOs, and `KickRepository` with reactive Kotlin `Flow`.
4. **Session Tracking Engine**:
   - Build `SessionViewModel` supporting customizable duration limits (30m, 1h, 2h, custom, untimed) and customizable targets (5-20, continuous).
   - Design the Active Kick Screen with pulse animation, kick history timeline, and finish summary dialog.
5. **Analytics & Trend Visualizer**:
   - Build custom Canvas charts: Duration-to-target trend line graph and hourly kick distribution bar chart.
6. **Visual Calendar & History**:
   - Interactive month calendar grid with date selection, status indicators, and session detail breakdown.
7. **Doctor PDF Report Generator**:
   - Implement `PdfReportGenerator` using Android `PdfDocument` and share intent.
8. **Notification Reminders & Polish**:
   - Implement `ReminderBroadcastReceiver` and scheduling via `AlarmManager`.
   - Dark/Light Theme with M3 color scheme and polish.
