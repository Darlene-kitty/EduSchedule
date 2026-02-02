import { useCallback, useEffect, useRef, useState } from 'react'

// Hook pour mesurer les performances des composants
export function usePerformance(componentName: string) {
  const renderStartTime = useRef<number>(Date.now())
  const [renderTime, setRenderTime] = useState<number>(0)
  const [rerenderCount, setRerenderCount] = useState<number>(0)

  useEffect(() => {
    const endTime = Date.now()
    const duration = endTime - renderStartTime.current
    setRenderTime(duration)
    setRerenderCount(prev => prev + 1)
    
    if (duration > 100) {
      console.warn(`⚠️ Slow render detected in ${componentName}: ${duration}ms`)
    }
    
    renderStartTime.current = endTime
  })

  const measureAsync = useCallback(async <T>(
    operation: () => Promise<T>,
    operationName: string
  ): Promise<T> => {
    const start = Date.now()
    try {
      const result = await operation()
      const duration = Date.now() - start
      
      if (duration > 1000) {
        console.warn(`⚠️ Slow async operation in ${componentName}.${operationName}: ${duration}ms`)
      }
      
      return result
    } catch (error) {
      const duration = Date.now() - start
      console.error(`❌ Failed operation in ${componentName}.${operationName} after ${duration}ms:`, error)
      throw error
    }
  }, [componentName])

  return {
    renderTime,
    rerenderCount,
    measureAsync
  }
}

// Hook pour la pagination optimisée
export function useOptimizedPagination<T>(
  items: T[],
  itemsPerPage: number = 20
) {
  const [currentPage, setCurrentPage] = useState(1)
  const [searchQuery, setSearchQuery] = useState('')
  const [filteredItems, setFilteredItems] = useState<T[]>(items)

  // Filtrage optimisé avec debounce
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      if (!searchQuery.trim()) {
        setFilteredItems(items)
        return
      }

      const filtered = items.filter(item => 
        JSON.stringify(item).toLowerCase().includes(searchQuery.toLowerCase())
      )
      setFilteredItems(filtered)
      setCurrentPage(1) // Reset à la première page lors du filtrage
    }, 300) // Debounce de 300ms

    return () => clearTimeout(timeoutId)
  }, [items, searchQuery])

  const totalPages = Math.ceil(filteredItems.length / itemsPerPage)
  const startIndex = (currentPage - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  const currentItems = filteredItems.slice(startIndex, endIndex)

  const goToPage = useCallback((page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page)
    }
  }, [totalPages])

  const nextPage = useCallback(() => {
    goToPage(currentPage + 1)
  }, [currentPage, goToPage])

  const prevPage = useCallback(() => {
    goToPage(currentPage - 1)
  }, [currentPage, goToPage])

  return {
    currentItems,
    currentPage,
    totalPages,
    totalItems: filteredItems.length,
    searchQuery,
    setSearchQuery,
    goToPage,
    nextPage,
    prevPage,
    hasNextPage: currentPage < totalPages,
    hasPrevPage: currentPage > 1
  }
}

// Hook pour le cache local optimisé
export function useOptimizedCache<T>(
  key: string,
  fetcher: () => Promise<T>,
  options: {
    ttl?: number // Time to live en millisecondes
    staleWhileRevalidate?: boolean
  } = {}
) {
  const { ttl = 5 * 60 * 1000, staleWhileRevalidate = true } = options // 5 minutes par défaut
  
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const [lastFetch, setLastFetch] = useState<number>(0)

  const isStale = useCallback(() => {
    return Date.now() - lastFetch > ttl
  }, [lastFetch, ttl])

  const fetchData = useCallback(async (force = false) => {
    if (!force && data && !isStale()) {
      return data
    }

    if (staleWhileRevalidate && data && isStale()) {
      // Retourner les données stale immédiatement et revalider en arrière-plan
      fetchData(true).catch(console.error)
      return data
    }

    setLoading(true)
    setError(null)

    try {
      const result = await fetcher()
      setData(result)
      setLastFetch(Date.now())
      
      // Stocker en localStorage pour persistance
      localStorage.setItem(`cache_${key}`, JSON.stringify({
        data: result,
        timestamp: Date.now()
      }))
      
      return result
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Unknown error')
      setError(error)
      throw error
    } finally {
      setLoading(false)
    }
  }, [data, fetcher, isStale, key, staleWhileRevalidate])

  // Charger depuis le cache local au montage
  useEffect(() => {
    const cached = localStorage.getItem(`cache_${key}`)
    if (cached) {
      try {
        const { data: cachedData, timestamp } = JSON.parse(cached)
        if (Date.now() - timestamp < ttl) {
          setData(cachedData)
          setLastFetch(timestamp)
          return
        }
      } catch (error) {
        console.warn('Failed to parse cached data:', error)
      }
    }
    
    // Pas de cache valide, charger les données
    fetchData().catch(console.error)
  }, [key, ttl, fetchData])

  const invalidate = useCallback(() => {
    localStorage.removeItem(`cache_${key}`)
    setData(null)
    setLastFetch(0)
    fetchData(true).catch(console.error)
  }, [key, fetchData])

  return {
    data,
    loading,
    error,
    isStale: isStale(),
    refetch: () => fetchData(true),
    invalidate
  }
}