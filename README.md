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
