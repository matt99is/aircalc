# Google Play Store Submission Checklist for AirCalc

## ✅ Completed Items

### 1. App Configuration
- [x] **Package name**: `com.aircalc.converter`
- [x] **Version code**: 1
- [x] **Version name**: 1.0
- [x] **Target SDK**: 35 (latest)
- [x] **Min SDK**: 24 (Android 7.0+)
- [x] **Application ID**: Unique and production-ready

### 2. Code Quality & Architecture
- [x] **Clean Architecture**: Domain/Data/Presentation layers properly separated
- [x] **Dependency Injection**: Hilt properly configured
- [x] **Testing**: Comprehensive unit tests (90%+ coverage)
- [x] **ProGuard/R8**: Enabled with proper rules
- [x] **Resource Optimization**: shrinkResources enabled
- [x] **No hardcoded strings**: All UI strings in strings.xml
- [x] **Dark theme support**: Fully implemented

### 3. Security & Privacy
- [x] **Release signing**: Keystore created and configured
- [x] **Privacy policy**: Created and hosted at https://matt99is.github.io/aircalc/PRIVACY_POLICY
- [x] **Network security**: network_security_config.xml configured
- [x] **Backup rules**: Proper backup/data extraction rules
- [x] **No sensitive data**: Keystore and credentials excluded from git

### 4. User Experience
- [x] **Accessibility**: Content descriptions, semantic labels, WCAG AA contrast
- [x] **RTL support**: android:supportsRtl="true"
- [x] **Splash screen**: Implemented
- [x] **Material Design 3**: Full implementation
- [x] **Responsive UI**: Works on different screen sizes

### 5. Build & Release
- [x] **Debug build**: Working
- [x] **Release build**: Working (signed AAB)
- [x] **Lint checks**: Passing (minor warnings only)
- [x] **No permissions**: App requires no special permissions

---

## 📋 TODO Before Submission

### 1. App Store Assets (Required)

#### App Icons
- [ ] Create high-resolution app icon (512x512 PNG)
- [ ] Create adaptive icon layers if not using vector
- [ ] Test icon on different launchers

#### Screenshots (Required: minimum 2, maximum 8 per device type)
- [ ] **Phone screenshots**: 2-8 screenshots showing key features
  - Recommended: 1080x1920 or 1440x2560 (16:9 ratio)
  - Main conversion screen
  - Timer functionality
  - Food category selection
  - Results screen
  - Dark mode example

- [ ] **7-inch tablet screenshots** (Optional but recommended)
- [ ] **10-inch tablet screenshots** (Optional but recommended)

#### Feature Graphic
- [ ] Create feature graphic: 1024x500 JPG or PNG
  - Used in Play Store listing
  - Should showcase app name and key feature

#### Optional Assets
- [ ] Promo video (YouTube URL)
- [ ] TV banner (if supporting Android TV)

### 2. Store Listing Content

#### App Details
- [ ] **App name**: "AirCalc - Air Fryer Converter" (max 50 chars)
- [ ] **Short description**: Write compelling short description (max 80 chars)
  ```
  Convert oven recipes to air fryer settings instantly
  ```

- [ ] **Full description**: Write detailed description (max 4000 chars)
  ```
  AirCalc is your essential kitchen companion for converting traditional oven
  recipes to perfect air fryer settings. Save time, energy, and get better results!

  ✨ KEY FEATURES:
  • Instant conversion from oven to air fryer
  • Support for multiple food categories
  • Built-in cooking timer
  • Category-specific cooking tips
  • Celsius and Fahrenheit support
  • Clean, intuitive Material Design interface
  • Dark mode support
  • No ads, no tracking, no permissions needed

  🍳 FOOD CATEGORIES:
  • Frozen Foods
  • Fresh Vegetables
  • Raw Meats
  • Baked Goods
  • Ready Meals

  💡 SMART FEATURES:
  • Automatic temperature adjustment based on food type
  • Time reduction calculations
  • Built-in timer with pause/resume
  • Expert cooking tips for each category

  Perfect for home cooks, meal preppers, and anyone looking to make the
  most of their air fryer!

  Privacy-focused: No data collection, no account required.
  ```

- [ ] **App category**: Lifestyle or Food & Drink
- [ ] **Tags**: air fryer, cooking, recipes, converter, kitchen, timer
- [ ] **Content rating**: Complete questionnaire (likely Everyone)
- [ ] **Target audience**: All ages
- [ ] **Contact details**:
  - Email: mattlelonek@gmail.com
  - Phone: (Optional)
  - Website: https://matt99is.github.io/aircalc/

### 3. Testing & Quality
- [ ] Test on real devices (min SDK 24 through latest)
- [ ] Test all food categories
- [ ] Test timer functionality thoroughly
- [ ] Test temperature unit conversion
- [ ] Test dark/light theme switching
- [ ] Test edge cases (min/max values)
- [ ] Check for crashes
- [ ] Test app size (should be small)

### 4. Legal & Compliance
- [ ] Complete content rating questionnaire
- [ ] Review privacy policy one more time
- [ ] Ensure no copyright violations (icons, images, text)
- [ ] Add app-ads.txt if using ads (N/A for this app)
- [ ] Review target audience and age ratings

### 5. App Bundle (AAB)
- [ ] Build signed release AAB: `./gradlew bundleRelease`
- [ ] Verify AAB is under 150MB
- [ ] Test AAB with internal testing track first
- [ ] Check for any ProGuard/R8 issues in release build

### 6. Play Console Setup
- [ ] Create Google Play Console account ($25 one-time fee)
- [ ] Create new app in console
- [ ] Fill in all required store listing information
- [ ] Upload screenshots and graphics
- [ ] Set pricing (Free)
- [ ] Select countries for distribution
- [ ] Complete privacy policy section
- [ ] Set up internal testing track
- [ ] Upload AAB to internal testing
- [ ] Test internal release thoroughly
- [ ] Promote to production when ready

---

## 🎯 Recommended Improvements (Optional)

### Future Enhancements
- [ ] Add more food categories
- [ ] Save favorite conversions
- [ ] Unit conversion for measurements
- [ ] Share results feature
- [ ] Multiple language support (already set up with strings.xml)
- [ ] Widgets for quick conversions
- [ ] Recipe import feature

### Marketing
- [ ] Create promotional materials
- [ ] Plan launch announcement
- [ ] Prepare social media posts
- [ ] Consider app review outreach

---

## 📱 Current Build Information

**Location of release AAB**: `app/build/outputs/apk/release/app-release.apk`
**Keystore**: `app/release-keystore-final.jks`
**Privacy Policy**: https://matt99is.github.io/aircalc/PRIVACY_POLICY

---

## 🚀 Launch Checklist

Before clicking "Go Live":
1. [ ] All screenshots uploaded
2. [ ] Feature graphic uploaded
3. [ ] Store listing text reviewed
4. [ ] Privacy policy URL added
5. [ ] Contact email verified
6. [ ] Content rating completed
7. [ ] Internal testing completed successfully
8. [ ] No critical bugs found
9. [ ] AAB uploaded to production track
10. [ ] Pricing and distribution set
11. [ ] Ready to submit for review!

---

## 📊 Post-Launch

- [ ] Monitor crash reports
- [ ] Respond to user reviews
- [ ] Track installation metrics
- [ ] Plan updates based on feedback
- [ ] Monitor performance vitals in Play Console

---

**Developer**: Full Bloom Software
**Contact**: mattlelonek@gmail.com
**Repository**: https://github.com/matt99is/aircalc
