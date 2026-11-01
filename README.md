# hackathon-be-spring

**English** | [한국어](README.ko.md)

Spring Boot REST API boilerplate for hackathons. Signup, login, and "who am I" work the moment you clone it, so the team starts on features, not on auth.

Companion frontend: [hackathon-fe-vite](https://github.com/Lapis0875/hackathon-fe-vite). The two share one API contract: [`docs/auth-contract.md`](docs/auth-contract.md).

## What's included

| Area | Choice |
| --- | --- |
| Runtime | Spring Boot 4.1, Java 21, Gradle (Kotlin DSL), package `com.hackathon.api` |
| Auth | Spring Security (stateless), JWT HS256 via jjwt, BCrypt through `PasswordEncoder` |
| Data | Spring Data JPA; H2 in-memory by default, `postgres` profile for PostgreSQL |
| API docs | springdoc OpenAPI: `/v3/api-docs`, `/swagger-ui.html` (the frontend generates its TypeScript types from this) |
| Errors | RFC 9457 `application/problem+json` for every 4xx/5xx; validation 400 adds `errors[{field,message}]` |
| Tests | One `@SpringBootTest` + MockMvc flow test covering the whole auth contract |

Deliberately left out: refresh tokens, roles, email verification, Docker, CI, Lombok.

## Run

Requires Java 21. Gradle comes with the wrapper.

```bash
./gradlew bootRun   # http://localhost:8080 (H2 in-memory; data resets on restart)
./gradlew test      # src/test/java/com/hackathon/api/AuthFlowTest.java
```

Quick check:

```bash
curl -s -X POST localhost:8080/api/auth/signup -H 'Content-Type: application/json' -d '{"email":"a@b.com","password":"secret123"}'
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"email":"a@b.com","password":"secret123"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
curl -s localhost:8080/api/me -H "Authorization: Bearer $TOKEN"
```

With PostgreSQL:

```bash
cp .env.example .env          # edit values
set -a; source .env; set +a
SPRING_PROFILES_ACTIVE=postgres ./gradlew bootRun
```

## Endpoints

| Method | Path | Auth | Responses |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | public | 201 `{id,email}` · 400 · 409 |
| POST | `/api/auth/login` | public | 200 `{accessToken,tokenType,expiresIn}` · 401 |
| GET | `/api/me` | Bearer | 200 `{id,email}` · 401 |
| GET | `/v3/api-docs`, `/swagger-ui.html`, `/actuator/health` | public | |

Every other path requires `Authorization: Bearer <jwt>`. CORS allows `http://localhost:5173` (the Vite dev server).

## Environment variables

| Variable | Default | Notes |
| --- | --- | --- |
| `JWT_SECRET` | DEV ONLY value in `application.yml` | ≥ 32 bytes. Replace before any deployment |
| `JWT_EXPIRES_IN_SECONDS` | `86400` | Token lifetime |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | see `.env.example` | `postgres` profile only |

## Project layout

```
src/main/java/com/hackathon/api/
├── ApiApplication.java
├── auth/      AuthController, AuthService, SignupRequest, LoginRequest, TokenResponse, UserResponse
├── user/      User (entity: id, email, passwordHash), UserRepository, MeController
├── security/  SecurityConfig, JwtProvider, JwtAuthFilter, AuthenticatedUser
└── common/    GlobalExceptionHandler, EmailAlreadyRegisteredException, OpenApiConfig, ProblemDetailSchema
src/main/resources/application.yml   default (H2) and postgres profiles
src/test/java/com/hackathon/api/AuthFlowTest.java
docs/auth-contract.md                shared FE/BE contract
```

## Extending

Add a domain (example: `post`):

1. `post/Post.java` — `@Entity`; copy the shape of `user/User.java`.
2. `post/PostRepository.java` — `extends JpaRepository<Post, Long>`.
3. `post/PostController.java` — `@RestController @RequestMapping("/api/posts")`; request/response types as records named `…Request` / `…Response` (those names become the frontend's generated types).
   The current user arrives as `@AuthenticationPrincipal AuthenticatedUser me` (see `user/MeController.java`).
4. Public route? Add it to `PUBLIC_PATHS` in `security/SecurityConfig.java`.
5. New error? Add an exception under `common/` and one `@ExceptionHandler` in `GlobalExceptionHandler`.
6. Tables are created by Hibernate (`ddl-auto`) on both H2 and Postgres; no migrations needed during a hackathon.

Rename the package: find-and-replace `com.hackathon.api`, then move `src/main/java/com/hackathon/api`.

## License

MIT
