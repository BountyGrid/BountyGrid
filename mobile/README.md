# BountyGrid Mobile

This is the cross-platform Flutter client for BountyGrid. It is intended to be
the app submitted to both the Apple App Store and Google Play Store.

## First-time setup

Install Flutter, Xcode, and Android Studio. Then run:

```bash
cd mobile
flutter create .
flutter pub get
flutter run
```

The app defaults to `http://10.0.2.2:8080/api` for Android emulator access to
the local Spring Boot backend. For iOS Simulator, use `http://localhost:8080/api`.
For real devices and store builds, use a public HTTPS API URL.

## Store readiness

Before App Store or Play Store release, configure:

- Public HTTPS backend URL
- App icon and launch screen
- iOS bundle ID and Apple signing team
- Android application ID and signing key
- Privacy strings for location, photos, and camera if those features are enabled
- TestFlight and Play internal testing tracks
