# Sefer (ספר)

A Bible reader for Android 

## Features
- Kotlin, Jetpack Compose, Material 3. Offline, no network permission.
- **Versions:** Tagalog (Ang Biblia, 1905) and English (ASV, 1901). Switch from the chip in the reader header or from the Aa sheet. Book names follow the version.
- **Themes:** Light, Dark, Dusk, Dawn (first launch follows the system light/dark setting).
- **Fonts:** Literata (default), Lora, Merriweather, Frank Ruhl Libre, Inter, Atkinson — bundled, so no downloads.
- **Text size:** 12–32 sp slider with live preview.
- **Search:** every word must match, results highlighted, up to 500 verses. Also understands references — `john 3:16`, `ps 23`, `1 sam 3`, `mga awit 23` — and offers a "Go to" shortcut.
- **Bookmarks:** tap a verse → bookmark / copy / share. Bookmarks are by reference, so they appear in either version.
- **Icon:** golden Star of David (adaptive + themed/monochrome).

## Build
Open this folder in Android Studio (Ladybug or newer) and run the `app` configuration, or:

    gradle wrapper        # once, if you want ./gradlew
    ./gradlew assembleDebug

Requires JDK 17, compileSdk 35, minSdk 26 (Android 8.0).

## Pre-build ready to install
- copy and install to android phone the file: sefer-v0.2.apk

## Data notes
- Both supplied databases contain the **Old Testament only** (39 books, 23,145 verses). The app builds its book list from the data, and all 66 book names (English + Tagalog) are already in `BookNames.kt`, so dropping in New Testament data needs no code changes.
- `tools/build_dbs.py` regenerates `app/src/main/assets/*.db` from the source files. Cleaning applied: 4 duplicated Tagalog verses resolved (corrected rows kept); the divine name, stored as Phoenician characters, converted to Hebrew letters (יהוה) so it renders instead of showing empty boxes.
- After changing a `.db`, bump `BibleRepository.DATA_VERSION`.

## Licenses
ASV (1901) and Ang Biblia (1905) are public domain. Fonts are SIL Open Font License 1.1 (Literata, Lora, Merriweather, Frank Ruhl Libre, Inter, Atkinson Hyperlegible Next); static Regular/Bold instances were cut from the Google Fonts variable files.
