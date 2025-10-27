# AirCalc - Release Build Testing Checklist

Use this checklist to verify that the **release build** (with ProGuard/R8 enabled) works correctly before submitting to Google Play Store.

**Device Under Test:** ___________________________
**Android Version:** ___________________________
**Test Date:** ___________________________
**Tester:** ___________________________

---

## ✅ Pre-Test Setup

- [ ] Release APK installed successfully
- [ ] Previous debug version uninstalled
- [ ] `adb logcat` running in separate terminal to catch crashes
- [ ] Device has working notification access
- [ ] Device has working vibration (optional but recommended)

---

## ✅ 1. App Launch and Initialization

### Test: Cold Start
- [ ] App launches successfully (no crash)
- [ ] Splash screen displays correctly
- [ ] Main screen loads within 2-3 seconds
- [ ] Default values displayed: 180°C, 25 minutes, Ready meals selected
- [ ] No crash logs in logcat

**Notes:** ___________________________________________________

---

## ✅ 2. Food Category Selection

### Test: All Categories
- [ ] "Ready meals" category button is visible and clickable
- [ ] "Raw meat" category button is visible and clickable
- [ ] "Veg" category button is visible and clickable
- [ ] "Frozen" category button is visible and clickable

### Test: Category Changes UI
- [ ] Tapping "Frozen" updates the description text
- [ ] Tapping "Raw meat" updates the description text
- [ ] Tapping "Veg" updates the description text
- [ ] Selected category is visually highlighted

**Notes:** ___________________________________________________

---

## ✅ 3. Temperature Input

### Test: Temperature Adjustment
- [ ] Plus button increases temperature by 5°C
- [ ] Minus button decreases temperature by 5°C
- [ ] Temperature display updates immediately
- [ ] Can reach 250°C (max) by tapping plus
- [ ] Can reach 120°C (min) by tapping minus
- [ ] Hold-to-repeat works (hold button for rapid changes)

### Test: Temperature Unit Toggle
- [ ] Temperature unit toggle shows "°C" and "°F"
- [ ] Tapping toggle switches from °C to °F
- [ ] Temperature value converts correctly (180°C → 356°F)
- [ ] Tapping again switches back to °C
- [ ] Conversion calculations use the selected unit

**Notes:** ___________________________________________________

---

## ✅ 4. Cooking Time Input

### Test: Time Adjustment
- [ ] Plus button increases time by 5 minutes
- [ ] Minus button decreases time by 5 minutes
- [ ] Time display updates immediately
- [ ] Can reach 180 minutes (max) by tapping plus
- [ ] Can reach 1 minute (min) by tapping minus
- [ ] Hold-to-repeat works for time buttons

**Notes:** ___________________________________________________

---

## ✅ 5. Conversion Flow

### Test: Convert Button
- [ ] "Convert" button is visible and enabled with valid input
- [ ] Tapping "Convert" navigates to results screen
- [ ] Navigation animation is smooth
- [ ] No crash during conversion
- [ ] No crash during navigation

### Test: Conversion Results Display
- [ ] Air fryer temperature is displayed
- [ ] Air fryer cooking time is displayed
- [ ] Results appear correct for selected category
  - Example: 180°C, 25 min, Ready meals → ~155°C, ~19 min
- [ ] "Air fryer settings" title is visible
- [ ] Category-specific cooking tip is displayed

**Notes:** ___________________________________________________

---

## ✅ 6. Timer Functionality (CRITICAL)

### Test: Timer Start
- [ ] Timer displays initial cooking time from conversion
- [ ] "Start" button is visible
- [ ] Tapping "Start" begins countdown
- [ ] Timer updates every second
- [ ] Circular progress indicator animates
- [ ] "Pause" button appears when timer is running

