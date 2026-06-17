# Architecture Documentation

## Overview

KeysCafe Keyboard follows a clean architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│  ┌─────────────────┐  ┌─────────────────────────────────┐   │
│  │  KeyboardView   │  │      SettingsActivity           │   │
│  │  (Compose UI)     │  │  (Jetpack Compose Screens)      │   │
│  └────────┬────────┘  └─────────────────────────────────┘   │
└───────────┼───────────────────────────────────────────────────┘
            │
┌───────────▼───────────────────────────────────────────────────┐
│                        IME LAYER                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │           KeysCafeInputMethodService                    │   │
│  │  ┌────────────┐  ┌────────────┐  ┌──────────────────┐   │   │
│  │  │  Keyboard  │  │   Touch    │  │     Sound        │   │   │
│  │  │  Renderer  │  │  Engine    │  │    Engine        │   │   │
│  │  └────────────┘  └────────────┘  └──────────────────┘   │   │
│  │  ┌────────────┐  ┌────────────┐  ┌──────────────────┐   │   │
│  │  │   Haptic   │  │   Theme    │  │    Layout        │   │   │
│  │  │  Engine    │  │  Manager   │  │   Manager        │   │   │
│  │  └────────────┘  └────────────┘  └──────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────┘
            │
┌───────────▼───────────────────────────────────────────────────┐
│                        DATA LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Layout JSON  │  │  Theme JSON  │  │  DataStore Prefs │   │
│  │   Files      │  │    Files     │  │   (Settings)     │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└───────────────────────────────────────────────────────────────┘
```

## Components

### 1. KeyboardRenderer
- Jetpack Compose-based rendering
- 60 FPS target with hardware acceleration
- Dynamic key sizing and positioning
- Multi-state animation system

### 2. TouchEngine
- Gesture detection (tap, long press, swipe)
- Multi-touch support
- Velocity tracking
- Fast repeat detection

### 3. LayoutManager
- JSON layout loading and caching
- Dynamic layout switching
- Memory-efficient caching
- Error recovery with fallback layouts

### 4. ThemeManager
- JSON theme loading
- Dynamic theme switching
- Color system with Compose integration
- Glow effect management

### 5. SettingsRepository
- DataStore preferences
- Type-safe settings access
- Settings flow for reactive UI

## Data Flow

```
User Touch → TouchEngine → KeyboardRenderer → IME Service → Android System
                ↓
         Sound/Haptic Engine
                ↓
         SettingsRepository (preferences)
```

## Performance Optimizations

1. **Layout Caching**: All layouts cached in memory after first load
2. **Lazy Loading**: Themes loaded on-demand
3. **Compose Optimization**: `remember` and `derivedStateOf` for state management
4. **Memory Management**: Cache clearing when memory pressure detected
5. **Frame Optimization**: Minimal recompositions with proper key usage
