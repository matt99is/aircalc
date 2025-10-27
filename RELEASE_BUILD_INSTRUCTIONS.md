# AirCalc - Release Build Testing Instructions

This guide walks you through building, installing, and testing the **release APK** with ProGuard/R8 obfuscation enabled.

## Why Test the Release Build?

ProGuard/R8 can break your app at runtime even if debug builds work perfectly. Common issues:
- Hilt dependency injection failures
- Data class serialization breaking
- Reflection-based code failing
- Crashes that only appear in release builds

**You MUST test the release build before submitting to Play Store.**

---

## Prerequisites

- Android device or emulator connected
- USB debugging enabled (for physical devices)
- Signing keystore configured (or willing to use unsigned APK for testing)

---

## Option 1: Build Signed Release APK (Recommended)

### Step 1: Check if Signing is Configured

```bash
# Check if you have signing properties set
grep -q "RELEASE_STORE_FILE" gradle.properties && echo "✓ Signing configured" || echo "✗ Not configured"
```

### Step 2a: If Signing IS Configured

Build the signed release APK:

```bash
./gradlew assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

### Step 2b: If Signing is NOT Configured

You have two options:

**Option A: Build unsigned for testing only**
```bash
./gradlew assembleRelease
```
You'll get an unsigned APK that you can still install for testing.

**Option B: Generate a test keystore** (use only for testing, not production)
```bash
keytool -genkey -v -keystore test-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 365 \
  -alias aircalc-test \
  -storepass test1234 -keypass test1234
```

Then add to `gradle.properties`:
```properties
RELEASE_STORE_FILE=test-keystore.jks
RELEASE_STORE_PASSWORD=test1234
RELEASE_KEY_ALIAS=aircalc-test
RELEASE_KEY_PASSWORD=test1234
```

And rebuild:
```bash
./gradlew assembleRelease
```

---

## Step 3: Install the Release APK

### Uninstall any existing version first (important!)

```bash
adb uninstall com.aircalc.converter
```

### Install the release APK

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

You should see:
```
Success
```

---

## Step 4: Launch and Test

### Launch the app

```bash
adb shell am start -n com.aircalc.converter/.MainActivity
```

Or manually tap the app icon on your device.

### Monitor for crashes

In a separate terminal, monitor logs:

```bash
adb logcat | grep -E "AndroidRuntime|AirCalc|FATAL"
```

Keep this running while you test. Any crashes will appear here.

---

## Step 5: Complete the Testing Checklist

Follow the **RELEASE_TESTING_CHECKLIST.md** file to verify all functionality works.

---

## Option 2: Quick Test Without Signing (Fastest)

If you just want to verify the app works with ProGuard:

```bash
# Clean previous builds
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Uninstall old version
adb uninstall com.aircalc.converter

# Install release build
adb install app/build/outputs/apk/release/app-release.apk

# Launch app
adb shell am start -n com.aircalc.converter/.MainActivity
```

---

## Troubleshooting

### "App not installed" error
- Uninstall the existing version completely
- Check if the APK is corrupted: `aapt dump badging app/build/outputs/apk/release/app-release.apk`

### App crashes immediately on launch
- Check logcat for the error: `adb logcat | grep FATAL`
- Common causes:
  - Missing ProGuard keep rules
  - Hilt injection failures
  - Missing AndroidManifest declarations

### "INSTALL_FAILED_UPDATE_INCOMPATIBLE"
```bash
# Force uninstall
adb uninstall com.aircalc.converter
# Try install again
adb install app/build/outputs/apk/release/app-release.apk
```

### Build fails with signing errors
- Use Option 2b above to create a test keystore
- Or build without signing for testing only

### ProGuard warnings during build
- Review the warnings in the build output
- Add necessary keep rules to `app/proguard-rules.pro`
- Rebuild

---

## Verify ProGuard is Working

To confirm ProGuard/R8 is actually obfuscating your code:

```bash
# Check the APK size (should be smaller than debug)
ls -lh app/build/outputs/apk/release/app-release.apk
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Inspect obfuscated classes (optional)
unzip -l app/build/outputs/apk/release/app-release.apk | grep "classes.dex"
```

Release APK should be **significantly smaller** than debug APK.

---

## Build Time Estimate

With the optimizations in `gradle.properties`:
- **First build:** 2-3 minutes
- **Incremental builds:** 30-60 seconds

---

## Next Steps

After successfully installing the release APK:

1. ✅ Complete the **RELEASE_TESTING_CHECKLIST.md**
2. ✅ Test all core functionality
3. ✅ Leave the app running for 5-10 minutes to catch delayed crashes
4. ✅ Test with airplane mode to verify offline functionality
5. ✅ Test dark/light theme switching

If everything passes, you're ready to submit to Google Play Store!

---

## Important Notes

- **Always test release builds** before submission
- Release builds behave differently than debug builds
- ProGuard can break features that work in debug
- Test on a real device if possible (not just emulator)
- Test on Android 12+ to verify alarm permissions work

---

## Quick Reference Commands

```bash
# Full release build and test workflow
./gradlew clean assembleRelease
adb uninstall com.aircalc.converter
adb install app/build/outputs/apk/release/app-release.apk
adb shell am start -n com.aircalc.converter/.MainActivity

# Monitor for crashes
adb logcat | grep -E "AndroidRuntime|FATAL"

# Check APK info
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep version
```
