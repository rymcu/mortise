export interface GlobalResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  pageNumber: number
  pageSize: number
  totalPage: number
  totalRow: number
}
