---
name: mortise-deployment
description: Use when deploying Mortise to runtime hosts, especially the current single-host Docker target at 192.168.88.146 under /opt/mortise. Covers backend jar rollout, site/admin Nuxt output rollout, standalone community frontend rollout, Nginx routing, docker-compose rebuilds, and native module troubleshooting for site SSR.
license: MIT
---

# Mortise Deployment

Project-specific deployment guidance for the current Docker-based runtime layout.

## When to Use

- Deploying backend changes to the live host
- Deploying `frontend` site or admin changes
- Deploying a standalone community frontend by replacing or reusing `mortise-site`
- Inspecting the 88.146 runtime topology under `/opt/mortise`
- Rebuilding `mortise-app`, `mortise-site`, or `mortise-admin`
- Troubleshooting SSR startup failures after frontend packaging

## Available Guidance

- **[references/topology.md](references/topology.md)** - runtime host layout, service ports, proxy routing, artifact locations
- **[references/playbooks.md](references/playbooks.md)** - step-by-step deployment commands for backend, site, and admin
- **[references/troubleshooting.md](references/troubleshooting.md)** - known deployment pitfalls, especially site native-module issues

## Loading Files

**Consider loading these reference files based on your task:**

- [ ] [references/topology.md](references/topology.md) - if you need to understand where artifacts live or which ports/routes are served
- [ ] [references/playbooks.md](references/playbooks.md) - if you need concrete deployment or rollback commands
- [ ] [references/troubleshooting.md](references/troubleshooting.md) - if the new image starts but runtime requests fail

**DO NOT load all files at once.** Load only what matches the deployment surface you are touching.

## Quick Start

1. Identify the deployment surface: `mortise-app`, `mortise-site`, or `mortise-admin`.
2. Back up the current runtime artifact before replacing it.
3. Sync the new jar or Nuxt `.output` to `/opt/mortise`.
4. Rebuild the corresponding image and restart only that service.
5. Verify with the service-specific health route or page route and then read logs.

## Guardrails

- Do **not** print or copy secrets from `.env` or container environment dumps into chat or docs.
- On `192.168.88.146`, trust the **actual** remote compose, Dockerfiles, and Nginx config over older local docs.
- The remote `frontend/` directory is a **deployment workspace**, not guaranteed to contain full monorepo source.
- `mortise-site` uses Nuxt SSR output with `better-sqlite3`; Windows-built output may need Linux-native repair before the site can boot cleanly.
- If you are deploying a standalone community app to the existing root site slot, treat it as a `mortise-site` deployment operationally: replace `/opt/mortise/frontend/site/.output`, rebuild `mortise-site`, and validate root-lifted community routes rather than the old `/community/*` paths.

## Fast Pitfalls Summary

- **Prefer `tar | ssh` over `scp -r` for `site/.output`** when the artifact is large. We hit `client_loop: send disconnect: Connection reset` during recursive copy, while tar streaming completed cleanly.
- **Treat Windows-built `site/.output` as needing remote native-module repair by default.** Reinstall `better-sqlite3` on the host via a Linux `node:22-slim` container before rebuilding `mortise-site`.
- **Standalone community builds still need the same `better-sqlite3` treatment.** The standalone template now enables `@nuxt/content`, and community markdown rendering reaches `parseMarkdown`, so the SSR bundle imports `better-sqlite3` even when you are not deploying the full `@mortise/site` app.
- **Large Nuxt SSR builds on Windows can OOM during Nitro prerender.** If `nuxt build` dies with `Reached heap limit`, retry with `NODE_OPTIONS=--max-old-space-size=8192` before changing app code or deployment flow.
- **In PowerShell, wrap remote `ssh` shell fragments with single quotes when they contain `$(...)`.** We hit a local `Get-Date` expansion when trying to run remote `$(date +%Y%m%d%H%M%S)` inside double quotes.
- **When sending shell scripts from PowerShell to remote bash, watch for CRLF.** We hit `bash: line N: $'\\r': command not found`; prefer a single-line remote command or strip CRLF before piping.
- **Do not validate Site deploys with `/` alone.** For the regular site, use business routes such as `/community` and `/community/collections`. For a standalone community root deployment, use lifted routes such as `/collections` and `/topics`, then read `docker logs --tail 40 mortise-site`.
- **Remote `docker build` can look stuck at `chown -R /app` even when it is healthy.** On the current host, rebuilding `mortise-site` may spend a couple of minutes on the final ownership-fix layer after copying `.output`; wait for completion before assuming the build hung.
- **`[Icon] failed to load icon ...` messages are noisy but not the primary health signal.** If business routes return `200` and the container stays up, treat these as follow-up asset issues rather than an SSR boot failure.

## Recommended Site / Standalone Deployment Shape

1. Build locally with the actual app you are shipping: `pnpm --filter @mortise/site build` for the main site, or the generated standalone app package when publishing a standalone community frontend.
2. Back up remote `/opt/mortise/frontend/site/.output`, and keep the remote `ssh` command single-quoted if it contains shell substitution like `$(date ...)`.
3. Replace it via `tar -C ... -cf - .output | ssh ... 'tar -xf -'`.
4. Repair `better-sqlite3` remotely if the build came from Windows, including standalone community bundles.
5. Rebuild `mortise-site`, restart only that service, then verify concrete business routes that match the deployed route shape.

## Standalone Community Deployment Notes

Use this flow when a standalone app generated from `frontend/templates/standalone` is intended to replace the current root `mortise-site`.

1. The runtime host still consumes `/opt/mortise/frontend/site/.output`; do **not** expect a separate remote monorepo app directory.
2. If the standalone app enables the community layer, assume `@nuxt/content` and `better-sqlite3` are in the SSR runtime unless you explicitly removed that dependency chain.
3. If local build fails on Windows with Nitro heap exhaustion, retry with `NODE_OPTIONS=--max-old-space-size=8192`.
4. After restart, validate root-lifted routes such as `/collections` and `/topics` instead of `/community/collections`.
5. Clean up any temporary local deploy app after rollout so it does not linger in the workspace or lockfile unnecessarily.
