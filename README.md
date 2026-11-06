# Ang Biblia / The Sefer – Android Bible apps

One codebase, two editions (Gradle product flavors). Kotlin + Jetpack Compose (Material 3). minSdk 26, targetSdk 35.

| Flavor      | App name   | Text                                   | applicationId                |
|-------------|------------|----------------------------------------|------------------------------|
| `angbiblia` | Ang Biblia | Tagalog Ang Biblia (1905)              | `com.tagangbiblia.app`       |
| `sefer`     | The Sefer  | American Standard Version (1901)       | `com.tagangbiblia.sefer`     |

They install side by side and keep separate bookmarks and settings.

## Build
Open this folder in Android Studio (Ladybug 2024.2+), let it sync, then pick a variant in
Build > Select Build Variant (`angbiblia…` or `sefer…`) and Run / Build APK(s).
Command line (JDK 17+, Android SDK): `gradle assembleSeferDebug`, `gradle assembleAngbibliaDebug`.

## Features
- Browse books and chapters; prev/next chapter bar; last position remembered
- Text size slider (14-40 sp) with live preview; Light / Dark / System theme
- Tap verses to select, then copy, bookmark or share
- Search: words (case/accent-insensitive, highlighted) or a reference like "Genesis 1:1"
- Bookmarks list (tap to jump to the verse)

## Databases
Bundled in `app/src/<flavor>/assets/`. Sefer.db is the ASV file you supplied, cleaned: the original contained every
verse twice (a typo-ridden copy and a corrected copy); only the corrected copy is kept (23,145 verses), then vacuumed.
If you replace an asset, bump DB_VERSION in BibleRepository.kt.
