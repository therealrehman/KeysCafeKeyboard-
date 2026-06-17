# 🔥 KeysCafe Keyboard

A professional Android custom keyboard application inspired by Samsung KeysCafe, built with **Jetpack Compose** and focused purely on **keyboard layout rendering and input**.

[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.02.00-purple.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## ✨ Features

### Keyboard Layouts
- **QWERTY** — Standard English layout
- **QWERTZ** — German layout
- **AZERTY** — French layout
- **Dvorak** — Programmer-friendly layout
- **Colemak** — Ergonomic layout
- **Numbers & Symbols** modes

All layouts support **instant dynamic switching** without restart.

### Themes
- 🔥 **Fire Glow** — Samsung KeysCafe inspired with per-key colored glows
- 🌑 **Dark** — Clean dark theme
- ☀️ **Light** — Clean light theme
- ⚫ **AMOLED Black** — Pure black for OLED screens

### Touch Engine
- Single Tap
- Double Tap
- Long Press
- Swipe (Left, Right, Up, Down)
- Multi-Touch
- Fast Repeated Taps

### Animations
- Multi-state key press: **White → Cyan → Pink → Orange**
- Ripple effects
- Keyboard-wide glow expansion
- Smooth scale transitions

### Customization
- Key sizes: Small, Medium, Large, Extra Large
- Custom size slider: 50% to 200%
- Sound effects with volume control
- Haptic feedback: None, Light, Medium, Strong
- Glow effect toggle

### Accessibility
- TalkBack support
- Large keys mode
- High contrast mode
- Color blind mode
- Screen reader labels

---

## 📸 Screenshots

| Fire Glow Theme | Dark Theme | Settings |
|:---:|:---:|:---:|
| ![Fire](docs/screenshots/fire_theme.png) | ![Dark](docs/screenshots/dark_theme.png) | ![Settings](docs/screenshots/settings.png) |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│  ┌─────────────┐    ┌────────────────┐  │
│  │ KeyboardView│    │ SettingsScreen│  │
│  │  (Compose)  │    │   (Compose)    │  │
│  └──────┬──────┘    └────────────────┘  │
└─────────┼───────────────────────────────┘
          │
┌─────────▼───────────────────────────────┐
│            IME Layer                    │
│  ┌────────────────────────────────────┐ │
│  │   KeysCafeInputMethodService       │ │
│  │  ┌──────────┐  ┌────────────────┐  │ │
│  │  │Keyboard  │  │  TouchEngine   │  │ │
│  │  │Renderer  │  │  (Gestures)    │  │ │
│  │  └──────────┘  └────────────────┘  │ │
│  │  ┌──────────┐  ┌────────────────┐  │ │
│  │  │  Sound   │  │ HapticEngine   │  │ │
│  │  │ Engine   │  │ (Vibration)    │  │ │
│  │  └──────────┘  └────────────────┘  │ │
│  └────────────────────────────────────┘ │
└─────────────────────────────────────────┘
          │
┌─────────▼───────────────────────────────┐
│           Data Layer                    │
│  ┌──────────┐  ┌──────────┐           │
│  │ Layout   │  │  Theme   │           │
│  │ Manager  │  │ Manager  │           │
│  │(JSON)    │  │ (JSON)   │           │
│  └──────────┘  └──────────┘           │
│  ┌────────────────────────────────┐   │
│  │   SettingsRepository           │   │
│  │   (DataStore Preferences)    │   │
│  └────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Kotlin 1.9.20

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/yourusername/KeysCafeKeyboard.git
cd KeysCafeKeyboard

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Enable Keyboard
1. Open **Settings → System → Languages & Input → On-screen keyboard → Manage keyboards**
2. Enable **KeysCafe Keyboard**
3. Tap **Select Keyboard** and choose **KeysCafe Keyboard**

---

## 📁 Project Structure

```
app/src/main/
├── java/com/keys/cafe/keyboard/
│   ├── KeysCafeApplication.kt
│   ├── KeysCafeInputMethodService.kt
│   ├── model/              # Data models
│   ├── layout/             # Layout engine
│   ├── theme/              # Theme engine
│   ├── touch/              # Touch detection
│   ├── sound/              # Sound effects
│   ├── haptic/             # Vibration feedback
│   ├── render/             # Compose rendering
│   ├── settings/           # Settings UI
│   ├── data/               # DataStore repository
│   ├── accessibility/      # TalkBack support
│   └── util/               # Error handling & performance
├── res/
│   ├── values/             # Strings, colors, themes
│   └── xml/                # IME method config
└── assets/
    ├── layouts/            # JSON layout files
    │   ├── qwerty.json
    │   ├── qwertz.json
    │   ├── azerty.json
    │   ├── dvorak.json
    │   ├── colemak.json
    │   ├── numbers.json
    │   └── symbols.json
    └── themes/             # JSON theme files
        ├── fire.json
        ├── dark.json
        ├── light.json
        └── amoled.json
```

---

## 🎨 Custom Themes

Create your own theme by adding a JSON file to `assets/themes/`:

```json
{
  "id": "mytheme",
  "name": "My Custom Theme",
  "type": "CUSTOM",
  "backgroundColor": 4278190080,
  "keyColor": 4282664004,
  "keyPressedColor": 4294967295,
  "keyTextColor": 4294967295,
  "keyPressedTextColor": 4278190080,
  "borderColor": 4285558896,
  "shadowColor": 4278190080,
  "cornerRadius": 8.0,
  "glowEnabled": true,
  "glowIntensity": 0.8,
  "animationSpeed": 1.0
}
```

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

---

## 📈 Performance

| Metric | Target | Status |
|--------|--------|--------|
| Keyboard Open Time | < 50ms | ✅ |
| Touch Response | < 10ms | ✅ |
| Memory Usage | < 100MB | ✅ |
| Frame Rate | 60 FPS | ✅ |
| Battery Impact | Optimized | ✅ |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Inspired by **Samsung KeysCafe**
- Built with **Jetpack Compose**
- Icons by **Material Design**

---

<p align="center">
  Made with ❤️ and 🔥
</p>
