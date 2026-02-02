// Optimisations d'interface utilisateur pour EduSchedule
import { useCallback, useMemo, useRef, useEffect } from 'react'

// Debounce hook pour les recherches
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value)

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value)
    }, delay)

    return () => {
      clearTimeout(handler)
    }
  }, [value, delay])

  return debouncedValue
}

// Throttle hook pour les événements fréquents
export function useThrottle<T extends (...args: any[]) => any>(
  callback: T,
  delay: number
): T {
  const lastRun = useRef(Date.now())

  return useCallback(
    ((...args) => {
      if (Date.now() - lastRun.current >= delay) {
        callback(...args)
        lastRun.current = Date.now()
      }
    }) as T,
    [callback, delay]
  )
}

// Hook pour la virtualisation des listes longues
export function useVirtualList<T>(
  items: T[],
  itemHeight: number,
  containerHeight: number
) {
  const [scrollTop, setScrollTop] = useState(0)

  const visibleItems = useMemo(() => {
    const startIndex = Math.floor(scrollTop / itemHeight)
    const endIndex = Math.min(
      startIndex + Math.ceil(containerHeight / itemHeight) + 1,
      items.length
    )

    return {
      startIndex,
      endIndex,
      items: items.slice(startIndex, endIndex),
      totalHeight: items.length * itemHeight,
      offsetY: startIndex * itemHeight
    }
  }, [items, itemHeight, containerHeight, scrollTop])

  const onScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
    setScrollTop(e.currentTarget.scrollTop)
  }, [])

  return { visibleItems, onScroll }
}

// Hook pour le lazy loading des images
export function useLazyImage(src: string, placeholder: string = '') {
  const [imageSrc, setImageSrc] = useState(placeholder)
  const [isLoaded, setIsLoaded] = useState(false)
  const imgRef = useRef<HTMLImageElement>(null)

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          const img = new Image()
          img.onload = () => {
            setImageSrc(src)
            setIsLoaded(true)
          }
          img.src = src
          observer.disconnect()
        }
      },
      { threshold: 0.1 }
    )

    if (imgRef.current) {
      observer.observe(imgRef.current)
    }

    return () => observer.disconnect()
  }, [src])

  return { imageSrc, isLoaded, imgRef }
}

// Hook pour la mémorisation des calculs coûteux
export function useExpensiveCalculation<T>(
  calculate: () => T,
  dependencies: any[]
): T {
  return useMemo(calculate, dependencies)
}

// Hook pour la gestion des états de chargement
export function useAsyncState<T>(
  asyncFunction: () => Promise<T>,
  dependencies: any[] = []
) {
  const [state, setState] = useState<{
    data: T | null
    loading: boolean
    error: Error | null
  }>({
    data: null,
    loading: true,
    error: null
  })

  useEffect(() => {
    let cancelled = false

    const execute = async () => {
      setState(prev => ({ ...prev, loading: true, error: null }))
      
      try {
        const result = await asyncFunction()
        if (!cancelled) {
          setState({ data: result, loading: false, error: null })
        }
      } catch (error) {
        if (!cancelled) {
          setState({ 
            data: null, 
            loading: false, 
            error: error instanceof Error ? error : new Error('Unknown error') 
          })
        }
      }
    }

    execute()

    return () => {
      cancelled = true
    }
  }, dependencies)

  return state
}

