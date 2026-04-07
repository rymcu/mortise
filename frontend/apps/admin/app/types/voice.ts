export type VoiceProviderType =
  | 'LOCAL_RUNTIME'
  | 'ALIYUN_TTS'
  | 'AZURE_TTS'
  | 'OPENAI_TTS'

export type VoiceCapability = 'ASR' | 'TTS' | 'VAD'

export type VoiceModelType = 'SENSEVOICE' | 'SHERPA_VAD' | 'TTS_VOICE'

export type VoiceTableAlign = 'left' | 'center' | 'right'

export type VoiceBadgeColor =
  | 'error'
  | 'info'
  | 'success'
  | 'primary'
  | 'secondary'
  | 'warning'
  | 'neutral'

export interface VoiceTableColumn {
  key: string
  label: string
  align?: VoiceTableAlign
}

export interface VoiceSelectOption {
  label: string
  value: string
}

export interface VoiceStatusMeta {
  label: string
  color: VoiceBadgeColor
}

export interface VoiceProviderInfo {
  id: string
  name?: string
  code?: string
  providerType?: string
  status?: number
  sortNo?: number
  defaultConfig?: string
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface VoiceModelInfo {
  id: string
  providerId?: string
  name?: string
  code?: string
  capability?: string
  modelType?: string
  runtimeName?: string
  version?: string
  language?: string
  status?: number
  concurrencyLimit?: number
  defaultModel?: boolean
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface VoiceProfileInfo {
  id: string
  name?: string
  code?: string
  language?: string
  asrProviderId?: string
  asrModelId?: string
  vadProviderId?: string
  vadModelId?: string
  ttsProviderId?: string
  ttsModelId?: string
  defaultParams?: string
  status?: number
  sortNo?: number
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface VoiceRuntimeNodeInfo {
  nodeId: string
  baseUrl?: string
  configStatus?: string
  probeStatus?: string
  detail?: string
  latencyMillis?: number
  checkedTime?: string
  loadedModels?: string[]
}

export interface VoiceJobInfo {
  id: string
  jobType?: string
  status?: string
  profileId?: string
  profileName?: string
  profileCode?: string
  userId?: string
  sourceModule?: string
  durationMillis?: number
  resultSummary?: string
  errorMessage?: string
  artifacts?: VoiceArtifactInfo[]
  createdTime?: string
  updatedTime?: string
}

export interface VoiceArtifactInfo {
  id: string
  fileId?: string
  artifactType?: string
  contentType?: string
  bucket?: string
  objectKey?: string
  fileUrl?: string
  filename?: string
  originalFilename?: string
  createdTime?: string
}

export interface VoiceProviderFormState {
  name: string
  code: string
  providerType: string
  status: number
  sortNo: number
  defaultConfig: string
  remark: string
}

export interface VoiceModelFormState {
  providerId: string
  name: string
  code: string
  capability: string
  modelType: string
  runtimeName: string
  version: string
  language: string
  concurrencyLimit: number | null
  defaultModel: boolean
  status: number
  remark: string
}

export interface VoiceProfileFormState {
  name: string
  code: string
  language: string
  asrProviderId: string
  asrModelId: string
  vadProviderId: string
  vadModelId: string
  ttsProviderId: string
  ttsModelId: string
  defaultParams: string
  status: number
  sortNo: number
  remark: string
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === 'object' && !Array.isArray(value)
}

function toOptionalString(value: unknown): string | undefined {
  if (typeof value === 'string' && value.trim()) {
    return value
  }

  if (typeof value === 'number' || typeof value === 'bigint') {
    return String(value)
  }

  return undefined
}

function toOptionalNumber(value: unknown): number | undefined {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return undefined
}

function toOptionalBoolean(value: unknown): boolean | undefined {
  if (typeof value === 'boolean') {
    return value
  }

  if (typeof value === 'string') {
    if (value === 'true') {
      return true
    }
    if (value === 'false') {
      return false
    }
  }

  return undefined
}

function toStringArray(value: unknown): string[] | undefined {
  if (!Array.isArray(value)) {
    return undefined
  }

  const result = value
    .map(item => toOptionalString(item))
    .filter((item): item is string => !!item)

  return result.length ? result : undefined
}

function toPayloadOptionalString(value: string): string | null {
  return value.trim() ? value.trim() : null
}

function toPayloadOptionalId(value: string): string | null {
  const normalized = value.trim()
  return normalized || null
}

export const voiceStatusOptions = [
  { label: '启用', value: '1' },
  { label: '禁用', value: '0' },
] satisfies VoiceSelectOption[]

export const voiceProviderTypeOptions = [
  { label: '本地运行时', value: 'LOCAL_RUNTIME' },
  { label: '阿里云 TTS', value: 'ALIYUN_TTS' },
  { label: 'Azure TTS', value: 'AZURE_TTS' },
  { label: 'OpenAI TTS', value: 'OPENAI_TTS' },
] satisfies VoiceSelectOption[]

export const voiceCapabilityOptions = [
  { label: '语音识别 ASR', value: 'ASR' },
  { label: '语音合成 TTS', value: 'TTS' },
  { label: '语音活动检测 VAD', value: 'VAD' },
] satisfies VoiceSelectOption[]

export const voiceModelTypeOptions = [
  { label: 'SenseVoice', value: 'SENSEVOICE' },
  { label: 'Sherpa VAD', value: 'SHERPA_VAD' },
  { label: 'TTS Voice', value: 'TTS_VOICE' },
] satisfies VoiceSelectOption[]

export const voiceJobStatusOptions = [
  { label: '已创建', value: 'CREATED' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '失败', value: 'FAILED' },
] satisfies VoiceSelectOption[]

export const voiceJobTypeOptions = [
  { label: '同步 ASR', value: 'ASR_SYNC' },
  { label: '流式 ASR', value: 'ASR_STREAM' },
  { label: '同步 TTS', value: 'TTS_SYNC' },
  { label: '流式 VAD', value: 'VAD_STREAM' },
] satisfies VoiceSelectOption[]

const voiceStatusMetaMap: Record<number, VoiceStatusMeta> = {
  1: { label: '启用', color: 'success' },
  0: { label: '禁用', color: 'neutral' },
}

const voiceRuntimeConfigStatusMetaMap: Record<string, VoiceStatusMeta> = {
  configured: { label: '已配置', color: 'success' },
  disabled: { label: '已禁用', color: 'neutral' },
}

const voiceRuntimeProbeStatusMetaMap: Record<string, VoiceStatusMeta> = {
  healthy: { label: '健康', color: 'success' },
  unhealthy: { label: '异常', color: 'error' },
  unreachable: { label: '不可达', color: 'warning' },
  skipped: { label: '已跳过', color: 'neutral' },
  invalid_config: { label: '配置无效', color: 'warning' },
}

const voiceJobStatusMetaMap: Record<string, VoiceStatusMeta> = {
  CREATED: { label: '已创建', color: 'neutral' },
  PROCESSING: { label: '处理中', color: 'info' },
  COMPLETED: { label: '已完成', color: 'success' },
  FAILED: { label: '失败', color: 'error' },
}

const voiceProviderTypeLabelMap: Record<string, string> = {
  LOCAL_RUNTIME: '本地运行时',
  ALIYUN_TTS: '阿里云 TTS',
  AZURE_TTS: 'Azure TTS',
  OPENAI_TTS: 'OpenAI TTS',
}

const voiceCapabilityLabelMap: Record<string, string> = {
  ASR: 'ASR',
  TTS: 'TTS',
  VAD: 'VAD',
}

const voiceModelTypeLabelMap: Record<string, string> = {
  SENSEVOICE: 'SenseVoice',
  SHERPA_VAD: 'Sherpa VAD',
  TTS_VOICE: 'TTS Voice',
}

const voiceJobTypeLabelMap: Record<string, string> = {
  ASR_SYNC: '同步 ASR',
  ASR_STREAM: '流式 ASR',
  TTS_SYNC: '同步 TTS',
  VAD_STREAM: '流式 VAD',
}

const voiceArtifactTypeLabelMap: Record<string, string> = {
  ASR_TRANSCRIPT: '识别文本',
  TTS_AUDIO: '合成音频',
}

export function getVoiceStatusMeta(status?: number): VoiceStatusMeta {
  return voiceStatusMetaMap[status ?? -1] ?? { label: '未知', color: 'warning' }
}

export function getVoiceRuntimeConfigStatusMeta(status?: string): VoiceStatusMeta {
  if (!status) {
    return { label: '未知', color: 'warning' }
  }

  return voiceRuntimeConfigStatusMetaMap[status] ?? { label: status, color: 'warning' }
}

export function getVoiceRuntimeProbeStatusMeta(status?: string): VoiceStatusMeta {
  if (!status) {
    return { label: '未知', color: 'warning' }
  }

  return voiceRuntimeProbeStatusMetaMap[status] ?? { label: status, color: 'warning' }
}

export function getVoiceJobStatusMeta(status?: string): VoiceStatusMeta {
  if (!status) {
    return { label: '未知', color: 'warning' }
  }

  return voiceJobStatusMetaMap[status] ?? { label: status, color: 'warning' }
}

export function getVoiceProviderTypeLabel(providerType?: string): string {
  if (!providerType) {
    return '-'
  }

  return voiceProviderTypeLabelMap[providerType] ?? providerType
}

export function getVoiceCapabilityLabel(capability?: string): string {
  if (!capability) {
    return '-'
  }

  return voiceCapabilityLabelMap[capability] ?? capability
}

export function getVoiceModelTypeLabel(modelType?: string): string {
  if (!modelType) {
    return '-'
  }

  return voiceModelTypeLabelMap[modelType] ?? modelType
}

export function getVoiceJobTypeLabel(jobType?: string): string {
  if (!jobType) {
    return '-'
  }

  return voiceJobTypeLabelMap[jobType] ?? jobType
}

export function getVoiceArtifactTypeLabel(artifactType?: string): string {
  if (!artifactType) {
    return '-'
  }

  return voiceArtifactTypeLabelMap[artifactType] ?? artifactType
}

export function normalizeVoiceProvider(value: unknown): VoiceProviderInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    name: toOptionalString(value.name),
    code: toOptionalString(value.code),
    providerType: toOptionalString(value.providerType),
    status: toOptionalNumber(value.status),
    sortNo: toOptionalNumber(value.sortNo),
    defaultConfig: toOptionalString(value.defaultConfig),
    remark: toOptionalString(value.remark),
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
  }
}

export function normalizeVoiceProviders(value: unknown): VoiceProviderInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeVoiceProvider(item))
    .filter((item): item is VoiceProviderInfo => !!item)
}

