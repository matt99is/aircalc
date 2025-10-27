# AirCalc - Data Safety Declaration for Google Play Console

This document contains the exact information you need to fill out the **Data Safety** form in Google Play Console.

Copy and paste this information directly into the Play Console form.

---

## Overview

**Does your app collect or share any user data?**
- ✅ **NO** - AirCalc does not collect or share any user data

---

## Data Collection

### Personal Information
- **Email addresses:** NO
- **User names:** NO
- **Phone numbers:** NO
- **Addresses:** NO
- **Other personal info:** NO

### Financial Information
- **Payment info:** NO
- **Purchase history:** NO
- **Credit info:** NO
- **Other financial info:** NO

### Location
- **Approximate location:** NO
- **Precise location:** NO

### Health and Fitness
- **Health info:** NO
- **Fitness info:** NO

### Messages
- **Emails:** NO
- **SMS or MMS:** NO
- **Other in-app messages:** NO

### Photos and Videos
- **Photos:** NO
- **Videos:** NO

### Audio Files
- **Voice or sound recordings:** NO
- **Music files:** NO
- **Other audio files:** NO

### Files and Docs
- **Files and docs:** NO

### Calendar
- **Calendar events:** NO

### Contacts
- **Contacts:** NO

### App Activity
- **App interactions:** NO
- **In-app search history:** NO
- **Installed apps:** NO
- **Other user-generated content:** NO
- **Other actions:** NO

### Web Browsing
- **Web browsing history:** NO

### App Info and Performance
- **Crash logs:** NO
- **Diagnostics:** NO
- **Other app performance data:** NO

### Device or Other IDs
- **Device or other IDs:** NO

---

## Data Sharing

**Does your app share any data with third parties?**
- ✅ **NO** - AirCalc does not share any data with third parties

---

## Data Storage

**Where is user data stored?**
- ✅ **On device only** - All data (timer state, conversion history) is stored locally using Android DataStore
- ❌ **Not on servers** - No data is transmitted to external servers
- ❌ **Not shared with third parties**

---

## Security Practices

### Data Encryption
**Is data encrypted in transit?**
- ✅ **N/A** - No data is transmitted (fully offline app)

**Is data encrypted at rest?**
- ✅ **YES** - Android DataStore uses encrypted storage provided by the Android operating system

### Deletion
**Can users request data deletion?**
- ✅ **N/A** - All data is stored locally on the user's device
- Users can delete all app data by:
  1. Uninstalling the app, OR
  2. Clearing app data in Android Settings → Apps → AirCalc → Storage → Clear Data

---

## Permissions Explanation

### VIBRATE
**Purpose:** Provide haptic feedback when the cooking timer completes
**User Benefit:** Tactile alert when food is ready

### POST_NOTIFICATIONS
**Purpose:** Display a notification when the cooking timer completes
**User Benefit:** Get alerted when food is ready, even if app is in background

### SCHEDULE_EXACT_ALARM
**Purpose:** Schedule precise alarm for cooking timer completion
**User Benefit:** Accurate timer alerts at the exact completion time

### USE_EXACT_ALARM
**Purpose:** Schedule exact alarms on Android 12+ devices
**User Benefit:** Ensures timer works correctly on newer Android versions

---

## Privacy Policy

**Does your app have a privacy policy?**
- ✅ **YES** - Privacy policy is available at: **[INSERT YOUR PRIVACY POLICY URL HERE]**

**Note:** You MUST have a privacy policy URL. If you don't have one yet, see the "Privacy Policy Template" section below.

---

## Privacy Policy Template

If you don't have a privacy policy yet, here's a simple template you can use:

```
AirCalc Privacy Policy

Last Updated: [Current Date]

AirCalc ("the app") is committed to protecting your privacy.

DATA COLLECTION
AirCalc does NOT collect, store, or transmit any personal information. The app operates entirely offline.

LOCAL DATA STORAGE
The app stores the following data locally on your device only:
- Timer state (current timer duration and status)
- Conversion history (optional feature, stored locally)

This data never leaves your device and is not accessible to us or any third parties.

THIRD-PARTY SERVICES
AirCalc does not use any third-party analytics, advertising, or tracking services.

PERMISSIONS
The app requests the following permissions:
- VIBRATE: For haptic feedback when timer completes
- POST_NOTIFICATIONS: To show timer completion alerts
- SCHEDULE_EXACT_ALARM: For precise timer functionality
- USE_EXACT_ALARM: For Android 12+ compatibility

DATA DELETION
You can delete all app data at any time by:
1. Uninstalling the app, OR
2. Going to Android Settings → Apps → AirCalc → Storage → Clear Data

CHANGES TO THIS POLICY
We may update this privacy policy from time to time. Any changes will be posted at this URL.

CONTACT
For questions about this privacy policy, contact: [YOUR EMAIL]

```

**Where to host your privacy policy:**
1. **GitHub Pages** (Free) - Create a repository with an index.html file
2. **Google Sites** (Free) - Create a simple one-page site
3. **Your own website** - If you have one
4. **Gist** - Use a public GitHub Gist

---

## Play Console Data Safety Form - Step-by-Step

### Step 1: Data Collection and Security
**Does your app collect or share any of the required user data types?**
- Select: **No, my app doesn't collect or share any of the required user data types**

### Step 2: Data Usage
*This section will be skipped since you selected "No" above*

### Step 3: Data Types
*This section will be skipped since you selected "No" above*

### Step 4: Data Security
*This section will be skipped since you selected "No" above*

### Step 5: Privacy Policy
**Privacy policy URL:** [YOUR PRIVACY POLICY URL]

### Step 6: Review and Submit
Review your answers and click **Submit**

---

## Important Notes

1. **Be Honest:** Google can audit your app. If you claim not to collect data but actually do, your app can be removed.

2. **Future Updates:** If you add analytics, crash reporting, or any data collection in the future, you MUST update this form.

3. **Privacy Policy is Required:** Even though you don't collect data, Google Play requires a privacy policy URL.

4. **Local Storage Doesn't Count:** Data stored locally on the device (like DataStore for timer state) does NOT need to be declared as "collected data" as long as it never leaves the device.

5. **Permissions:** The permissions you request (vibrate, notifications, alarms) do NOT need to be declared in data safety as long as they don't involve data collection or sharing.

---

## Quick Copy-Paste Summary

**For the Play Console form, copy this:**

```
Data Collection: NO
Data Sharing: NO
Privacy Policy: [YOUR URL]

Permissions Justification:
- VIBRATE: Haptic feedback for timer completion
- POST_NOTIFICATIONS: Alert user when timer completes
- SCHEDULE_EXACT_ALARM: Precise timer functionality
- USE_EXACT_ALARM: Android 12+ timer support

Data Storage: All data stored locally on device using Android DataStore. No data transmitted to servers or third parties.
```

---

## Verification Checklist

Before submitting to Play Store:

- [ ] Confirmed app truly does NOT collect any personal data
- [ ] Confirmed app does NOT send any data to external servers
- [ ] Confirmed app does NOT use analytics (Firebase, Google Analytics, etc.)
- [ ] Confirmed app does NOT use crash reporting (Firebase Crashlytics, Sentry, etc.)
- [ ] Confirmed app does NOT use advertising networks
- [ ] Privacy policy URL is live and accessible
- [ ] Privacy policy accurately describes the app's data practices
- [ ] Data safety form filled out correctly in Play Console

---

## Questions?

**Q: Do I need to declare timer state stored in DataStore?**
A: No. Data stored locally on device that never leaves the device does not need to be declared.

**Q: What if I add Firebase Crashlytics later?**
A: You MUST update the data safety form to declare that you collect "Crash logs" and "Diagnostics".

**Q: Can I skip the privacy policy?**
A: No. Google Play requires all apps to have a privacy policy URL, even if you don't collect data.

**Q: What if Google rejects my declaration?**
A: Google may audit your app. Make sure your code matches your declaration. If rejected, they'll tell you why.

---

## Next Steps

1. ✅ Create and host your privacy policy
2. ✅ Fill out the Data Safety form in Play Console
3. ✅ Submit your app for review
4. ✅ Monitor for any feedback from Google

Good luck with your submission! 🎉
