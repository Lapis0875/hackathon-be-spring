# Auth API contract (hackathon boilerplates)

Shared by `hackathon-be-spring` (server) and `hackathon-fe-vite` (client). Both sides implement exactly this. Changes require the orchestrator.

## Ports / origins
- BE: `http://localhost:8080`. All routes under `/api`.
- FE dev: `http://localhost:5173`. Vite proxies `/api` → `http://localhost:8080` (so FE calls relative `/api/...`).
- BE CORS: allow origin `http://localhost:5173`, methods GET/POST/PUT/PATCH/DELETE/OPTIONS, header `Authorization`, `Content-Type`.
- OpenAPI: `GET /v3/api-docs` (JSON), Swagger UI at `/swagger-ui.html`. Both public (no auth).

## Auth
- Bearer JWT, HS256, secret from env `JWT_SECRET` (dev default in `application.yml`, ≥32 bytes), expiry 24h (`JWT_EXPIRES_IN_SECONDS`, default 86400). Claims: `sub` = user id (string), `email`, `iat`, `exp`.
- No refresh token. Client stores access token in `localStorage["accessToken"]`; on any 401 the client clears it and redirects to `/login`.
- Public routes: `POST /api/auth/signup`, `POST /api/auth/login`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/actuator/health`. Everything else requires Bearer.

## Endpoints

### POST /api/auth/signup
Request `application/json`:
```json
{ "email": "a@b.com", "password": "secret123" }
```
Validation: email must be valid email; password 8–72 chars.
- 201 → `{ "id": 1, "email": "a@b.com" }`
- 400 validation error (ProblemDetail)
- 409 email already registered (ProblemDetail, `title: "Email already registered"`)

### POST /api/auth/login
Request:
```json
{ "email": "a@b.com", "password": "secret123" }
```
- 200 → `{ "accessToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 86400 }`
- 401 bad credentials (ProblemDetail). No `WWW-Authenticate: Basic` header (avoid browser basic-auth popup).

### GET /api/me
Header `Authorization: Bearer <jwt>`.
- 200 → `{ "id": 1, "email": "a@b.com" }`
- 401 missing/invalid/expired token (ProblemDetail)

## Error body (all 4xx/5xx)
RFC 9457 `application/problem+json`:
```json
{ "type": "about:blank", "title": "Email already registered", "status": 409, "detail": "a@b.com is already registered", "instance": "/api/auth/signup" }
```
Validation 400 additionally includes `"errors": [{ "field": "password", "message": "size must be between 8 and 72" }]`.

## Data model
`User`: `id` (Long, identity), `email` (unique, not null), `passwordHash` (not null, BCrypt). Nothing else.

## Types the FE generates (from /v3/api-docs; schema names must match)
- `SignupRequest { email: string; password: string }`
- `LoginRequest { email: string; password: string }`
- `TokenResponse { accessToken: string; tokenType: "Bearer"; expiresIn: number }`
- `UserResponse { id: number; email: string }`
- `ProblemDetail { type?: string; title?: string; status?: number; detail?: string; instance?: string; errors?: { field: string; message: string }[] }`
