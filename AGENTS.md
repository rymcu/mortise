# AGENTS.md

> Authoritative quick-reference for AI coding agents operating in this repository.
> For deeper guidance see the files cross-referenced at the bottom.

## Build & Test (Java 21, Maven — never use mvnw)

| Task | Command |
|------|---------|
| Full build (skip tests) | `mvn clean package -DskipTests` |
| Full install | `mvn clean install` |
| Run all tests | `mvn test` |
| Single module compile | `mvn -pl mortise-system -am clean compile -DskipTests` |
| Single module tests | `mvn -pl mortise-system -am test` |
| Single test class | `mvn -pl <module> -am test -Dtest=SomeTest` |
| Cross-module wiring check | `mvn -pl mortise-app -am clean compile -DskipTests` |
| Run application | `mvn spring-boot:run` (from mortise-app) |

## Frontend (pnpm only — no npm/yarn)

All commands run from `frontend/`. Workspace pins `pnpm@10.29.3`.

| Task | Command |
|------|---------|
| Install deps | `pnpm install` |
| Dev admin app | `pnpm dev:admin` |
| Dev site app | `pnpm dev:site` |
| Build all | `pnpm build` |
| Lint all | `pnpm lint` |
| Typecheck all | `pnpm typecheck` |
| Typecheck packages | `pnpm typecheck:packages` |
| Lint single app | `pnpm --filter @mortise/admin lint` |
| Typecheck single app | `pnpm --filter @mortise/site typecheck` |

## Architecture

Multi-module Spring Boot 3.5 monolith (`com.rymcu`). Entry point: `mortise-app`.

```
Layer 6: mortise-app                      — assembly, no business logic
Layer 5: *-admin / *-api                  — controllers, request/response DTOs
Layer 4: *-application / *-infra          — services, mappers, persistence
Layer 3: mortise-auth, mortise-web-support, mortise-monitor  — app base (no same-layer deps)
Layer 2: mortise-log, mortise-cache, mortise-notification, mortise-persistence
Layer 1: mortise-common, mortise-core     — shared utilities, GlobalResult, SPI contracts
```

Dependency flows **downward only**. Same-layer modules must **never** depend on each other directly — use SPI interfaces, Spring events, or assembly-layer composition instead.

Frontend: Nuxt 4 + Nuxt UI 4.5 + TypeScript monorepo under `frontend/` (apps/admin, apps/site, packages/\*, layers/\*).

## Code Style — Java

- **Lombok everywhere**: `@Data` on entities/DTOs, `@Slf4j` for logging, `@RequiredArgsConstructor` for constructor DI (preferred), `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor` when builder needed.
- **DI**: Constructor injection via `@RequiredArgsConstructor` (preferred) or `@Resource` field injection (legacy). Use `@Autowired(required = false)` only for optional SPI dependencies.
- **ORM**: MyBatis-Flex — entities use `@Table(value = "mortise_xxx", schema = "mortise")`, `@Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)`, `@Column`. Mappers extend `BaseMapper<T>` with `@Mapper`.
- **IDs**: ULID-based via `FlexId` for all primary keys (`Long`). Business codes use `UlidCreator` (e.g. `PRD-{ULID}`).
- **Controllers**: Use `@AdminController` or `@ApiController` (custom meta-annotations, auto-prefixed `/api/v1/admin` and `/api/v1`). Always return `GlobalResult<T>`. Add `@Tag` (OpenAPI), `@PreAuthorize("hasAuthority('module:resource:action')")` per method, `@ApiLog`/`@OperationLog`.
- **Exceptions**: Throw `BusinessException` (400) or `ServiceException` (500). Both are unchecked. Handled globally by `GlobalExceptionHandler` in `mortise-web-support`.
- **Naming**: Google Java style — `UpperCamelCase` classes, `lowerCamelCase` methods/vars, `UPPER_SNAKE_CASE` constants, `lowercase` packages. Nouns for classes, verbs for methods.
- **Null handling**: Prefer `Optional<T>`, avoid returning/accepting `null`. Use `Objects.requireNonNull()`.
- **Services**: Interfaces extend `IService<T>`, implementations extend `ServiceImpl<Mapper, Entity>`. Keep stateless.
- **Logging**: `@Slf4j` (Lombok) with parameterized messages: `log.info("User {} logged in", userId)`. Never `System.out.println()`.
- **Validation**: JSR-380 annotations (`@NotNull`, `@Size`, `@Valid`) on request models.
- **Soft delete**: All entities have `@Column(isLogicDelete = true) private Integer delFlag`.

## Code Style — Frontend (TypeScript / Vue)

