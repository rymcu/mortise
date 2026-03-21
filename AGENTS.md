# AGENTS.md

## Build & Test (Java 21, Maven — never use mvnw)
- Build: `mvn clean package -DskipTests`
- Test all: `mvn test`
- Single module: `mvn -pl mortise-system -am clean test`
- Single test: `mvn -pl <module> -am test -Dtest=SomeTest`
- Frontend (pnpm only, run from `frontend/`): `pnpm install && pnpm dev:admin`

## Architecture
Multi-module Spring Boot 3.5 monolith (group `com.rymcu`). Entry point: `mortise-app`.
Layered modules follow DDD: `domain → application → infra → admin/api`. Admin/API must not depend on Domain/Infra directly.
Key infra: `mortise-common`, `mortise-core` (GlobalResult, base entities), `mortise-auth` (JWT+OAuth2+Spring Security), `mortise-web-support` (@AdminController/@ApiController, GlobalExceptionHandler), `mortise-persistence` (MyBatis-Flex, Flyway, PostgreSQL).
Extend via SPI interfaces (CacheConfigurer, NotificationSender, CustomUserDetailsService, etc.).
Frontend: Nuxt 4 + Nuxt UI 4.5 + TypeScript monorepo under `frontend/` (pnpm workspace).

## Code Style
- Lombok everywhere (`@Data`, `@Builder`, `@Getter/@Setter`). Entities: `@Table`/`@Id`/`@Column` (MyBatis-Flex).
- DI via `@Resource` or constructor injection. Mappers extend `BaseMapper<T>` with `@Mapper`.
- Controllers use `@AdminController`/`@ApiController` (custom meta-annotations), return `GlobalResult`.
- Exceptions: throw `BusinessException`/`ServiceException`, handled by `GlobalExceptionHandler`.
- IDs generated via ULID (`FlexId`). DB: PostgreSQL 17 preferred, Flyway migrations.
- Comments, commit messages, docs in **简体中文** by default. Commits follow Conventional Commits.
- Read `.github/copilot-instructions.md` and `docs/module-dependency-and-spi-architecture.md` before cross-module changes.
