import {
  fetchAdminPost,
  fetchAdminPut,
  fetchAdminDelete,
  fetchAdminBatchDelete
} from '@mortise/core-sdk'

/**
 * 通用 CRUD 操作 composable
 * 提供新增、编辑、删除、批量删除等操作封装
 */
export function useAdminCrud(basePath: string) {
  const { $api } = useNuxtApp()
  const loading = ref(false)
  const errorMessage = ref('')

  /** 新增 */
  async function create<T = unknown>(
    data: Record<string, unknown>
  ): Promise<T | null> {
    loading.value = true
    errorMessage.value = ''
    try {
      return await fetchAdminPost<T>($api, basePath, data)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '新增失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 更新 */
  async function update<T = unknown>(
    id: string | number,
    data: Record<string, unknown>
  ): Promise<T | null> {
    loading.value = true
    errorMessage.value = ''
    try {
      return await fetchAdminPut<T>($api, `${basePath}/${id}`, data)
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '更新失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 删除 */
  async function remove(id: string | number): Promise<boolean> {
    loading.value = true
    errorMessage.value = ''
    try {
      await fetchAdminDelete($api, `${basePath}/${id}`)
      return true
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '删除失败'
      return false
    } finally {
      loading.value = false
    }
  }

  /** 批量删除 */
  async function batchRemove(ids: (string | number)[]): Promise<boolean> {
    loading.value = true
    errorMessage.value = ''
    try {
      await fetchAdminBatchDelete($api, basePath, ids)
      return true
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '删除失败'
      return false
    } finally {
      loading.value = false
    }
  }

  /** 自定义 POST 操作（如重置密码、分配角色等） */
  async function postAction<T = unknown>(
    subPath: string,
    data?: Record<string, unknown>
  ): Promise<T | null> {
    loading.value = true
    errorMessage.value = ''
    try {
      return await fetchAdminPost<T>($api, `${basePath}${subPath}`, data ?? {})
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '操作失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 自定义 PUT 操作（如启用/禁用等） */
  async function putAction<T = unknown>(
    subPath: string,
    data?: Record<string, unknown>
  ): Promise<T | null> {
    loading.value = true
    errorMessage.value = ''
    try {
      return await fetchAdminPut<T>($api, `${basePath}/${subPath}`, data ?? {})
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : '操作失败'
      return null
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    errorMessage,
    create,
    update,
    remove,
    batchRemove,
    postAction,
    putAction
  }
}