- **IDs are always `string`**, never `number` — including route params and API response fields.
- **No inline types in `.vue` SFC `<script setup>`** — extract to `types/*.ts` files, use `import type`.
- **Component size**: Max ~300 lines per `.vue` file. Extract modals, forms, sub-lists into child components.
- **Form inputs**: `UInput`/`USelect`/`UTextarea` inside `UFormField` must have `class="w-full"`.
- **USelect**: Never use `value: ''` in items (reserved). Use `placeholder` for "all/default" options.
- **Shared logic** goes in `packages/*`, not duplicated across apps. `@mortise/core-sdk` is the backend SDK surface.
- **Nuxt auto-imports**: Prefer auto-imported composables over manual imports.

## Permission Conventions

Format: `module:resource:action` (e.g. `system:user:list`, `community:article:audit`).
Actions: `list`, `query`, `add`, `edit`, `delete`, `assign`, `audit`, `export`, `import`, `upload`, `download`, `clear`.
Always method-level `@PreAuthorize("hasAuthority('...')")`, never class-level.

## Flyway Migration Rules

- **Global version sequence** across all modules — never restart from V1 per module.
- Run `./get-next-flyway-version.ps1` before adding a migration, or scan all `src/main/resources/db/migration/V*.sql` across the entire repo.
- Standard path: `mortise-xx-infra/src/main/resources/db/migration/`. Legacy: `mortise-xx/src/main/resources/db/migration/`. Both count.
- Naming: `V{version}__{description}.sql`. Resolve duplicates before creating new scripts.
- Run `./setup-git-hooks.ps1` once per clone to enable the pre-commit hook that blocks duplicate Flyway versions and PRs missing `规范链接:`.
- DB: PostgreSQL 17 preferred.

## SPI Extension Pattern

Cross-module integration uses SPI interfaces defined in the owning module:
- `SecurityConfigurer` (mortise-auth) — modules contribute security rules
- `CacheConfigurer` (mortise-cache) — modules register cache strategies
- `NotificationSender` (mortise-notification) — pluggable channels
- `CustomUserDetailsService` (mortise-auth) — multi-user-table login
- `LogStorage` (mortise-log), `JacksonConfigurer` (mortise-web-support)
- Domain ports: `DeviceProtocolGateway`, `ChatModelProvider`, `ActionExecutor`, etc.

SPI interfaces typically include `getOrder()` for priority and `isEnabled()` guard.

## Module Placement Quick Reference

| Content | Goes in |
|---------|---------|
| Entities, enums, domain concepts | `*-domain` |
| Service interfaces & implementations | `*-application` |
| Mappers, adapters, persistence | `*-infra` |
| Admin controllers & DTOs | `*-admin` |
| Public API controllers & DTOs | `*-api` |
| Cross-domain shared utilities | `mortise-common` or `mortise-core` |
| Runtime assembly & wiring | `mortise-app` |

## Terminal & Language

- Default terminal: **PowerShell 7 (pwsh)** — use PowerShell syntax, not bash.
- Comments, commit messages, docs: **简体中文** by default. Commits follow Conventional Commits.

## Verification Checklist

| Change scope | Minimum verification |
|---|---|
| Single backend module | `mvn -pl <module> -am clean compile -DskipTests` |
| Cross-module / aggregation | `mvn -pl mortise-app -am clean compile -DskipTests` |
| Test-sensitive change | `mvn -pl <module> -am test` |
| Admin frontend | `pnpm --filter @mortise/admin lint` |
| Site frontend | `pnpm --filter @mortise/site typecheck` |
| Shared package | `pnpm typecheck:packages` |
| Cross-app frontend | `pnpm lint && pnpm typecheck` (from `frontend/`) |

## Anti-Patterns

- Do not add same-layer module dependencies — use SPI or events.
- Do not put business logic in `mortise-common`, `mortise-core`, or `mortise-app`.
- Do not use `number` IDs in frontend code.
- Do not define types inline in `.vue` files.
- Do not use `mvnw`/`mvnw.cmd` — always `mvn`.
- Do not use npm/yarn — always `pnpm`.
- Do not assume optional layers (community, commerce) are present.
- Do not infer Flyway version from a single module — check globally.

## Cross-References

| Topic | File |
|---|---|
| Master agent entry point | `.github/copilot-instructions.md` |
| Module boundaries & SPI | `docs/module-dependency-and-spi-architecture.md` |
| Java best practices | `.github/instructions/java.instructions.md` |
| Spring Boot conventions | `.github/instructions/springboot.instructions.md` |
| Backend boundaries | `.github/instructions/backend-module-boundaries.instructions.md` |
| Permission naming | `.github/instructions/permission-conventions.instructions.md` |
| Frontend patterns | `.github/instructions/frontend-nuxt-patterns.instructions.md` |
| Env troubleshooting | `.github/prompts/mortise-env-triage.prompt.md` |
| TDD workflow | `.github/prompts/tdd-workflow.prompt.md` |