export function normalizeVoiceModel(value: unknown): VoiceModelInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    providerId: toOptionalString(value.providerId),
    name: toOptionalString(value.name),
    code: toOptionalString(value.code),
    capability: toOptionalString(value.capability),
    modelType: toOptionalString(value.modelType),
    runtimeName: toOptionalString(value.runtimeName),
    version: toOptionalString(value.version),
    language: toOptionalString(value.language),
    status: toOptionalNumber(value.status),
    concurrencyLimit: toOptionalNumber(value.concurrencyLimit),
    defaultModel: toOptionalBoolean(value.defaultModel),
    remark: toOptionalString(value.remark),
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
  }
}

export function normalizeVoiceModels(value: unknown): VoiceModelInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeVoiceModel(item))
    .filter((item): item is VoiceModelInfo => !!item)
}

export function normalizeVoiceProfile(value: unknown): VoiceProfileInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    name: toOptionalString(value.name),
    code: toOptionalString(value.code),
    language: toOptionalString(value.language),
    asrProviderId: toOptionalString(value.asrProviderId),
    asrModelId: toOptionalString(value.asrModelId),
    vadProviderId: toOptionalString(value.vadProviderId),
    vadModelId: toOptionalString(value.vadModelId),
    ttsProviderId: toOptionalString(value.ttsProviderId),
    ttsModelId: toOptionalString(value.ttsModelId),
    defaultParams: toOptionalString(value.defaultParams),
    status: toOptionalNumber(value.status),
    sortNo: toOptionalNumber(value.sortNo),
    remark: toOptionalString(value.remark),
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
  }
}

