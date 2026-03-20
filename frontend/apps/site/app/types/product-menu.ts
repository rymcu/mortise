import type { AppProduct, AppProductCategory } from '@mortise/core-sdk'

export type ProductMenuSection = {
  id: string
  label: string
  products: AppProduct[]
}

export type CategoryEntry = {
  category: AppProductCategory
  sections: ProductMenuSection[]
}
