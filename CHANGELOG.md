# Changelog

All notable changes to AirCalc will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.7.1] - 2025-10-25

### Fixed
- Fixed button lag on timer controls - removed unnecessary delays for instant response
- Fixed button labels to properly show Start → Pause → Resume flow
- Fixed notification appearing briefly when app is in foreground
- Locked phone orientation to portrait for better UX (tablets can still rotate)

### Changed
- **Optimized notification updates** - Now updates only when minutes change instead of every second (60x reduction in battery usage)
- **Simplified notification display** - Shows time in minutes only (e.g., "15 min") instead of MM:SS format
- Optimized wake lock timeout to be dynamic based on timer duration + 5min buffer (previously fixed 1 hour)
- Improved MediaPlayer with proper cancellation handling to prevent potential crashes
- Removed emoji icons from timer control buttons for cleaner UI

### Technical
- Created Constants.kt for centralized configuration values
- Added distinctUntilChanged() with ConversionInputKey to prevent unnecessary UI recompositions
- Extracted BorderCard component to shared CommonComponents.kt to eliminate code duplication
- Improved notification service lifecycle management
- Better coroutine cancellation handling in timer service

## [1.7.0] - 2025-10-24

### Fixed
- **CRITICAL: Fixed timer state loss on screen rotation** - Timer now properly survives device rotation and configuration changes
- **CRITICAL: Fixed MediaPlayer memory leak on rotation** - MediaPlayer now managed in ViewModel to prevent leaks across configuration changes
- Fixed timer pause/resume functionality - Timer now properly pauses and resumes without resetting
- Fixed timer reset behavior - Reset now reinitializes timer with cooking time instead of showing 00:00
- Fixed timer start button - Now resumes paused timer instead of restarting from beginning
- Fixed system back button navigation from timer screen to main screen

### Changed
- **App now defaults to Celsius (°C)** with 180°C default temperature for better international UX
- Migrated timer management from MainActivity to ViewModel for proper lifecycle handling
- Updated MainActivity to handle orientation and screen size changes without Activity recreation
- Refactored timer state to use ViewModel's TimerManager instead of legacy Composable state
- MediaPlayer now properly managed in AndroidViewModel with Application context
- Updated contact email to matt@glassllama.co.uk
- Switched to semantic versioning (MAJOR.MINOR.PATCH format)
- Replaced company name from Full Bloom Software to GlassLlama

### Technical
- Added `orientation|screenSize|keyboardHidden` to AndroidManifest configChanges
- ViewModel extends AndroidViewModel for safe MediaPlayer management
- Timer state survives configuration changes via ViewModel scope
- MediaPlayer properly released in onCleared() to prevent memory leaks
- Timer coroutine job stays alive when paused to enable proper resume functionality
- Added BackHandler to ConversionResultsScreen for proper back navigation
- Converted legacy TimerState class to abstract adapter pattern

### Removed
- Removed redundant documentation files (PLAY_STORE_CHECKLIST.md, PRIVACY_POLICY.md, RELEASE_NOTES.md)
- Removed legacy timer implementation code from MainActivity (MediaPlayer, vibration, LaunchedEffect)

## [1.6.1] - 2025-10-24

### Fixed
- Fixed LRU cache implementation for better performance and proper eviction of least-recently-used items
- Fixed TimerManager lifecycle to prevent potential memory leaks
- Removed artificial 500ms delay in conversion operations for instant cached results

### Changed
- Refactored MainActivity to properly use ViewModel (MVVM architecture)
- Extracted window theme management logic into dedicated WindowThemeManager class for better maintainability
- Added @Immutable annotations to domain models for improved Compose performance
- Improved code organization and separation of concerns

### Removed
- Removed legacy AirFryerConverter.kt (83 lines of dead code)
- Removed legacy TimerManager.kt (68 lines of dead code)

### Technical
- MainActivity reduced from 1,663 to 1,615 lines (-3%)
- Proper thread-safe LRU cache using LinkedHashMap with access-order
- TimerManager lifecycle now tied to ViewModel scope instead of singleton
- Created gradle.properties with proper AndroidX configuration
- All conversions now properly validated through domain layer
- Better Compose recomposition performance with immutable state

## [1.6.0] - 2025-10-19

### Changed
- **Major formula update for Frozen Foods**: Now uses same temperature (0°F reduction) with 50% time reduction for better results matching manufacturer guidelines
- **Fresh Vegetables**: Adjusted to 25°F temp reduction and 80% time multiplier (from 30°F/75%)
- **Raw Meats**: Adjusted time multiplier to 80% (from 85%)
- Updated all unit tests to match new conversion formulas

