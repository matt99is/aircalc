# Changelog

All notable changes to AirCalc will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Updated contact email to matt@glassllama.co.uk

## [1.6] - 2025-10-19

### Changed
- **Major formula update for Frozen Foods**: Now uses same temperature (0°F reduction) with 50% time reduction for better results matching manufacturer guidelines
- **Fresh Vegetables**: Adjusted to 25°F temp reduction and 80% time multiplier (from 30°F/75%)
- **Raw Meats**: Adjusted time multiplier to 80% (from 85%)
- Updated all unit tests to match new conversion formulas

### Fixed
- Fixed blank screen issue when switching device theme (dark/light mode) while app is open
- Fixed app state loss when returning from background during theme changes
- Improved theme handling with proper configuration change management

## [1.5] - 2025-10-18

### Fixed
- Fixed build warnings and improved code quality
- Cleaned up unnecessary files and improved directory structure

## [1.4] - 2025-10-17

### Changed
- Updated UI colors for better visual consistency
- Updated privacy policy with GlassLlama details

### Fixed
- Fixed status bar display issues

## [1.3] - 2025-10-16

### Changed
- Removed analytics mentions from privacy policy
- Added HTML privacy policy for GitHub Pages hosting

## [1.1] - 2025-10-15

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

## [1.0] - Initial Development

### Added
- Initial project setup
- Basic conversion calculator structure

---

## Google Play Release Notes

### Version 1.6
```
• Updated conversion formulas based on industry research for more accurate results
• Frozen foods now cook in half the time at the same temperature
• Fixed blank screen issue when switching between dark/light mode
• Improved app stability when returning from background
```

### Version 1.5
```
• Fixed build warnings and improved code quality
• Performance improvements and cleanup
```

### Version 1.4
```
• Updated UI colors for better visual experience
• Fixed status bar display issues
```

### Version 1.1
```
• Initial release of AirCalc
• Convert oven recipes to air fryer settings instantly
• Support for multiple food categories with smart conversions
• Built-in cooking timer
• Dark mode support
• No ads, no tracking, completely privacy-focused
```

---

[unreleased]: https://github.com/matt99is/aircalc/compare/v1.6...HEAD
[1.6]: https://github.com/matt99is/aircalc/releases/tag/v1.6
[1.5]: https://github.com/matt99is/aircalc/releases/tag/v1.5
[1.4]: https://github.com/matt99is/aircalc/releases/tag/v1.4
[1.3]: https://github.com/matt99is/aircalc/releases/tag/v1.3
[1.1]: https://github.com/matt99is/aircalc/releases/tag/v1.1
[1.0]: https://github.com/matt99is/aircalc/releases/tag/v1.0
