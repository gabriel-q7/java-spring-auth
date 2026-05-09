# java-spring-auth

Spring Boot 3 (Java 21) modular monolith backend with:
- `auth` module (`/auth/register`, `/auth/login`, `/auth/refresh`)
- `users` module (`/users/me` GET and PUT)
- JWT stateless authentication
- SQLite + Spring Data JPA

## Run locally

1. Ensure Java 21 and Maven are installed.
2. (Optional) set JWT secret:
   - `export JWT_SECRET="your-very-long-secret-key-at-least-64-chars"`
3. Start app:
   - `mvn spring-boot:run`

> Note: refresh tokens are rotated per user, so issuing a new token pair invalidates previous refresh tokens for that user.

## Test

- `mvn test`
