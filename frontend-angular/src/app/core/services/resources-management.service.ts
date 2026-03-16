import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface Resource {
  id: number;
  name: string;
  type: 'equipment' | 'material' | 'software' | 'other';
  quantity: number;
  available: number;
  location: string;
  description?: string;
  status?: 'available' | 'limited' | 'unavailable';
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ResourcesManagementService {
  private storageService = inject(StorageService);
  private resourcesSubject = new BehaviorSubject<Resource[]>([]);
  resources$ = this.resourcesSubject.asObservable();

  constructor() {
    this.loadResourcesFromStorage();
  }

  private loadResourcesFromStorage(): void {
    const resources = this.storageService.getItem<Resource[]>('resourcesList') || this.getDefaultResources();
    this.resourcesSubject.next(resources);
  }

  private getDefaultResources(): Resource[] {
    return [
      {
        id: 1,
        name: 'Projecteur',
        type: 'equipment',
        quantity: 15,
        available: 12,
        location: 'Magasin A',
        description: 'Projecteurs HD pour salles de cours',
        status: 'available',
        createdAt: '2024-01-10'
      },
      {
        id: 2,
        name: 'Ordinateur portable',
        type: 'equipment',
        quantity: 30,
        available: 25,
        location: 'Magasin B',
        description: 'Ordinateurs pour étudiants',
        status: 'available',
        createdAt: '2024-01-10'
      },
      {
        id: 3,
        name: 'Microscope',
        type: 'equipment',
        quantity: 10,
        available: 8,
        location: 'Laboratoire',
        description: 'Microscopes optiques',
        status: 'available',
        createdAt: '2024-01-10'
      }
    ];
  }

  private saveResources(resources: Resource[]): void {
    this.storageService.setItem('resourcesList', resources);
    this.resourcesSubject.next(resources);
  }

  getResources(): Observable<Resource[]> {
    return this.resources$;
  }

  getResourceById(id: number): Resource | undefined {
    return this.resourcesSubject.value.find(r => r.id === id);
  }

  addResource(resource: Omit<Resource, 'id' | 'createdAt'>): Observable<Resource> {
    return new Observable(observer => {
      setTimeout(() => {
        const resources = this.resourcesSubject.value;
        const newResource: Resource = {
          ...resource,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0],
          status: this.calculateStatus(resource.available, resource.quantity)
        };
        
        const updatedResources = [...resources, newResource];
        this.saveResources(updatedResources);
        
        observer.next(newResource);
        observer.complete();
      }, 300);
    });
  }

  updateResource(id: number, resourceData: Partial<Resource>): Observable<Resource> {
    return new Observable(observer => {
      setTimeout(() => {
        const resources = this.resourcesSubject.value;
        const index = resources.findIndex(r => r.id === id);
        
        if (index !== -1) {
          const updatedResource = { ...resources[index], ...resourceData };
          
          // Recalculer le statut si quantity ou available change
          if (resourceData.available !== undefined || resourceData.quantity !== undefined) {
            updatedResource.status = this.calculateStatus(
              updatedResource.available,
              updatedResource.quantity
            );
          }
          
          const updatedResources = [...resources];
          updatedResources[index] = updatedResource;
          
          this.saveResources(updatedResources);
          observer.next(updatedResource);
        } else {
          observer.error(new Error('Ressource non trouvée'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteResource(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const resources = this.resourcesSubject.value;
        const updatedResources = resources.filter(r => r.id !== id);
        
        this.saveResources(updatedResources);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  private calculateStatus(available: number, quantity: number): 'available' | 'limited' | 'unavailable' {
    if (available === 0) return 'unavailable';
    if (available < quantity * 0.3) return 'limited';
    return 'available';
  }

  borrowResource(id: number, count: number = 1): Observable<Resource> {
    const resource = this.getResourceById(id);
    if (!resource) {
      return new Observable(observer => observer.error(new Error('Ressource non trouvée')));
    }
    
    if (resource.available < count) {
      return new Observable(observer => observer.error(new Error('Quantité insuffisante')));
    }
    
    return this.updateResource(id, { available: resource.available - count });
  }

  returnResource(id: number, count: number = 1): Observable<Resource> {
    const resource = this.getResourceById(id);
    if (!resource) {
      return new Observable(observer => observer.error(new Error('Ressource non trouvée')));
    }
    
    const newAvailable = Math.min(resource.available + count, resource.quantity);
    return this.updateResource(id, { available: newAvailable });
  }
}
