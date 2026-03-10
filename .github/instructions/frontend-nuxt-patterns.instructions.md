---
description: 'Mortise frontend Nuxt app, layer, and package patterns'
applyTo: 'frontend/**/*.{vue,ts,tsx,js,mjs,cjs}'
---

# Frontend Nuxt Patterns

## Scope

- This instruction applies to the Mortise frontend workspace under `frontend/`.
- It covers Nuxt apps, shared packages, and layers.
- Use it with the root workspace instructions; when guidance conflicts, prefer this file for frontend structure, typing, and verification.

## Workspace Shape

- `frontend/apps/admin` is the admin app. Treat it as the backend management UI.
- `frontend/apps/site` is the site and end-user app. Treat it as the host for optional layers.
- `frontend/packages` contains shared capabilities such as auth, SDK, UI, and config. Prefer extending these shared packages rather than duplicating logic in each app.
- `frontend/layers/base` is always-on shared behavior.
- `frontend/layers/community` and `frontend/layers/commerce` are optional layers. Do not assume they are always present, enabled, or available to edit.
- `frontend/templates` is not part of the default workspace build, lint, or typecheck flow. Avoid changing it unless the task is specifically about templates.

## Package and App Boundaries

- Keep cross-app reusable logic in `frontend/packages/*`.
- Keep app-specific route behavior, page state, and UI composition inside the corresponding app.
- Do not copy shared auth or SDK logic into pages or components when `@mortise/auth` or `@mortise/core-sdk` already owns that concern.
- Respect the existing boundary that `@mortise/core-sdk` is the shared backend SDK surface; avoid inventing parallel fetch wrappers inside random pages.
- When touching optional layers, preserve the pattern that `apps/site` hosts them through Nuxt layer extension rather than direct hard-coded feature sprawl.

## Nuxt Conventions

- Follow existing Nuxt auto-import patterns before adding manual imports.
- Prefer existing composables, generated imports, and workspace utilities over ad hoc local helpers.
- `apps/admin` uses `ssr: false` and relies on relative `/mortise` API calls plus Vite proxy during local development.
- `apps/site` uses `ssr: true` and defaults to a direct backend URL in runtime config.
- When docs and code disagree on ports or scripts, trust `package.json`, `nuxt.config.ts`, and actual app configuration over older README text.

## Type Rules

- **All frontend IDs must be `string`, never `number`.** This includes route params and API fields such as `id`, `userId`, `articleId`, and similar identifiers.
- Do not coerce route params to numbers.
- **Do not define `interface` or `type` inside Vue SFC `<script setup>` blocks.** Extract them into dedicated `.ts` files, usually under an app or package `types/` directory, then import with `import type`.
- Prefer repository types, local inference, or dedicated type modules over inline type declarations in components.
- In this workspace, direct type imports from `@nuxt/ui` may be unstable. Prefer existing local types or inference when possible.

## Form and Component Rules

- Any `UInput` with password behavior must implement show/hide toggle behavior rather than a static `type="password"` field.
- Keep the repository's accessibility pattern for password toggles: dynamic `type`, trailing toggle button, correct `aria-label`, and `aria-pressed`.
- Reuse existing UI building blocks and class patterns before introducing new one-off styling systems.
- Do not move complex data shaping, API orchestration, or business rules into presentation-only components if a composable, store, or shared package is the better home.

## Content and Media Rules

- Prefer Nuxt-generated auto-imports for Markdown/content runtime helpers instead of direct imports that may break typecheck.
- In particular, avoid directly importing `parseMarkdown` from `@nuxtjs/mdc/runtime` when an auto-imported equivalent is already available in the workspace.
- Upload APIs often return relative media paths. Use existing media URL helpers instead of assembling public URLs manually.
- For Markdown media workflows, preserve the established edit/display/store conversion utilities instead of introducing alternative conversion logic.

## Environment and Runtime Pitfalls

- Use `pnpm` only. Do not use npm or yarn in this workspace.
- `frontend/package.json` pins `pnpm@10.29.3`; stay aligned with workspace scripts.
- In `apps/admin`, setting `NUXT_PUBLIC_API_BASE` to a full remote URL during local development can bypass the proxy and cause CORS issues.
- In `apps/site`, editor diagnostics may misreport Nuxt auto-import issues; prefer actual `pnpm --filter @mortise/site typecheck` results.
- Optional layers and private submodules may be missing by design. Treat missing commercial/frontend layer access as an environment boundary before assuming the code is broken.

## Change Heuristics

- Before adding a new helper, check whether the same concern already exists in `packages/auth`, `packages/core-sdk`, `packages/ui`, or shared composables.
- Before introducing a new type file, match the nearest existing naming and placement convention.
- Before changing routing or auth behavior, inspect the app's `nuxt.config.ts` and any shared auth runtime package code.
- Keep changes local to the app or package that owns the concern. Avoid broad workspace-wide refactors unless the task actually spans multiple apps.

## Verification

- Admin page/component change: prefer `pnpm --filter @mortise/admin lint` or `pnpm --filter @mortise/admin typecheck`.
- Site page/component/content integration change: prefer `pnpm --filter @mortise/site typecheck`.
- Shared package change: run the matching package typecheck or `pnpm typecheck:packages`.
- Cross-app frontend change: consider `pnpm lint` and `pnpm typecheck` from `frontend/`.
- If a task only changes docs or comments, mention that no frontend build validation was run.

## Anti-Patterns

- Do not introduce `number` IDs in frontend models or route handling.
- Do not define types inline in `.vue` files.
- Do not bypass shared auth/SDK packages with page-local fetch abstractions unless the task explicitly requires a new shared capability.
- Do not trust stale docs over live config for ports, scripts, or runtime behavior.
- Do not assume optional layers are always installed, enabled, or available.