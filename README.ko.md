# hackathon-be-spring

[English](README.md) | **한국어**

해커톤용 Spring Boot REST API 보일러플레이트. 클론 직후 회원가입·로그인·내 정보 조회가 동작하므로 팀은 인증이 아니라 기능부터 만들 수 있습니다.

짝이 되는 프론트엔드: [hackathon-fe-vite](https://github.com/Lapis0875/hackathon-fe-vite). 두 저장소는 하나의 API 계약 [`docs/auth-contract.md`](docs/auth-contract.md)를 공유합니다.

## 포함된 것

| 영역 | 선택 |
| --- | --- |
| 런타임 | Spring Boot 4.1, Java 21, Gradle(Kotlin DSL), 패키지 `com.hackathon.api` |
| 인증 | Spring Security(stateless), jjwt 기반 JWT HS256, `PasswordEncoder`를 통한 BCrypt |
| 데이터 | Spring Data JPA. 기본은 H2 인메모리, `postgres` 프로파일로 PostgreSQL |
| API 문서 | springdoc OpenAPI: `/v3/api-docs`, `/swagger-ui.html` (프론트엔드가 여기서 TypeScript 타입을 생성) |
| 에러 | 모든 4xx/5xx는 RFC 9457 `application/problem+json`. 검증 400은 `errors[{field,message}]` 추가 |
| 테스트 | 인증 계약 전체를 도는 `@SpringBootTest` + MockMvc 플로우 테스트 1개 |

의도적으로 뺀 것: 리프레시 토큰, 권한(roles), 이메일 인증, Docker, CI, Lombok.

## 실행

Java 21 필요. Gradle은 래퍼로 포함.

```bash
./gradlew bootRun   # http://localhost:8080 (H2 인메모리, 재시작 시 데이터 초기화)
./gradlew test      # src/test/java/com/hackathon/api/AuthFlowTest.java
```

빠른 확인:

```bash
curl -s -X POST localhost:8080/api/auth/signup -H 'Content-Type: application/json' -d '{"email":"a@b.com","password":"secret123"}'
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"email":"a@b.com","password":"secret123"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
curl -s localhost:8080/api/me -H "Authorization: Bearer $TOKEN"
```

PostgreSQL 사용:

```bash
cp .env.example .env          # 값 수정
set -a; source .env; set +a
SPRING_PROFILES_ACTIVE=postgres ./gradlew bootRun
```

## 엔드포인트

| Method | Path | 인증 | 응답 |
| --- | --- | --- | --- |
| POST | `/api/auth/signup` | 공개 | 201 `{id,email}` · 400 · 409 |
| POST | `/api/auth/login` | 공개 | 200 `{accessToken,tokenType,expiresIn}` · 401 |
| GET | `/api/me` | Bearer | 200 `{id,email}` · 401 |
| GET | `/v3/api-docs`, `/swagger-ui.html`, `/actuator/health` | 공개 | |

그 외 모든 경로는 `Authorization: Bearer <jwt>` 필수. CORS는 `http://localhost:5173`(Vite 개발 서버) 허용.

## 환경 변수

| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `JWT_SECRET` | `application.yml`의 DEV ONLY 값 | 32바이트 이상. 배포 전 반드시 교체 |
| `JWT_EXPIRES_IN_SECONDS` | `86400` | 토큰 만료(초) |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | `.env.example` 참고 | `postgres` 프로파일에서만 사용 |

## 프로젝트 구성

```
src/main/java/com/hackathon/api/
├── ApiApplication.java
├── auth/      AuthController, AuthService, SignupRequest, LoginRequest, TokenResponse, UserResponse
├── user/      User(엔티티: id, email, passwordHash), UserRepository, MeController
├── security/  SecurityConfig, JwtProvider, JwtAuthFilter, AuthenticatedUser
└── common/    GlobalExceptionHandler, EmailAlreadyRegisteredException, OpenApiConfig, ProblemDetailSchema
src/main/resources/application.yml   default(H2)·postgres 프로파일
src/test/java/com/hackathon/api/AuthFlowTest.java
docs/auth-contract.md                FE/BE 공유 계약
```

## 참조 팩

`docs/refs/`에 Context7에서 받아 둔 공식 문서 스냅샷이 있습니다. 구성은 다음과 같습니다.

- **핵심 스택**: Spring Boot 4.1, Spring Framework 7, Spring Security 7, Spring Data JPA, Hibernate 7, springdoc, jjwt, HikariCP, JUnit, Gradle
- **관계형 DB**: PostgreSQL(타입, 인덱스, upsert/COPY, pgjdbc), MySQL, MariaDB, SQL Server, Oracle, H2, Spring Data JDBC, Flyway, Liquibase, jOOQ, MyBatis, Querydsl, Testcontainers
- **파일·이미지 데이터**: multipart 업로드, 스트리밍 다운로드, AWS S3 SDK v2(presign, Transfer Manager), Spring Cloud AWS, MinIO, Apache Tika, Thumbnailator, TwelveMonkeys ImageIO, Hibernate LOB
- **CSV·스프레드시트**: Commons CSV, OpenCSV, FastCSV, Spring Batch CSV 적재, Apache POI

[`docs/refs/INDEX.md`](docs/refs/INDEX.md)에서 시작하세요. 주제별 문서 버전, 프로젝트 버전(또는 "아직 의존성 아님"), 고정된 Context7 ID가 정리되어 있습니다. "아직 의존성 아님" 항목은 기능에 필요할 때 추가할 수 있는 선택지입니다.

갱신·추가: `docs/refs/manifest.json`을 수정한 뒤 `node scripts/fetch-refs.mjs` 실행(`--force` 재수집, `--only <text>` 필터). Node 22.13 이상과 `npx ctx7 login`(또는 `CONTEXT7_API_KEY`)이 필요하고, 항목 하나당 Context7 API 호출 1회를 씁니다. 앱 실행에는 Node가 필요 없습니다.

## 확장하기

새 도메인 추가(예: `post`):

1. `post/Post.java` — `@Entity`. `user/User.java` 형태를 따라 작성.
2. `post/PostRepository.java` — `extends JpaRepository<Post, Long>`.
3. `post/PostController.java` — `@RestController @RequestMapping("/api/posts")`. 요청/응답은 `…Request` / `…Response`로 끝나는 record로 (이 이름이 프론트엔드 생성 타입 이름이 됩니다).
   로그인 사용자는 `@AuthenticationPrincipal AuthenticatedUser me` 파라미터로 받습니다(`user/MeController.java` 참고).
4. 공개 경로가 필요하면 `security/SecurityConfig.java`의 `PUBLIC_PATHS`에 추가.
5. 새 에러는 `common/`에 예외 클래스 추가 후 `GlobalExceptionHandler`에 `@ExceptionHandler` 한 개 추가.
6. 테이블은 H2·Postgres 모두 Hibernate가 자동 생성(`ddl-auto`)하므로 해커톤 중 마이그레이션 불필요.

패키지 이름 변경: `com.hackathon.api`를 전체 찾아바꾸기 한 뒤 `src/main/java/com/hackathon/api` 디렉터리를 옮기면 됩니다.

## 라이선스

MIT
