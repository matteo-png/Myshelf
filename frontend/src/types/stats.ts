export interface StatsOverview {
  collectionsCount: number
  itemsCount: number
  categoriesCount: number
  tagsCount: number
  purchasePlacesCount: number
  totalEstimatedValue: number
}

export interface StatsGroup {
  label: string
  count: number
  totalValue: number
}

export interface StatsTimeSeriesPoint {
  period: string
  count: number
  totalValue: number
}