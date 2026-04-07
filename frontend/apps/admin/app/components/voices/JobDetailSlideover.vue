<script setup lang="ts">
import {
  getVoiceArtifactTypeLabel,
  getVoiceJobStatusMeta,
  getVoiceJobTypeLabel,
} from '~/types/voice'
import type { VoiceArtifactInfo, VoiceJobInfo } from '~/types/voice'

defineProps<{
  open: boolean
  job: VoiceJobInfo | null
}>()

defineEmits<{
  'update:open': [value: boolean]
}>()

function displayProfile(job: VoiceJobInfo | null): string {
  if (!job) {
    return '-'
  }

  return job.profileName || job.profileCode || job.profileId || '-'
}

function displayArtifactName(artifact: VoiceArtifactInfo): string {
  return artifact.originalFilename || artifact.filename || artifact.fileId || artifact.id
}
</script>

<template>
  <USlideover :open="open" title="语音任务详情" @update:open="$emit('update:open', $event)">
    <template #body>
      <div v-if="job" class="space-y-3 p-4 text-sm">
        <div class="grid grid-cols-3 gap-x-4 gap-y-3">
          <div class="col-span-1 text-muted">任务 ID</div>
          <div class="col-span-2 break-all font-mono text-xs">{{ job.id }}</div>

          <div class="col-span-1 text-muted">任务类型</div>
          <div class="col-span-2">
            <UBadge color="neutral" variant="outline" size="xs">
              {{ getVoiceJobTypeLabel(job.jobType) }}
            </UBadge>
          </div>

          <div class="col-span-1 text-muted">任务状态</div>
          <div class="col-span-2">
            <UBadge
              :color="getVoiceJobStatusMeta(job.status).color"
              variant="subtle"
              size="xs"
            >
              {{ getVoiceJobStatusMeta(job.status).label }}
            </UBadge>
          </div>

          <div class="col-span-1 text-muted">Profile</div>
          <div class="col-span-2">{{ displayProfile(job) }}</div>

          <div class="col-span-1 text-muted">用户 ID</div>
          <div class="col-span-2">{{ job.userId || '-' }}</div>

          <div class="col-span-1 text-muted">来源模块</div>
          <div class="col-span-2">{{ job.sourceModule || '-' }}</div>

          <div class="col-span-1 text-muted">耗时（ms）</div>
          <div class="col-span-2">{{ job.durationMillis ?? '-' }}</div>

          <div class="col-span-1 text-muted">创建时间</div>
          <div class="col-span-2">{{ job.createdTime || '-' }}</div>

          <div class="col-span-1 text-muted">更新时间</div>
          <div class="col-span-2">{{ job.updatedTime || '-' }}</div>
        </div>

        <template v-if="job.resultSummary">
          <div class="text-muted">结果摘要</div>
          <pre class="overflow-x-auto rounded bg-elevated p-3 font-mono text-xs whitespace-pre-wrap">{{ job.resultSummary }}</pre>
        </template>

        <template v-if="job.errorMessage">
          <div class="text-red-500">错误信息</div>
          <pre class="overflow-x-auto rounded bg-red-50 p-3 font-mono text-xs text-red-700 whitespace-pre-wrap dark:bg-red-950 dark:text-red-300">{{ job.errorMessage }}</pre>
        </template>

        <template v-if="job.artifacts?.length">
          <div class="text-muted">产物记录</div>
          <div class="space-y-3">
            <div
              v-for="artifact in job.artifacts"
              :key="artifact.id"
              class="rounded border border-default p-3"
            >
              <div class="mb-2 flex flex-wrap items-center gap-2">
                <UBadge color="primary" variant="subtle" size="xs">
                  {{ getVoiceArtifactTypeLabel(artifact.artifactType) }}
                </UBadge>
                <span class="font-medium">{{ displayArtifactName(artifact) }}</span>
              </div>

              <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-xs">
                <div class="col-span-1 text-muted">Artifact ID</div>
                <div class="col-span-2 font-mono">{{ artifact.id }}</div>

                <div class="col-span-1 text-muted">文件 ID</div>
                <div class="col-span-2 font-mono">{{ artifact.fileId || '-' }}</div>

                <div class="col-span-1 text-muted">内容类型</div>
                <div class="col-span-2">{{ artifact.contentType || '-' }}</div>

                <div class="col-span-1 text-muted">存储路径</div>
                <div class="col-span-2 break-all font-mono">{{ artifact.objectKey || '-' }}</div>

                <div class="col-span-1 text-muted">创建时间</div>
                <div class="col-span-2">{{ artifact.createdTime || '-' }}</div>

                <div class="col-span-1 text-muted">文件链接</div>
                <div class="col-span-2 break-all">
                  <a
                    v-if="artifact.fileUrl"
                    :href="artifact.fileUrl"
                    target="_blank"
                    rel="noreferrer"
                    class="text-primary underline-offset-4 hover:underline"
                  >
                    {{ artifact.fileUrl }}
                  </a>
                  <span v-else>-</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>
  </USlideover>
</template>