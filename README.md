# PeloDeck

PeloDeck is an Android cycling dashboard and launcher for compatible Peloton tablets. It shows bike metrics alongside an embedded browser and includes Whiskers & the Ball by Reverie Factory.

## Requirements

- Android 11 or later, a compatible tablet and bike, and a working Peloton sensor service.
- Android Studio, Android SDK 34, JDK 17, and an installed, supported system WebView.
- Network access for websites and online classes.

Hardware support varies by device and firmware. Speed, distance and calories are estimates. Verify measurements against your bike before relying on them.

## Download

[Download the PeloDeck test APK](https://github.com/robotscroll/pelodeck/releases/download/v0.1.0-test/PeloDeck-public-debug.apk) or see the [test release](https://github.com/robotscroll/pelodeck/releases/tag/v0.1.0-test).

This is an early **debug test build**. It has passed build and unit tests but has not completed device or security testing. Install it over an existing compatible build to preserve app data. Updates require the same application ID and signing key. Browser sign-ins are shared across rider profiles.

## Build and install

Open the project in Android Studio, configure the local Android SDK, and build the debug variant using `gradlew assembleDebug`. The APK appears under `app/build/outputs/apk/debug/`. Install updates over the existing app to retain its data. Release builds require your own signing configuration and security review.

Select PeloDeck as the system Home app if you want it to act as the launcher. System Settings remains accessible from the launcher. Profiles, themes and bookmarks can be managed in the app. Class Select opens YouTube searches in the browser. Fullscreen video displays a compact ride metrics ribbon.

## Privacy and security

Browser sign-ins, cookies and website storage are **shared across all rider profiles** on the same installation. Switching riders does not sign out the previous rider. Anyone using the app may access active browser sessions. Websites manage their own session duration.

The browser permits HTTPS addresses only and blocks mixed content and external application links. Some websites and login flows may not work within an embedded WebView. Keep Android and the system WebView updated. Turn off wireless debugging when it is not in use. Network isolation can reduce exposure on shared networks.

## Game

Whiskers & the Ball is included with permission from Reverie Factory. Its local leaderboard is stored on the device. Visit https://www.reveriefactory.com.

## Third-party notices

The hardware service integration incorporates material from OpenRide. See OPENRIDE-LICENSE and the bundled notices. Review all third-party dependencies and assets before distributing a release.

## Limitations

PeloDeck is an independent project and is not affiliated with Peloton. Hardware and video playback depend on device firmware and website behavior. Bike firmware and system WebView security updates are provided by the device or WebView vendor. The project requires further device and security testing before public distribution.