### Fixed
- Fixed blank screen issue when switching device theme (dark/light mode) while app is open
- Fixed app state loss when returning from background during theme changes
- Improved theme handling with proper configuration change management

## [1.5.0] - 2025-10-18

### Fixed
- Fixed build warnings and improved code quality
- Cleaned up unnecessary files and improved directory structure

## [1.4.0] - 2025-10-17

### Changed
- Updated UI colors for better visual consistency
- Updated privacy policy with GlassLlama details

### Fixed
- Fixed status bar display issues

## [1.3.0] - 2025-10-16

### Changed
- Removed analytics mentions from privacy policy
- Added HTML privacy policy for GitHub Pages hosting

## [1.1.0] - 2025-10-15

### Added
- Core conversion functionality for all major food categories (Frozen Foods, Fresh Vegetables, Raw Meats, Ready Meals)
- Modern Material Design 3 interface with dynamic theming
- Temperature unit conversion (°F ↔ °C)
- Built-in cooking timer with pause/resume functionality
- Real-time validation and conversion estimates
- Category-specific cooking tips
- Dark mode support with custom color scheme
- Optimized performance and small app size
- Clean Architecture implementation (Domain/Data/Presentation layers)
- Hilt dependency injection
- Comprehensive unit test coverage

### Technical
- Target SDK 35 (Android 15)
- Minimum SDK 24 (Android 7.0+)
- Jetpack Compose UI
- Material Design 3 theming
- ProGuard/R8 optimization enabled

## [1.0.0] - Initial Development

### Added
- Initial project setup
- Basic conversion calculator structure

---

## Google Play Release Notes

### Version 1.7.1
```
• Massive battery optimization: Notifications now update 60x less frequently
• Fixed timer button lag for instant response
• Fixed button labels: Start → Pause → Resume flow
• Locked phone orientation to portrait (tablets can still rotate)
• Cleaner UI: Removed emoji icons from buttons
• Notification now shows time in minutes only
• Performance improvements and bug fixes
```

### Version 1.7.0
```
• MAJOR FIX: Timer now survives screen rotation without losing time
• MAJOR FIX: Fixed memory leaks for much better stability
• Fixed timer pause/resume - now works correctly without resetting
• Fixed timer reset - now shows cooking time instead of 00:00
• App now defaults to Celsius (°C) for better international support
• Fixed back button navigation from timer screen
• Improved app reliability and performance
```

### Version 1.6.1
```
• Performance improvements: Faster conversions with optimized caching
• Fixed potential memory leaks for improved stability
• Code quality improvements and bug fixes
• Better app performance and responsiveness
```

### Version 1.6.0
```
• Updated conversion formulas based on industry research for more accurate results
• Frozen foods now cook in half the time at the same temperature
• Fixed blank screen issue when switching between dark/light mode
• Improved app stability when returning from background
```

### Version 1.5.0
```
• Fixed build warnings and improved code quality
• Performance improvements and cleanup
```

### Version 1.4.0
```
• Updated UI colors for better visual experience
• Fixed status bar display issues
```

### Version 1.1.0
```
• Initial release of AirCalc
• Convert oven recipes to air fryer settings instantly
• Support for multiple food categories with smart conversions
• Built-in cooking timer
• Dark mode support
• No ads, no tracking, completely privacy-focused
```

---

## Versioning Guide

AirCalc follows [Semantic Versioning](https://semver.org/):
- **MAJOR.MINOR.PATCH** (e.g., 1.6.0)
- **MAJOR** (2.0.0) - Breaking changes, complete redesigns
- **MINOR** (1.7.0) - New features, significant updates
- **PATCH** (1.6.1) - Bug fixes, small improvements

---

[unreleased]: https://github.com/matt99is/aircalc/compare/v1.7.1...HEAD
[1.7.1]: https://github.com/matt99is/aircalc/releases/tag/v1.7.1
[1.7.0]: https://github.com/matt99is/aircalc/releases/tag/v1.7.0
[1.6.1]: https://github.com/matt99is/aircalc/releases/tag/v1.6.1
[1.6.0]: https://github.com/matt99is/aircalc/releases/tag/v1.6.0
[1.5.0]: https://github.com/matt99is/aircalc/releases/tag/v1.5.0
[1.4.0]: https://github.com/matt99is/aircalc/releases/tag/v1.4.0
[1.3.0]: https://github.com/matt99is/aircalc/releases/tag/v1.3.0
[1.1.0]: https://github.com/matt99is/aircalc/releases/tag/v1.1.0
[1.0.0]: https://github.com/matt99is/aircalc/releases/tag/v1.0.0