export function normalizeVoiceProfiles(value: unknown): VoiceProfileInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeVoiceProfile(item))
    .filter((item): item is VoiceProfileInfo => !!item)
}

export function normalizeVoiceRuntimeNode(value: unknown): VoiceRuntimeNodeInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const nodeId = toOptionalString(value.nodeId)
  if (!nodeId) {
    return null
  }

  return {
    nodeId,
    baseUrl: toOptionalString(value.baseUrl),
    configStatus: toOptionalString(value.configStatus),
    probeStatus: toOptionalString(value.probeStatus),
    detail: toOptionalString(value.detail),
    latencyMillis: toOptionalNumber(value.latencyMillis),
    checkedTime: toOptionalString(value.checkedTime),
    loadedModels: toStringArray(value.loadedModels),
  }
}

export function normalizeVoiceJob(value: unknown): VoiceJobInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    jobType: toOptionalString(value.jobType),
    status: toOptionalString(value.status),
    profileId: toOptionalString(value.profileId),
    profileName: toOptionalString(value.profileName),
    profileCode: toOptionalString(value.profileCode),
    userId: toOptionalString(value.userId),
    sourceModule: toOptionalString(value.sourceModule),
    durationMillis: toOptionalNumber(value.durationMillis),
    resultSummary: toOptionalString(value.resultSummary),
    errorMessage: toOptionalString(value.errorMessage),
    artifacts: normalizeVoiceArtifacts(value.artifacts),
    createdTime: toOptionalString(value.createdTime),
    updatedTime: toOptionalString(value.updatedTime),
  }
}

