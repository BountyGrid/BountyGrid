# BountyGrid

BountyGrid is a Spring Boot lost-and-found reward platform based on the developer documentation.

## Stack

- Java 17
- Spring Boot 3.2
- Thymeleaf
- Spring Security with JWT cookies
- Spring Data JPA
- H2 for development, PostgreSQL for production
- STOMP WebSocket messaging

## Run

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`.

## Android app

The native Android client lives in `android/`. It calls the Spring Boot API at
`http://10.0.2.2:8080/api` by default, which is the Android emulator address for
your host machine.

1. Start the backend with `mvn spring-boot:run`.
2. Open `android/` in Android Studio.
3. Run the `app` configuration on an emulator or device.

If you run on a physical phone, change the API base URL on the login screen to
your computer's LAN address, for example `http://192.168.1.20:8080/api`.

## App Store and Play Store app

The cross-platform Flutter client lives in `mobile/`. This is the recommended
app for publishing to both the Apple App Store and Google Play Store.

```bash
cd mobile
flutter create .
flutter pub get
flutter run
```

For store builds, point the app at a public HTTPS backend URL instead of a local
development URL.
