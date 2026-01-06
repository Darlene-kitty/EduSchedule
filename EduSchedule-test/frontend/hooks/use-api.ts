/**
 * Hook personnalisé pour gérer les appels API avec état de chargement et gestion d'erreurs
 */

import { useState, useCallback } from 'react'
import { ApiError } from '@/lib/api-client'
import { useToast } from './use-toast'

interface UseApiOptions {
  onSuccess?: (data: any) => void
  onError?: (error: Error) => void
  showSuccessToast?: boolean
  showErrorToast?: boolean
  successMessage?: string
}

export function useApi<T = any>(
  apiFunction: (...args: any[]) => Promise<T>,
  options: UseApiOptions = {}
) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<Error | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const { toast } = useToast()

  const {
    onSuccess,
    onError,
    showSuccessToast = false,
    showErrorToast = true,
    successMessage = 'Opération réussie',
  } = options

  const execute = useCallback(
    async (...args: any[]) => {
      setIsLoading(true)
      setError(null)

      try {
        const result = await apiFunction(...args)
        setData(result)

        if (showSuccessToast) {
          toast({
            title: 'Succès',
            description: successMessage,
            variant: 'default',
          })
        }

        if (onSuccess) {
          onSuccess(result)
        }

        return result
      } catch (err) {
        const error = err instanceof ApiError 
          ? new Error(err.message)
          : err instanceof Error
          ? err
          : new Error('Une erreur est survenue')

        setError(error)

        if (showErrorToast) {
          toast({
            title: 'Erreur',
            description: error.message,
            variant: 'destructive',
          })
        }

        if (onError) {
          onError(error)
        }

        throw error
      } finally {
        setIsLoading(false)
      }
    },
    [apiFunction, onSuccess, onError, showSuccessToast, showErrorToast, successMessage, toast]
  )

  const reset = useCallback(() => {
    setData(null)
    setError(null)
    setIsLoading(false)
  }, [])

  return {
    data,
    error,
    isLoading,
    execute,
    reset,
  }
}

/**
 * Hook pour les mutations (POST, PUT, DELETE)
 */
export function useMutation<T = any>(
  apiFunction: (...args: any[]) => Promise<T>,
  options: UseApiOptions = {}
) {
  return useApi(apiFunction, {
    showSuccessToast: true,
    showErrorToast: true,
    ...options,
  })
}

/**
 * Hook pour les requêtes (GET)
 */
export function useQuery<T = any>(
  apiFunction: (...args: any[]) => Promise<T>,
  options: UseApiOptions = {}
) {
  return useApi(apiFunction, {
    showSuccessToast: false,
    showErrorToast: true,
    ...options,
  })
}