export function normalizeVoiceArtifact(value: unknown): VoiceArtifactInfo | null {
  if (!isRecord(value)) {
    return null
  }

  const id = toOptionalString(value.id)
  if (!id) {
    return null
  }

  return {
    id,
    fileId: toOptionalString(value.fileId),
    artifactType: toOptionalString(value.artifactType),
    contentType: toOptionalString(value.contentType),
    bucket: toOptionalString(value.bucket),
    objectKey: toOptionalString(value.objectKey),
    fileUrl: toOptionalString(value.fileUrl),
    filename: toOptionalString(value.filename),
    originalFilename: toOptionalString(value.originalFilename),
    createdTime: toOptionalString(value.createdTime),
  }
}

export function normalizeVoiceArtifacts(value: unknown): VoiceArtifactInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeVoiceArtifact(item))
    .filter((item): item is VoiceArtifactInfo => !!item)
}

export function normalizeVoiceJobs(value: unknown): VoiceJobInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeVoiceJob(item))
    .filter((item): item is VoiceJobInfo => !!item)
}

export function isOptionalJsonObjectString(value: string): boolean {
  const normalized = value.trim()
  if (!normalized) {
    return true
  }

  try {
    const parsed = JSON.parse(normalized)
    return !!parsed && typeof parsed === 'object' && !Array.isArray(parsed)
  } catch {
    return false
  }
}

export function normalizeVoiceRuntimeNodes(value: unknown): VoiceRuntimeNodeInfo[] {
  if (!Array.isArray(value)) {
    return []
  }

  return value
    .map(item => normalizeVoiceRuntimeNode(item))
    .filter((item): item is VoiceRuntimeNodeInfo => !!item)
}

export function createVoiceProviderFormState(
  data?: Partial<VoiceProviderInfo>
): VoiceProviderFormState {
  return {
    name: data?.name ?? '',
    code: data?.code ?? '',
    providerType: data?.providerType ?? 'LOCAL_RUNTIME',
    status: data?.status ?? 1,
    sortNo: data?.sortNo ?? 0,
    defaultConfig: data?.defaultConfig ?? '',
    remark: data?.remark ?? '',
  }
}

