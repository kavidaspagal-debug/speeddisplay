# Speed Display - Android GPS Speedometer

A clean, minimal GPS speedometer designed for HUD (heads-up display) use on windshields.

## Features
- **Big digits only** — no clutter, no decorations
- **Horizontal (landscape) screen** locked orientation
- **Single tap** — mirrors the display horizontally for windshield reflection
- **Tap & hold** — opens color settings
- **Color options:** Green, Blue, Red, Yellow
- **Black background** always
- **Screen stays on** while app is open

## How to Build

### Requirements
- Android Studio (Hedgehog 2023.1.1 or newer)
- Android SDK 34
- Kotlin 1.9+

### Steps
1. Open Android Studio
2. Select **File → Open** and choose the `SpeedDisplay` folder
3. Wait for Gradle sync to complete
4. Connect your Android phone (USB debugging enabled)
5. Press **Run ▶** or use **Build → Build APK**

### Install APK directly
If you have the APK:
```
adb install SpeedDisplay.apk
```

## Usage
- **Launch the app** — it will request GPS permission, tap Allow
- **Drive** — speed shows in km/h using your GPS
- **Single tap** — flip the display (mirror mode for windshield HUD)
- **Hold 2 seconds** — open color settings (Green / Blue / Red / Yellow)
- **Works best** when phone is placed on dashboard facing the windshield in mirror mode

## Project Structure
```
SpeedDisplay/
├── app/
│   └── src/main/
│       ├── java/com/speeddisplay/
│       │   ├── MainActivity.kt       ← GPS + display logic
│       │   └── SettingsDialog.kt     ← Color picker dialog
│       ├── res/
│       │   ├── font/eras_itc_demi.ttf
│       │   ├── layout/activity_main.xml
│       │   └── values/
│       │       ├── strings.xml
│       │       └── themes.xml
│       └── AndroidManifest.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Notes
- GPS accuracy depends on your phone and signal quality
- Speed updates every 500ms
- Mirror mode state is saved between sessions
- Selected color is saved between sessions
