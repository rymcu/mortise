<script setup lang="ts">
import type { HealthComponent, HealthResponse, MetricMeasurement, MetricResponse, InfoResponse } from '~/types'

const { $api } = useNuxtApp()

// 数据
const health = ref<HealthResponse | null>(null)
const info = ref<InfoResponse | null>(null)

// 指标
const memUsed = ref<number | null>(null)
const memMax = ref<number | null>(null)
const jvmCpu = ref<number | null>(null)
const sysCpu = ref<number | null>(null)
const threadsLive = ref<number | null>(null)
const threadsPeak = ref<number | null>(null)

const loading = ref(false)
const lastRefresh = ref<string>('')

// 自动刷新定时器
let refreshTimer: ReturnType<typeof setInterval> | null = null

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`
}

function formatPercent(value: number | null): string {
  if (value == null) return '-'
  return `${(value * 100).toFixed(1)}%`
}

function memPercent(): number {
  if (!memUsed.value || !memMax.value || memMax.value <= 0) return 0
  return Math.round((memUsed.value / memMax.value) * 100)
}

async function getMetric(name: string, tag?: string): Promise<number | null> {
  try {
    const path = tag ? `/actuator/metrics/${name}?tag=${tag}` : `/actuator/metrics/${name}`
    const data = await $api<MetricResponse>(path, { skipAuth: true })
    const val = data?.measurements?.[0]?.value
    return val != null ? val : null
  } catch {
    return null
  }
}

async function loadData() {
  loading.value = true
  try {
    const [healthData, infoData] = await Promise.allSettled([
      $api<HealthResponse>('/actuator/health', { skipAuth: true }),
      $api<InfoResponse>('/actuator/info', { skipAuth: true })
    ])

    health.value = healthData.status === 'fulfilled' ? healthData.value : null
    info.value = infoData.status === 'fulfilled' ? infoData.value : null

    const [mu, mm, jCpu, sCpu, tLive, tPeak] = await Promise.all([
      getMetric('jvm.memory.used', 'area:heap'),
      getMetric('jvm.memory.max', 'area:heap'),
      getMetric('process.cpu.usage'),
      getMetric('system.cpu.usage'),
      getMetric('jvm.threads.live'),
      getMetric('jvm.threads.peak')
    ])

    memUsed.value = mu
    memMax.value = mm
    jvmCpu.value = jCpu
    sysCpu.value = sCpu
    threadsLive.value = tLive
    threadsPeak.value = tPeak

    lastRefresh.value = new Date().toLocaleTimeString()
  } finally {
    loading.value = false
  }
}

function statusColor(status: string): 'success' | 'error' | 'warning' | 'neutral' {
  switch (status?.toUpperCase()) {
    case 'UP': return 'success'
    case 'DOWN': return 'error'
    case 'OUT_OF_SERVICE': return 'warning'
    default: return 'neutral'
  }
}

function cpuProgressColor(val: number | null): 'success' | 'warning' | 'error' {
  if (val == null) return 'success'
  if (val > 0.8) return 'error'
  if (val > 0.5) return 'warning'
  return 'success'
}

onMounted(() => {
  loadData()
  refreshTimer = setInterval(loadData, 30_000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

await loadData()
</script>

<template>
  <UDashboardPanel id="monitor">
    <template #header>
      <UDashboardNavbar title="系统监控">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
        <template #trailing>
          <div class="flex items-center gap-3">
            <span v-if="lastRefresh" class="text-xs text-muted hidden sm:block">
              最后刷新：{{ lastRefresh }}
            </span>
            <UButton
              icon="i-lucide-refresh-cw"
              size="sm"
              color="neutral"
              variant="soft"
              :loading="loading"
              @click="loadData"
            >
              刷新
            </UButton>
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-6">
        <!-- 整体健康状态 -->
        <UCard>
          <template #header>
            <div class="flex items-center gap-2">
              <UIcon name="i-lucide-activity" class="text-primary size-5" />
              <span class="font-semibold">健康状态</span>
              <UBadge
                v-if="health"
                :color="statusColor(health.status)"
                variant="subtle"
                size="sm"
              >
                {{ health?.status || '未知' }}
              </UBadge>
            </div>
          </template>

          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
            <template v-if="health?.components">
              <div
                v-for="(comp, name) in health.components"
                :key="name"
                class="flex items-center justify-between rounded-lg border border-default p-3"
              >
                <span class="text-sm capitalize">{{ name }}</span>
                <UBadge :color="statusColor(comp.status)" variant="subtle" size="xs">
                  {{ comp.status }}
                </UBadge>
              </div>
            </template>
            <div
              v-else-if="!loading"
              class="col-span-full text-center text-sm text-muted"
            >
              暂无健康状态数据
            </div>
            <div v-if="loading" class="col-span-full text-center text-sm text-muted">
              加载中...
            </div>
          </div>
        </UCard>

        <!-- 指标统计卡片 -->
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <!-- JVM 堆内存 -->
          <UCard>
            <template #header>
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-database" class="text-primary size-5" />
                <span class="font-semibold">JVM 堆内存</span>
              </div>
            </template>
            <div class="space-y-3">
              <div class="flex justify-between text-sm">
                <span class="text-muted">已用 / 最大</span>
                <span class="font-mono">
                  {{ memUsed != null ? formatBytes(memUsed) : '-' }}
                  /
                  {{ memMax != null ? formatBytes(memMax) : '-' }}
                </span>
              </div>
              <div class="h-2 overflow-hidden rounded-full bg-elevated">
                <div
                  class="h-full rounded-full transition-all"
                  :class="{
                    'bg-success-500': memPercent() < 70,
                    'bg-warning-500': memPercent() >= 70 && memPercent() < 85,
                    'bg-error-500': memPercent() >= 85
                  }"
                  :style="{ width: `${memPercent()}%` }"
                />
              </div>
              <div class="text-right text-xs text-muted">使用率 {{ memPercent() }}%</div>
            </div>
          </UCard>

          <!-- CPU 使用率 -->
          <UCard>
            <template #header>
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-cpu" class="text-primary size-5" />
                <span class="font-semibold">CPU 使用率</span>
              </div>
            </template>
            <div class="space-y-3">
              <div class="flex items-center justify-between text-sm">
                <span class="text-muted">JVM 进程 CPU</span>
                <span class="font-mono font-semibold">{{ formatPercent(jvmCpu) }}</span>
              </div>
              <div class="h-2 overflow-hidden rounded-full bg-elevated">
                <div
                  class="h-full rounded-full transition-all"
                  :class="{
                    'bg-success-500': (jvmCpu ?? 0) < 0.5,
                    'bg-warning-500': (jvmCpu ?? 0) >= 0.5 && (jvmCpu ?? 0) < 0.8,
                    'bg-error-500': (jvmCpu ?? 0) >= 0.8
                  }"
                  :style="{ width: `${((jvmCpu ?? 0) * 100).toFixed(1)}%` }"
                />
              </div>
              <div class="flex items-center justify-between text-sm">
                <span class="text-muted">系统 CPU</span>
                <span class="font-mono font-semibold">{{ formatPercent(sysCpu) }}</span>
              </div>
              <div class="h-2 overflow-hidden rounded-full bg-elevated">
                <div
                  class="h-full rounded-full transition-all"
                  :class="{
                    'bg-success-500': (sysCpu ?? 0) < 0.5,
                    'bg-warning-500': (sysCpu ?? 0) >= 0.5 && (sysCpu ?? 0) < 0.8,
                    'bg-error-500': (sysCpu ?? 0) >= 0.8
                  }"
                  :style="{ width: `${((sysCpu ?? 0) * 100).toFixed(1)}%` }"
                />
              </div>
            </div>
          </UCard>

          <!-- 线程 -->
          <UCard>
            <template #header>
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-layers" class="text-primary size-5" />
                <span class="font-semibold">线程</span>
              </div>
            </template>
            <div class="grid grid-cols-2 gap-4">
              <div class="rounded-lg bg-elevated p-4 text-center">
                <div class="text-3xl font-bold text-primary">
                  {{ threadsLive != null ? Math.round(threadsLive) : '-' }}
                </div>
                <div class="mt-1 text-xs text-muted">活跃线程</div>
              </div>
              <div class="rounded-lg bg-elevated p-4 text-center">
                <div class="text-3xl font-bold">
                  {{ threadsPeak != null ? Math.round(threadsPeak) : '-' }}
                </div>
                <div class="mt-1 text-xs text-muted">峰值线程</div>
              </div>
            </div>
          </UCard>

          <!-- 应用信息 -->
          <UCard>
            <template #header>
              <div class="flex items-center gap-2">
                <UIcon name="i-lucide-info" class="text-primary size-5" />
                <span class="font-semibold">应用信息</span>
              </div>
            </template>
            <template v-if="info">
              <div class="space-y-2 text-sm">
                <div
                  v-if="info.app?.name"
                  class="flex justify-between"
                >
                  <span class="text-muted">应用名称</span>
                  <span>{{ info.app.name }}</span>
                </div>
                <div
                  v-if="info.app?.version"
                  class="flex justify-between"
                >
                  <span class="text-muted">版本</span>
                  <span class="font-mono text-xs">{{ info.app.version }}</span>
                </div>
                <div
                  v-if="info.java?.version"
                  class="flex justify-between"
                >
                  <span class="text-muted">Java 版本</span>
                  <span class="font-mono text-xs">{{ info.java.version }}</span>
                </div>
                <template v-for="(val, key) in info" :key="key">
                  <div
                    v-if="key !== 'app' && key !== 'java' && typeof val !== 'object'"
                    class="flex justify-between"
                  >
                    <span class="text-muted capitalize">{{ key }}</span>
                    <span class="font-mono text-xs">{{ val }}</span>
                  </div>
                </template>
              </div>
            </template>
            <div v-else class="text-center text-sm text-muted">
              暂无应用信息
            </div>
          </UCard>
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