export function createVoiceModelFormState(
  data?: Partial<VoiceModelInfo>
): VoiceModelFormState {
  return {
    providerId: data?.providerId ?? '',
    name: data?.name ?? '',
    code: data?.code ?? '',
    capability: data?.capability ?? 'ASR',
    modelType: data?.modelType ?? 'SENSEVOICE',
    runtimeName: data?.runtimeName ?? '',
    version: data?.version ?? '',
    language: data?.language ?? '',
    concurrencyLimit: data?.concurrencyLimit ?? null,
    defaultModel: data?.defaultModel ?? false,
    status: data?.status ?? 1,
    remark: data?.remark ?? '',
  }
}

export function createVoiceProfileFormState(
  data?: Partial<VoiceProfileInfo>
): VoiceProfileFormState {
  return {
    name: data?.name ?? '',
    code: data?.code ?? '',
    language: data?.language ?? '',
    asrProviderId: data?.asrProviderId ?? '',
    asrModelId: data?.asrModelId ?? '',
    vadProviderId: data?.vadProviderId ?? '',
    vadModelId: data?.vadModelId ?? '',
    ttsProviderId: data?.ttsProviderId ?? '',
    ttsModelId: data?.ttsModelId ?? '',
    defaultParams: data?.defaultParams ?? '',
    status: data?.status ?? 1,
    sortNo: data?.sortNo ?? 0,
    remark: data?.remark ?? '',
  }
}

export function toVoiceProviderPayload(
  state: VoiceProviderFormState
): Record<string, unknown> {
  return {
    name: state.name.trim(),
    code: state.code.trim(),
    providerType: state.providerType,
    status: state.status,
    sortNo: state.sortNo,
    defaultConfig: toPayloadOptionalString(state.defaultConfig),
    remark: toPayloadOptionalString(state.remark),
  }
}

export function toVoiceModelPayload(
  state: VoiceModelFormState
): Record<string, unknown> {
  return {
    providerId: toPayloadOptionalId(state.providerId),
    name: state.name.trim(),
    code: state.code.trim(),
    capability: state.capability,
    modelType: state.modelType,
    runtimeName: toPayloadOptionalString(state.runtimeName),
    version: toPayloadOptionalString(state.version),
    language: toPayloadOptionalString(state.language),
    concurrencyLimit: state.concurrencyLimit,
    defaultModel: state.defaultModel,
    status: state.status,
    remark: toPayloadOptionalString(state.remark),
  }
}

export function toVoiceProfilePayload(
  state: VoiceProfileFormState
): Record<string, unknown> {
  return {
    name: state.name.trim(),
    code: state.code.trim(),
    language: toPayloadOptionalString(state.language),
    asrProviderId: toPayloadOptionalId(state.asrProviderId),
    asrModelId: toPayloadOptionalId(state.asrModelId),
    vadProviderId: toPayloadOptionalId(state.vadProviderId),
    vadModelId: toPayloadOptionalId(state.vadModelId),
    ttsProviderId: toPayloadOptionalId(state.ttsProviderId),
    ttsModelId: toPayloadOptionalId(state.ttsModelId),
    defaultParams: toPayloadOptionalString(state.defaultParams),
    status: state.status,
    sortNo: state.sortNo,
    remark: toPayloadOptionalString(state.remark),
  }
}

export function buildVoiceProviderOptions(
  providers: VoiceProviderInfo[]
): VoiceSelectOption[] {
  return providers.map(provider => ({
    label: `${provider.name || provider.code || provider.id} (${provider.code || provider.id})`,
    value: provider.id,
  }))
}

export function buildVoiceModelOptions(
  models: VoiceModelInfo[],
  capability?: string,
  providerId?: string
): VoiceSelectOption[] {
  return models
    .filter(model => !capability || model.capability === capability)
    .filter(model => !providerId || model.providerId === providerId)
    .map(model => ({
      label: `${model.name || model.code || model.id} (${model.code || model.id})`,
      value: model.id,
    }))
}