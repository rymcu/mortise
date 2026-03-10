---
description: 'Mortise backend module boundaries, layering rules, and extension patterns'
applyTo: 'pom.xml, mortise-*/pom.xml, mortise-*/**/*.java'
---

# Backend Module Boundaries

## Scope

- This instruction applies to Mortise backend modules, including Maven `pom.xml` files and Java sources under `mortise-*`.
- Use it together with `java.instructions.md` and `springboot.instructions.md`; when guidance conflicts, prefer the more specific boundary rule here for module placement and dependencies.

## Layering Rules

- Preserve the existing dependency direction: `mortise-common` / `mortise-core` → infrastructure services → application base → business `application/infra` → business `admin/api` → `mortise-app` aggregation.
- **Do not introduce same-layer direct dependencies.** In particular, modules such as `mortise-auth`, `mortise-web-support`, and `mortise-monitor` must not directly depend on each other just to share configuration.
- `mortise-app` is the aggregation boundary. Cross-module wiring that only makes sense at runtime should usually be assembled there rather than pushed downward.
- Do not move business logic into `mortise-common` or `mortise-core` unless it is truly cross-domain and reusable without business context.

## Module Placement

- Put domain models, enums, and pure business concepts in `*-domain`.
- Put orchestration and business use cases in `*-application`.
- Put persistence, third-party adapters, mappers, and technical integrations in `*-infra`.
- Keep `*-admin` and `*-api` focused on transport concerns, request/response models, authorization annotations, and endpoint composition.
- Keep controllers thin. If a controller needs non-trivial branching, the logic is probably misplaced and belongs in application services.

## Cross-Module Extension Patterns

- Prefer **SPI interfaces**, Spring events, or aggregation-layer composition for cross-module extension.
- Avoid hard-coding another same-layer module's endpoints, beans, or configuration rules inside a module that should remain independent.
- If one module needs to contribute security rules, cache TTLs, logging hooks, or similar behavior to another module, define an extension contract in the owning module and implement it from the contributor module.
- Use optional dependencies only when a module needs compile-time access to an extension contract but should not force a hard runtime dependency chain.

## Business Module Conventions

- Standard business modules should follow the existing `domain/application/infra/admin/api` split rather than mixing all responsibilities into one module.
- For foundational business modules such as `mortise-member`, keep the shared/base service layer minimal and generic.
- Scenario-specific methods for admin or client use cases belong in the consuming module's own service layer, not back in the shared base module.
- Do not leak persistence entities directly through every API; prefer dedicated request/response DTOs or records.

## Maven Boundaries

- Use `mvn`, never `mvnw` or `mvnw.cmd`.
- Be careful when editing the root `pom.xml`: not every directory in the repository belongs in the default reactor.
- Commercial or private modules may exist in the workspace but still be intentionally excluded from the root `<modules>` list.
- `mortise-app/pom.xml` uses the `pro` profile to assemble optional commercial modules based on local directory presence. Check that profile before changing aggregation behavior.
- Do not add broad dependencies between modules when a narrower dependency or SPI contract will do.

## Change Heuristics

- Before creating a new module dependency, ask whether the need is actually:
  - a DTO placement problem,
  - an SPI extension point,
  - an application-service boundary issue,
  - or an aggregation concern for `mortise-app`.
- When touching multiple backend modules, preserve their existing public contracts unless the task explicitly requires a boundary change.
- Avoid opportunistic package moves or wide refactors unless they are required to fix a real architectural violation.

## Verification

- Single-module backend change: prefer `mvn -pl <module> -am clean compile -DskipTests`.
- Cross-module wiring or aggregation change: prefer `mvn -pl mortise-app -am clean compile -DskipTests`.
- Test-only or behavior-sensitive changes: run `mvn -pl <module> -am test` when practical.
- If validation is blocked by environment issues such as missing `ENCRYPTION_KEY`, PostgreSQL permissions, or private module access, state that clearly instead of making speculative code changes.

## Anti-Patterns

- Do not solve a same-layer dependency problem by adding a direct module dependency “just to get it working”.
- Do not put admin-only or client-only methods into shared foundational modules.
- Do not treat `mortise-app` as a place for business logic; it is an assembly boundary.
- Do not assume README examples are authoritative when `pom.xml`, code structure, or actual profiles say otherwise.