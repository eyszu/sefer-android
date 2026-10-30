# Ang Biblia (Tagalog, 1905) – Ang Biblia with Original Name of God in Android app

Kotlin + Jetpack Compose (Material 3). minSdk 26, targetSdk 35.

## Build
1. Open this folder in Android Studio (Ladybug 2024.2 or newer) and let it sync.
2. Run on a device/emulator, or Build > Build APK(s).
   Command line: `gradle assembleDebug` (needs JDK 17+ and the Android SDK).

## Features
- Browse books and chapters; prev/next chapter bar; last position is remembered
- Text size slider (14–40 sp) with live preview
- Light / Dark / System theme
- Tap verses to select, then copy, bookmark or share
- Bookmarks list (tap to jump to the verse, trash icon to remove)

The Bible database is bundled at app/src/main/assets/TagAngBiblia.db (vacuumed copy of
TagAngBiblia_YHWH.db: same data, 42 MB -> 4 MB). Bookmarks are stored separately in user.db.
If you replace the asset, bump DB_VERSION in BibleRepository.kt.
