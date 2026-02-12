// Cache optimisé pour les disponibilités des enseignants
import { TeacherAvailability, DayOfWeek } from "@/lib/api/teacher-availability"

interface CacheEntry<T> {
  data: T
  timestamp: number
  ttl: number // Time to live en millisecondes
}

class AvailabilityCache {
  private cache = new Map<string, CacheEntry<any>>()
  private readonly DEFAULT_TTL = 5 * 60 * 1000 // 5 minutes

  // Générer une clé de cache
  private generateKey(prefix: string, ...params: (string | number)[]): string {
    return `${prefix}:${params.join(':')}`
  }

  // Vérifier si une entrée est expirée
  private isExpired(entry: CacheEntry<any>): boolean {
    return Date.now() - entry.timestamp > entry.ttl
  }

  // Obtenir une entrée du cache
  private get<T>(key: string): T | null {
    const entry = this.cache.get(key)
    if (!entry || this.isExpired(entry)) {
      this.cache.delete(key)
      return null
    }
    return entry.data
  }

  // Définir une entrée dans le cache
  private set<T>(key: string, data: T, ttl: number = this.DEFAULT_TTL): void {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  // Supprimer une entrée du cache
  private delete(key: string): void {
    this.cache.delete(key)
  }

  // Supprimer toutes les entrées liées à un enseignant
  private deleteTeacherEntries(teacherId: number): void {
    const keysToDelete: string[] = []
    for (const key of this.cache.keys()) {
      if (key.includes(`teacher:${teacherId}`)) {
        keysToDelete.push(key)
      }
    }
    keysToDelete.forEach(key => this.cache.delete(key))
  }

  // === API publique ===

  // Cache des disponibilités d'un enseignant
  getTeacherAvailabilities(teacherId: number): TeacherAvailability[] | null {
    const key = this.generateKey('teacher', teacherId, 'availabilities')
    return this.get<TeacherAvailability[]>(key)
  }

  setTeacherAvailabilities(teacherId: number, availabilities: TeacherAvailability[]): void {
    const key = this.generateKey('teacher', teacherId, 'availabilities')
    this.set(key, availabilities)
  }

  // Cache des créneaux disponibles pour un jour
  getAvailableSlots(teacherId: number, dayOfWeek: DayOfWeek): any[] | null {
    const key = this.generateKey('teacher', teacherId, 'slots', dayOfWeek)
    return this.get<any[]>(key)
  }

  setAvailableSlots(teacherId: number, dayOfWeek: DayOfWeek, slots: any[]): void {
    const key = this.generateKey('teacher', teacherId, 'slots', dayOfWeek)
    this.set(key, slots)
  }

  // Cache de vérification de disponibilité
  getAvailabilityCheck(teacherId: number, startTime: string, endTime: string): boolean | null {
    const key = this.generateKey('teacher', teacherId, 'check', startTime, endTime)
    return this.get<boolean>(key)
  }

  setAvailabilityCheck(teacherId: number, startTime: string, endTime: string, available: boolean): void {
    const key = this.generateKey('teacher', teacherId, 'check', startTime, endTime)
    this.set(key, available, 2 * 60 * 1000) // TTL plus court pour les vérifications
  }

  // Cache des heures totales
  getTotalHours(teacherId: number): number | null {
    const key = this.generateKey('teacher', teacherId, 'total-hours')
    return this.get<number>(key)
  }

  setTotalHours(teacherId: number, hours: number): void {
    const key = this.generateKey('teacher', teacherId, 'total-hours')
    this.set(key, hours)
  }

  // Cache des créneaux préférés
  getPreferredSlots(teacherId: number): TeacherAvailability[] | null {
    const key = this.generateKey('teacher', teacherId, 'preferred')
    return this.get<TeacherAvailability[]>(key)
  }

  setPreferredSlots(teacherId: number, slots: TeacherAvailability[]): void {
    const key = this.generateKey('teacher', teacherId, 'preferred')
    this.set(key, slots)
  }

  // Invalidation du cache
  invalidateTeacher(teacherId: number): void {
    this.deleteTeacherEntries(teacherId)
  }

  invalidateAll(): void {
    this.cache.clear()
  }

  // Nettoyage automatique des entrées expirées
  cleanup(): void {
    const now = Date.now()
    const keysToDelete: string[] = []
    
    for (const [key, entry] of this.cache.entries()) {
      if (now - entry.timestamp > entry.ttl) {
        keysToDelete.push(key)
      }
    }
    
    keysToDelete.forEach(key => this.cache.delete(key))
  }

  // Statistiques du cache
  getStats(): { size: number; hitRate: number } {
    return {
      size: this.cache.size,
      hitRate: 0 // TODO: Implémenter le tracking des hits/misses
    }
  }
}

// Instance singleton
export const availabilityCache = new AvailabilityCache()

// Nettoyage automatique toutes les 10 minutes
setInterval(() => {
  availabilityCache.cleanup()
}, 10 * 60 * 1000)

// Hook React pour utiliser le cache
import { useState, useEffect } from 'react'

export function useCachedAvailabilities(teacherId: number) {
  const [availabilities, setAvailabilities] = useState<TeacherAvailability[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loadAvailabilities = async () => {
      try {
        setLoading(true)
        setError(null)

        // Vérifier le cache d'abord
        const cached = availabilityCache.getTeacherAvailabilities(teacherId)
        if (cached) {
          setAvailabilities(cached)
          setLoading(false)
          return
        }

        // Charger depuis l'API si pas en cache
        const { teacherAvailabilityApi } = await import('@/lib/api/teacher-availability')
        const data = await teacherAvailabilityApi.getTeacherAvailabilities(teacherId)
        
        // Mettre en cache
        availabilityCache.setTeacherAvailabilities(teacherId, data)
        setAvailabilities(data)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erreur de chargement')
      } finally {
        setLoading(false)
      }
    }

    if (teacherId) {
      loadAvailabilities()
    }
  }, [teacherId])

  const invalidateCache = () => {
    availabilityCache.invalidateTeacher(teacherId)
  }

  return { availabilities, loading, error, invalidateCache }
}