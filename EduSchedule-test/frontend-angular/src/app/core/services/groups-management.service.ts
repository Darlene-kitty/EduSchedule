import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { StorageService } from './storage.service';

export interface StudentGroup {
  id: number;
  name: string;
  level: string;
  promotion: string;
  capacity: number;
  enrolled: number;
  courses: string[];
  responsible: string;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class GroupsManagementService {
  private storageService = inject(StorageService);
  private groupsSubject = new BehaviorSubject<StudentGroup[]>([]);
  groups$ = this.groupsSubject.asObservable();

  constructor() {
    this.loadGroupsFromStorage();
  }

  private loadGroupsFromStorage(): void {
    const groups = this.storageService.getItem<StudentGroup[]>('groupsList') || this.getDefaultGroups();
    this.groupsSubject.next(groups);
  }

  private getDefaultGroups(): StudentGroup[] {
    return [
      { id: 1, name: 'L1-G1', level: 'L1', promotion: 'Licence 1', capacity: 30, enrolled: 28, courses: ['INFO101', 'MATH101', 'PHYS101'], responsible: 'Dr. Martin Dupont', createdAt: '2024-01-10' },
      { id: 2, name: 'L1-G2', level: 'L1', promotion: 'Licence 1', capacity: 30, enrolled: 30, courses: ['INFO101', 'MATH101'], responsible: 'Prof. Jean Moreau', createdAt: '2024-01-10' },
      { id: 3, name: 'L2-G1', level: 'L2', promotion: 'Licence 2', capacity: 28, enrolled: 25, courses: ['CHEM205', 'MATH201', 'PHYS201'], responsible: 'Dr. Claire Dubois', createdAt: '2024-01-10' },
      { id: 4, name: 'L2-G2', level: 'L2', promotion: 'Licence 2', capacity: 28, enrolled: 22, courses: ['CHEM205'], responsible: 'Dr. Claire Dubois', createdAt: '2024-01-10' },
      { id: 5, name: 'L3-G1', level: 'L3', promotion: 'Licence 3', capacity: 30, enrolled: 27, courses: ['MATH301', 'STAT301', 'INFO301'], responsible: 'Dr. Martin Dupont', createdAt: '2024-01-10' },
      { id: 6, name: 'L3-G2', level: 'L3', promotion: 'Licence 3', capacity: 30, enrolled: 26, courses: ['MATH301', 'STAT301'], responsible: 'Dr. Marie Blanc', createdAt: '2024-01-10' },
      { id: 7, name: 'M1-G1', level: 'M1', promotion: 'Master 1',  capacity: 25, enrolled: 20, courses: ['PHYS402', 'MATH402'], responsible: 'Prof. Sophie Bernard', createdAt: '2024-01-10' },
      { id: 8, name: 'M1-G2', level: 'M1', promotion: 'Master 1',  capacity: 25, enrolled: 18, courses: ['MATH402'], responsible: 'Dr. Martin Dupont', createdAt: '2024-01-10' }
    ];
  }

  private saveGroups(groups: StudentGroup[]): void {
    this.storageService.setItem('groupsList', groups);
    this.groupsSubject.next(groups);
  }

  getGroups(): Observable<StudentGroup[]> {
    return this.groups$;
  }

  getGroupById(id: number): StudentGroup | undefined {
    return this.groupsSubject.value.find(g => g.id === id);
  }

  addGroup(group: Omit<StudentGroup, 'id' | 'createdAt'>): Observable<StudentGroup> {
    return new Observable(observer => {
      setTimeout(() => {
        const groups = this.groupsSubject.value;
        const newGroup: StudentGroup = {
          ...group,
          id: Date.now(),
          createdAt: new Date().toISOString().split('T')[0]
        };
        
        const updatedGroups = [...groups, newGroup];
        this.saveGroups(updatedGroups);
        
        observer.next(newGroup);
        observer.complete();
      }, 300);
    });
  }

  updateGroup(id: number, groupData: Partial<StudentGroup>): Observable<StudentGroup> {
    return new Observable(observer => {
      setTimeout(() => {
        const groups = this.groupsSubject.value;
        const index = groups.findIndex(g => g.id === id);
        
        if (index !== -1) {
          const updatedGroup = { ...groups[index], ...groupData };
          const updatedGroups = [...groups];
          updatedGroups[index] = updatedGroup;
          
          this.saveGroups(updatedGroups);
          observer.next(updatedGroup);
        } else {
          observer.error(new Error('Groupe non trouvé'));
        }
        
        observer.complete();
      }, 300);
    });
  }

  deleteGroup(id: number): Observable<void> {
    return new Observable(observer => {
      setTimeout(() => {
        const groups = this.groupsSubject.value;
        const updatedGroups = groups.filter(g => g.id !== id);
        
        this.saveGroups(updatedGroups);
        observer.next();
        observer.complete();
      }, 300);
    });
  }

  searchGroups(query: string): StudentGroup[] {
    const groups = this.groupsSubject.value;
    const lowerQuery = query.toLowerCase();
    
    return groups.filter(group => 
      group.name.toLowerCase().includes(lowerQuery) ||
      group.promotion.toLowerCase().includes(lowerQuery) ||
      group.responsible.toLowerCase().includes(lowerQuery)
    );
  }
}
