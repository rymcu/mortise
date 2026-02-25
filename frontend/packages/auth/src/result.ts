import type { GlobalResultEnvelope } from './types'

export async function unwrapGlobalResult<T>(response: Response): Promise<T> {
  const payload = (await response.json()) as GlobalResultEnvelope<T>
  if (!response.ok) {
    throw new Error(payload?.message || 'Request failed')
  }

  if (!payload) {
    throw new Error('Invalid response envelope')
  }

  if (payload.code !== 200) {
    throw new Error(payload.message || 'Business request failed')
  }

  return payload.data
}