### Test: Timer Pause/Resume
- [ ] Tapping "Pause" stops the countdown
- [ ] Time remains frozen while paused
- [ ] "Resume" button appears when paused
- [ ] Tapping "Resume" continues countdown from paused time
- [ ] Timer accuracy is maintained (doesn't skip or jump)

### Test: Timer Reset
- [ ] "Reset" button is visible
- [ ] Tapping "Reset" stops the timer
- [ ] Timer resets to initial cooking time
- [ ] "Start" button appears after reset
- [ ] Can start timer again after reset

### Test: Timer Completion
- [ ] Let timer run for at least 2 minutes, then skip to completion
- [ ] When timer reaches 0:00, notification appears
- [ ] Notification shows "Timer Complete!" or similar message
- [ ] Tapping notification opens the app
- [ ] Device vibrates when timer completes (if supported)
- [ ] Timer alarm sound plays (respects Do Not Disturb mode)

**Notes:** ___________________________________________________

---

## ✅ 7. Navigation and Back Button

### Test: Back Navigation
- [ ] App title "AirCalc" in results screen acts as back button
- [ ] Tapping "AirCalc" returns to input screen
- [ ] Previous input values are preserved (temp, time, category)
- [ ] Device back button also navigates back from results screen
- [ ] Navigation is smooth without crashes

**Notes:** ___________________________________________________

---

## ✅ 8. Dark Mode Support

### Test: Theme Switching
- [ ] Enable dark mode in device settings
- [ ] App switches to dark theme automatically
- [ ] All text is readable in dark mode
- [ ] All buttons are visible in dark mode
- [ ] Timer progress ring is visible in dark mode
- [ ] Switch back to light mode
- [ ] App switches to light theme automatically
- [ ] All elements are readable in light mode

**Notes:** ___________________________________________________

---

## ✅ 9. Screen Rotation (Tablets/Devices that Allow Rotation)

### Test: Portrait to Landscape
- [ ] Rotate device from portrait to landscape
- [ ] App re-layouts correctly (if tablet)
- [ ] Input values are preserved after rotation
- [ ] Timer continues running through rotation
- [ ] No crashes during rotation
- [ ] Selected food category is preserved

**Notes:** ___________________________________________________

---

## ✅ 10. Offline Functionality

### Test: Airplane Mode
- [ ] Enable airplane mode on device
- [ ] App continues to work normally
- [ ] Conversions still work
- [ ] Timer still works
- [ ] No network error messages appear
- [ ] Disable airplane mode - app still works

**Notes:** ___________________________________________________

---

## ✅ 11. Permissions (Android 12+)

### Test: Notification Permission
- [ ] App requests notification permission on first launch (Android 13+)
- [ ] Granting permission allows timer completion notifications
- [ ] Denying permission: timer still works but no notification

### Test: Alarm Permission
- [ ] App can schedule exact alarms (required for timer)
- [ ] No crashes related to alarm scheduling
- [ ] Timer completes at the correct time

**Notes:** ___________________________________________________

---

## ✅ 12. Stability and Performance

### Test: Memory Leaks
- [ ] Run conversions 10 times rapidly
- [ ] Start and stop timer 10 times
- [ ] Navigate back and forth 10 times
- [ ] App remains responsive
- [ ] No crashes after repeated use
- [ ] Check logcat for any warnings or errors

### Test: Long-Running
- [ ] Leave app open for 5 minutes
- [ ] Use app normally during this time
- [ ] App remains stable
- [ ] Timer continues to work
- [ ] No background crashes

### Test: Background/Foreground
- [ ] Start a 5-minute timer
- [ ] Press home button (app goes to background)
- [ ] Wait 1 minute
- [ ] Return to app
- [ ] Timer is still running and accurate
- [ ] When timer completes in background, notification appears

**Notes:** ___________________________________________________

---

## ✅ 13. Edge Cases

### Test: Minimum Values
- [ ] Set temperature to minimum (120°C)
- [ ] Set time to minimum (1 minute)
- [ ] Convert - no crash
- [ ] Results are displayed correctly

### Test: Maximum Values
- [ ] Set temperature to maximum (250°C)
- [ ] Set time to maximum (180 minutes)
- [ ] Convert - no crash
- [ ] Results are displayed correctly

### Test: Rapid Input Changes
- [ ] Rapidly tap plus/minus buttons
- [ ] App remains responsive
- [ ] Values update correctly
- [ ] No crashes or freezes

**Notes:** ___________________________________________________

---

## ✅ 14. ProGuard/R8 Specific Checks

### Test: Hilt Dependency Injection
- [ ] App launches (confirms Hilt injection works)
- [ ] ViewModel is accessible (no injection errors)
- [ ] All features work (confirms all dependencies injected correctly)

### Test: Data Classes
- [ ] Conversion results display correctly (data classes not broken)
- [ ] Navigation passes data correctly
- [ ] Timer state persists correctly

### Test: Reflection (if any)
- [ ] No crashes related to missing classes
- [ ] All features that might use reflection work

**Notes:** ___________________________________________________

---

## ✅ Final Verification

- [ ] **No crashes observed during entire test session**
- [ ] **All core features work as expected**
- [ ] **App is ready for Play Store submission**

---

## Issue Tracking

If any test fails, document here:

| Test # | Issue Description | Severity (High/Medium/Low) | Logcat Error |
|--------|-------------------|----------------------------|--------------|
| | | | |
| | | | |
| | | | |

---

## Sign-Off

**Tester Signature:** _________________________
**Date:** _________________________
**Status:** ☐ PASS - Ready for submission  |  ☐ FAIL - Issues found

---

## Next Steps After Passing

1. ✅ Build final signed release APK with production keystore
2. ✅ Generate AAB (Android App Bundle) for Play Store:
   ```bash
   ./gradlew bundleRelease
   ```
3. ✅ Fill out Play Console data safety form (see DATA_SAFETY_DECLARATION.md)
4. ✅ Upload to Google Play Console
5. ✅ Submit for review

---

## Notes

- Test on multiple devices if possible (different Android versions)
- Test on both emulator and physical device
- Keep detailed notes of any issues found
- Check logcat throughout testing for warnings
- If any HIGH severity issue is found, DO NOT submit until fixed

Good luck with your submission! 🚀
