import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EquipmentCatalogItem {
  id: number;
  description: string;
}

export interface MuscleCatalogItem {
  id: number;
  description: string;
}

export interface ExerciseCatalogItem {
  id: number;
  description: string;
  type: 'STRENGTH' | 'CARDIO';
  equipmentId: number;
  equipmentDescription: string;
}

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private readonly apiUrl = `${environment.apiUrl}/api/catalog`;

  constructor(private readonly http: HttpClient) {}

  listEquipments(): Observable<EquipmentCatalogItem[]> {
    return this.http.get<EquipmentCatalogItem[]>(`${this.apiUrl}/equipments`);
  }

  createEquipment(description: string): Observable<EquipmentCatalogItem> {
    return this.http.post<EquipmentCatalogItem>(`${this.apiUrl}/equipments`, { description });
  }

  updateEquipment(id: number, description: string): Observable<EquipmentCatalogItem> {
    return this.http.put<EquipmentCatalogItem>(`${this.apiUrl}/equipments/${id}`, { description });
  }

  deleteEquipment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/equipments/${id}`);
  }

  listMuscles(): Observable<MuscleCatalogItem[]> {
    return this.http.get<MuscleCatalogItem[]>(`${this.apiUrl}/muscles`);
  }

  createMuscle(description: string): Observable<MuscleCatalogItem> {
    return this.http.post<MuscleCatalogItem>(`${this.apiUrl}/muscles`, { description });
  }

  updateMuscle(id: number, description: string): Observable<MuscleCatalogItem> {
    return this.http.put<MuscleCatalogItem>(`${this.apiUrl}/muscles/${id}`, { description });
  }

  deleteMuscle(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/muscles/${id}`);
  }

  listExercises(): Observable<ExerciseCatalogItem[]> {
    return this.http.get<ExerciseCatalogItem[]>(`${this.apiUrl}/exercises`);
  }

  createExercise(payload: {
    description: string;
    type: 'STRENGTH' | 'CARDIO';
    equipmentId: number;
  }): Observable<ExerciseCatalogItem> {
    return this.http.post<ExerciseCatalogItem>(`${this.apiUrl}/exercises`, payload);
  }

  updateExercise(
    id: number,
    payload: { description: string; type: 'STRENGTH' | 'CARDIO'; equipmentId: number }
  ): Observable<ExerciseCatalogItem> {
    return this.http.put<ExerciseCatalogItem>(`${this.apiUrl}/exercises/${id}`, payload);
  }

  deleteExercise(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/exercises/${id}`);
  }
}

