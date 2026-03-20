/**
 * 将数组均匀分成指定数量的列（round-robin 分配）。
 */
export function chunkItems<T>(items: T[], chunkCount: number): T[][] {
  if (!items.length) {
    return []
  }

  const normalizedChunkCount = Math.max(1, Math.min(chunkCount, items.length))
  const columns = Array.from({ length: normalizedChunkCount }, () => [] as T[])

  items.forEach((item, index) => {
    columns[index % normalizedChunkCount]!.push(item)
  })

  return columns
}
