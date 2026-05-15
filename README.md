# LectureVault — Android Studio Project (Full Version)

**Tagline:** Capture. Organize. Study Smarter.

A local-first Android app for students to capture, organize, and manage photos of lectures, notes, books, whiteboards, and assignments — separate from the personal phone gallery.

## Tech stack
- Kotlin + Jetpack Compose (Material 3)
- MVVM (ViewModel + StateFlow)
- Room database (local-first, offline)
- CameraX for in-app photo capture
- Storage Access Framework for image import
- DataStore for theme & settings persistence
- AlarmManager + NotificationCompat for reminders
- FileProvider for safe sharing/PDF export
- Coil for image loading
- Accompanist Permissions

## How to open
1. Unzip the archive.
2. Open Android Studio (Hedgehog or newer).
3. **File → Open** and select the `LectureVault` folder.
4. Let Gradle sync (Gradle 8.7, AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06).
5. Run on a device/emulator with API 24+.

> Internet is required on the **first** sync to download Gradle and dependencies.

## Project structure
```
app/src/main/java/com/lecturevault/app/
├── MainActivity.kt              # entry + theme + notification channel
├── LectureVaultApp.kt
├── data/
│   ├── SettingsRepository.kt    # DataStore: theme / onboarded / camera quality
│   ├── database/AppDatabase.kt  # Room: Subject / Folder / Note / NotePage / Reminder + DAOs
│   └── repository/LectureRepository.kt
├── viewmodel/LectureViewModel.kt
├── navigation/LectureVaultNavHost.kt   # bottom nav + onboarding + camera + trash + reminders routes
├── ui/
│   ├── theme/Theme.kt           # Light/Dark/System driven by DataStore
│   └── screens/
│       ├── Screens.kt           # Home, Subjects, Favorites, Search, Settings, Subject detail, Note viewer
│       ├── OnboardingScreen.kt  # 4-page intro (HorizontalPager)
│       ├── CameraCaptureScreen.kt # CameraX preview + multi-page capture + save as note
│       └── TrashAndRemindersScreens.kt
└── utils/
    ├── FileUtils.kt             # internal storage + PdfExporter
    ├── ShareUtils.kt            # share via Intent
    └── ReminderScheduler.kt     # AlarmManager + BroadcastReceiver
```

## Implemented features (full version)

### Core
- 5 Room entities + DAOs: Subject / Folder / Note / NotePage / Reminder
- Repository + ViewModel with StateFlow streams
- Bottom Navigation: Home / Subjects / Favorites / Search / Settings

### Onboarding & Permissions
- 4-page onboarding with HorizontalPager (gated by DataStore)
- Permission explanation screen for Camera (Accompanist)

### Subjects
- Add / Edit / Archive / Delete
- 6 color presets + optional semester field
- Grid cards with color-tinted backgrounds

### Folders
- Create folders inside any subject
- Listed in subject detail screen

### Capture & Import
- **CameraX in-app capture**: live preview, multi-page capture, title field, save as one note
- Multi-image import via Storage Access Framework (saved to internal storage, never the gallery)

### Notes
- Multi-page note viewer (vertical scroll)
- Toggle favorite (star)
- Tag chips on note rows
- Move to trash / Restore / Permanent delete / Empty trash
- **Export to PDF** + **Share** via system chooser

### Reminders
- Create reminders (title + hours from now)
- Scheduled with `AlarmManager`
- Notification channel "reminders" registered on app start

### Search
- Live search by title / description / tags

### Settings
- **Theme switcher** (System / Light / Dark) — persisted via DataStore, applied app-wide
- Camera quality (High / Medium / Low) — persisted
- Quick links to Reminders, Trash, Backup placeholder, About

### Branding
- Adaptive launcher icon (vault + book vector on brand blue)
- Material 3 theming with custom palette

## Permissions handled
- `CAMERA` — for in-app capture (with explanation screen)
- `READ_MEDIA_IMAGES` (API 33+) / `READ_EXTERNAL_STORAGE` (≤32) — for import
- `POST_NOTIFICATIONS` (API 33+) — for reminders
- `SCHEDULE_EXACT_ALARM` — for reminders

## Storage
All captured/imported images and exported PDFs live under `context.filesDir/notes/` (internal app storage) and never appear in the user's gallery. Sharing is done via FileProvider with `${applicationId}.fileprovider`.

## Future work (not in MVP)
- OCR search inside images (ML Kit Text Recognition)
- Cloud sync (Firebase / Drive)
- Drag-to-reorder pages inside a multi-page note
- Crop / rotate / B&W enhance editor on captured pages
- Widget for quick capture