// Utilitaires de performance
export const performanceUtils = {
  // Mesurer le temps d'exécution d'une fonction
  measureTime: <T>(fn: () => T, label: string): T => {
    const start = performance.now()
    const result = fn()
    const end = performance.now()
    console.log(`${label}: ${end - start}ms`)
    return result
  },

  // Mesurer le temps d'exécution d'une fonction async
  measureTimeAsync: async <T>(fn: () => Promise<T>, label: string): Promise<T> => {
    const start = performance.now()
    const result = await fn()
    const end = performance.now()
    console.log(`${label}: ${end - start}ms`)
    return result
  },

  // Créer un batch processor pour les opérations groupées
  createBatchProcessor: <T>(
    processor: (items: T[]) => Promise<void>,
    batchSize: number = 10,
    delay: number = 100
  ) => {
    let batch: T[] = []
    let timeoutId: NodeJS.Timeout | null = null

    return (item: T) => {
      batch.push(item)

      if (timeoutId) {
        clearTimeout(timeoutId)
      }

      if (batch.length >= batchSize) {
        processor([...batch])
        batch = []
      } else {
        timeoutId = setTimeout(() => {
          if (batch.length > 0) {
            processor([...batch])
            batch = []
          }
        }, delay)
      }
    }
  },

  // Optimiser les re-renders avec React.memo
  memoizeComponent: <P extends object>(
    Component: React.ComponentType<P>,
    areEqual?: (prevProps: P, nextProps: P) => boolean
  ) => {
    return React.memo(Component, areEqual)
  },

  // Créer un sélecteur mémorisé pour Redux/Zustand
  createMemoizedSelector: <T, R>(
    selector: (state: T) => R,
    equalityFn?: (a: R, b: R) => boolean
  ) => {
    let lastState: T
    let lastResult: R

    return (state: T): R => {
      if (state !== lastState) {
        const newResult = selector(state)
        if (!equalityFn || !equalityFn(lastResult, newResult)) {
          lastResult = newResult
        }
        lastState = state
      }
      return lastResult
    }
  }
}

// Composant optimisé pour les listes virtualisées
import React, { useState } from 'react'

interface VirtualListProps<T> {
  items: T[]
  itemHeight: number
  containerHeight: number
  renderItem: (item: T, index: number) => React.ReactNode
  className?: string
}

export function VirtualList<T>({
  items,
  itemHeight,
  containerHeight,
  renderItem,
  className = ''
}: VirtualListProps<T>) {
  const { visibleItems, onScroll } = useVirtualList(items, itemHeight, containerHeight)

  return (
    <div
      className={`overflow-auto ${className}`}
      style={{ height: containerHeight }}
      onScroll={onScroll}
    >
      <div style={{ height: visibleItems.totalHeight, position: 'relative' }}>
        <div
          style={{
            transform: `translateY(${visibleItems.offsetY}px)`,
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0
          }}
        >
          {visibleItems.items.map((item, index) =>
            renderItem(item, visibleItems.startIndex + index)
          )}
        </div>
      </div>
    </div>
  )
}

// Composant de chargement optimisé
interface OptimizedLoaderProps {
  loading: boolean
  error?: string | null
  children: React.ReactNode
  fallback?: React.ReactNode
  errorFallback?: React.ReactNode
}

export function OptimizedLoader({
  loading,
  error,
  children,
  fallback,
  errorFallback
}: OptimizedLoaderProps) {
  if (error) {
    return errorFallback || <div className="text-red-500">Erreur: {error}</div>
  }

  if (loading) {
    return fallback || <div className="animate-pulse">Chargement...</div>
  }

  return <>{children}</>
}

// Hook pour la gestion des erreurs globales
export function useErrorBoundary() {
  const [error, setError] = useState<Error | null>(null)

  const resetError = useCallback(() => {
    setError(null)
  }, [])

  const captureError = useCallback((error: Error) => {
    setError(error)
    console.error('Error captured:', error)
  }, [])

  useEffect(() => {
    if (error) {
      // Optionnel: envoyer l'erreur à un service de monitoring
      // sendErrorToMonitoring(error)
    }
  }, [error])

  return { error, resetError, captureError }
}

// Utilitaires de cache pour les composants
export const componentCache = new Map<string, React.ComponentType<any>>()

export function getCachedComponent<P>(
  key: string,
  factory: () => React.ComponentType<P>
): React.ComponentType<P> {
  if (!componentCache.has(key)) {
    componentCache.set(key, factory())
  }
  return componentCache.get(key)!
}