import { Injectable, inject } from '@angular/core';
import { StorageService } from './storage.service';

export interface CacheConfig {
  key: string;
  ttl?: number; // Time to live en millisecondes
}

export interface CacheItem<T> {
  data: T;
  timestamp: number;
  ttl?: number;
}

@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private storageService = inject(StorageService);

  /**
   * Sauvegarder des données dans le cache
   */
  set<T>(key: string, data: T, ttl?: number): void {
    const cacheItem: CacheItem<T> = {
      data,
      timestamp: Date.now(),
      ttl
    };
    this.storageService.setItem(`cache_${key}`, cacheItem);
  }

  /**
   * Récupérer des données du cache
   */
  get<T>(key: string): T | null {
    const cacheItem = this.storageService.getItem<CacheItem<T>>(`cache_${key}`);
    
    if (!cacheItem) {
      return null;
    }

    // Vérifier si le cache a expiré
    if (cacheItem.ttl) {
      const isExpired = Date.now() - cacheItem.timestamp > cacheItem.ttl;
      if (isExpired) {
        this.remove(key);
        return null;
      }
    }

    return cacheItem.data;
  }

  /**
   * Supprimer un élément du cache
   */
  remove(key: string): void {
    this.storageService.removeItem(`cache_${key}`);
  }

  /**
   * Vérifier si une clé existe dans le cache et est valide
   */
  has(key: string): boolean {
    return this.get(key) !== null;
  }

  /**
   * Nettoyer tous les caches expirés
   */
  clearExpired(): void {
    // Cette méthode nécessiterait d'itérer sur toutes les clés du localStorage
    // Pour simplifier, on peut l'implémenter plus tard si nécessaire
  }

  /**
   * Vider tout le cache
   */
  clearAll(): void {
    // Supprimer tous les éléments qui commencent par 'cache_'
    if (typeof window !== 'undefined' && window.localStorage) {
      const keys = Object.keys(localStorage);
      keys.forEach(key => {
        if (key.startsWith('cache_')) {
          localStorage.removeItem(key);
        }
      });
    }
  }
}
