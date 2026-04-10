---
name: mortise-deployment
description: Use when deploying Mortise to runtime hosts, especially the current single-host Docker target at 192.168.88.146 under /opt/mortise. Covers backend jar rollout, site/admin Nuxt output rollout, Nginx routing, docker-compose rebuilds, and native module troubleshooting for site SSR.
license: MIT
---

# Mortise Deployment

Project-specific deployment guidance for the current Docker-based runtime layout.

## When to Use

- Deploying backend changes to the live host
- Deploying `frontend` site or admin changes
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

## Fast Pitfalls Summary

- **Prefer `tar | ssh` over `scp -r` for `site/.output`** when the artifact is large. We hit `client_loop: send disconnect: Connection reset` during recursive copy, while tar streaming completed cleanly.
- **Treat Windows-built `site/.output` as needing remote native-module repair by default.** Reinstall `better-sqlite3` on the host via a Linux `node:22-slim` container before rebuilding `mortise-site`.
- **In PowerShell, wrap remote `ssh` shell fragments with single quotes when they contain `$(...)`.** We hit a local `Get-Date` expansion when trying to run remote `$(date +%Y%m%d%H%M%S)` inside double quotes.
- **When sending shell scripts from PowerShell to remote bash, watch for CRLF.** We hit `bash: line N: $'\\r': command not found`; prefer a single-line remote command or strip CRLF before piping.
- **Do not validate Site deploys with `/` alone.** Use business routes such as `/community` and `/community/collections`, then read `docker logs --tail 40 mortise-site`.

## Recommended Site Deployment Shape

1. Build locally with `pnpm --filter @mortise/site build`.
2. Back up remote `/opt/mortise/frontend/site/.output`, and keep the remote `ssh` command single-quoted if it contains shell substitution like `$(date ...)`.
3. Replace it via `tar -C ... -cf - .output | ssh ... 'tar -xf -'`.
4. Repair `better-sqlite3` remotely if the build came from Windows.
5. Rebuild `mortise-site`, restart only that service, then verify concrete community routes.
