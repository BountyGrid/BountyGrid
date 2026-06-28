# BountyGrid Mobile Store Release Checklist

Use the Flutter app in `mobile/` for both the Apple App Store and Google Play Store.

## Local Testing

1. Install Flutter, Xcode, Android Studio, and Java 17.
2. Start the backend:

```bash
mvn spring-boot:run
```

3. Generate Flutter platform folders:

```bash
cd mobile
flutter create .
flutter pub get
```

4. Run Android emulator:

```bash
./scripts/run_android_local.sh
```

5. Run iOS Simulator:

```bash
./scripts/run_ios_local.sh
```

## Production API

Store builds must use HTTPS. Copy the example config and replace the URL:

```bash
cd mobile
cp config/production.example.json config/production.json
```

Set `API_BASE_URL` to your deployed backend, for example:

```json
{
  "APP_ENV": "production",
  "API_BASE_URL": "https://api.bountygrid.com/api"
}
```

## iOS App Store

After `flutter create .`, open `mobile/ios/Runner.xcworkspace` in Xcode and configure:

- Bundle identifier, for example `com.bountygrid.app`
- Apple Developer Team
- App display name: `BountyGrid`
- App icon
- Launch screen
- Camera/photo/location permission text if those features are enabled
- Signing and capabilities

Build:

```bash
cd mobile
./scripts/build_ios_release.sh
```

Upload the IPA with Xcode Organizer or Transporter, then test in TestFlight.

## Google Play Store

After `flutter create .`, configure:

- Android application ID, for example `com.bountygrid.app`
- App icon
- Version code and version name
- Upload signing key
- Store listing text, screenshots, privacy policy URL

Build:

```bash
cd mobile
./scripts/build_android_release.sh
```

Upload the generated AAB to Play Console internal testing first.

## Backend Requirements

- Public HTTPS URL
- PostgreSQL production database
- Strong `BOUNTYGRID_JWT_SECRET`
- Restricted CORS origins for browser/web usage
- File upload storage outside the application directory
- Privacy policy and terms URLs
