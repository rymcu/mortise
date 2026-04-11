import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { join, resolve } from 'node:path'

function normalizeRelativeDir(dir) {
  return dir.replace(/\\/g, '/').replace(/^\/+|\/+$/g, '')
}

function readJson(filePath) {
  return JSON.parse(readFileSync(filePath, 'utf-8'))
}

function walkLayers(rootDir, baseDir = rootDir, prefix = '') {
  const results = []
  if (!existsSync(rootDir)) {
    return results
  }

  for (const entry of readdirSync(rootDir, { withFileTypes: true })) {
    if (!entry.isDirectory()) continue
    if (entry.name === 'node_modules' || entry.name.startsWith('.')) continue

    const nextPrefix = prefix ? `${prefix}/${entry.name}` : entry.name
    const entryDir = join(baseDir, nextPrefix)
    const pkgPath = join(entryDir, 'package.json')
    const nuxtCfgPath = join(entryDir, 'nuxt.config.ts')

    if (existsSync(pkgPath) && existsSync(nuxtCfgPath)) {
      try {
        const pkg = readJson(pkgPath)
        results.push({
          relativeDir: normalizeRelativeDir(nextPrefix),
          absoluteDir: entryDir,
          packageName: pkg.name
        })
        continue
      } catch {
        continue
      }
    }

    results.push(...walkLayers(join(rootDir, entry.name), baseDir, nextPrefix))
  }

  return results
}

function readDeclaredLayerDeps(appRoot) {
  const pkgPath = resolve(appRoot, 'package.json')
  const appPkg = readJson(pkgPath)
  return new Set([
    ...Object.keys(appPkg.dependencies ?? {}),
    ...Object.keys(appPkg.devDependencies ?? {}),
    ...Object.keys(appPkg.optionalDependencies ?? {})
  ])
}

function isInstalledLayer(appRoot, packageName) {
  if (!packageName) {
    return false
  }
  return existsSync(resolve(appRoot, 'node_modules', ...packageName.split('/')))
}

function shouldIncludeLayer(relativeDir, appKind) {
  if (relativeDir === 'base') {
    return true
  }

  const isAdminLayer = relativeDir.startsWith('admin/')
  if (appKind === 'site') {
    return !isAdminLayer
  }
  if (appKind === 'admin') {
    return isAdminLayer
  }
  return true
}

export function collectLayerEntries(layersRoot) {
  return walkLayers(layersRoot)
}

export function resolveAppLayerEntries({
  appRoot,
  layersRoot,
  localLayersBase,
  appKind
}) {
  const declaredLayerDeps = readDeclaredLayerDeps(appRoot)
  const entries = collectLayerEntries(layersRoot)
  const layers = []

  for (const entry of entries) {
    const { relativeDir, packageName } = entry
    if (!shouldIncludeLayer(relativeDir, appKind)) {
      continue
    }

    if (relativeDir !== 'base') {
      const isDeclared = declaredLayerDeps.has(packageName)
      const isInstalled = isInstalledLayer(appRoot, packageName)
      if (!(isDeclared && isInstalled)) {
        continue
      }
    }

    const routePrefix =
      relativeDir === 'base' ? '' : (relativeDir.split('/').at(-1) ?? '')

    layers.push({
      ...entry,
      isBase: relativeDir === 'base',
      localPath: join(localLayersBase, relativeDir).replace(/\\/g, '/'),
      routePrefix
    })
  }

  return layers
}

export function resolveAppLayerExtends(options) {
  return resolveAppLayerEntries(options).map((entry) => entry.localPath)
}

export function resolveLayerEntry(layerInput, appName, layersRoot) {
  const entries = collectLayerEntries(layersRoot).filter(
    (entry) => entry.relativeDir !== 'base'
  )

  const normalizedInput = normalizeRelativeDir(
    layerInput
      .replace(/^@mortise\//, '')
      .replace(/-layer$/, '')
      .replace(/^layers\//, '')
  )

  const preferredDirs =
    appName === 'admin'
      ? [`admin/${normalizedInput}`, normalizedInput]
      : [normalizedInput, `admin/${normalizedInput}`]

  for (const dir of preferredDirs) {
    const matched = entries.find((entry) => entry.relativeDir === dir)
    if (matched) {
      return matched
    }
  }

  return (
    entries.find((entry) => entry.packageName === layerInput) ??
    entries.find(
      (entry) => entry.packageName === `@mortise/${normalizedInput}-layer`
    ) ??
    null
  )
}
