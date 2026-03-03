import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PlannedWorkout } from '../models/planned-workout.model';
import { CompletedWorkout } from '../models/completed-workout.model';
import { PlannedWorkoutItem } from '../models/planned-workout-item.model';

export interface ExerciseOption {
  id: number;
  description: string;
  type: string;
  equipment: string;
}

export interface PlannedWorkoutItemCreate {
  exerciseId: number;
  sequence?: number;
  repetitions?: number;
  distance?: number;
  durationSeconds?: number;
}

export interface PlannedWorkoutCreateRequest {
  description: string;
  items: PlannedWorkoutItemCreate[];
}

@Injectable({
  providedIn: 'root'
})
export class PlannedWorkoutService {

  private readonly apiUrl = `${environment.apiUrl}`;

  constructor(private readonly http: HttpClient) { }

  getAllPlannedWorkouts(): Observable<PlannedWorkout[]> {
    return this.http.get<PlannedWorkout[]>(
      `${this.apiUrl}/api/planned-workout/list`
    );
  }

  getExercises(): Observable<ExerciseOption[]> {
    return this.http.get<ExerciseOption[]>(
      `${this.apiUrl}/api/planned-workout/exercises`
    );
  }

  getWorkoutItems(plannedWorkoutId: number): Observable<PlannedWorkoutItem[]> {
    return this.http.get<PlannedWorkoutItem[]>(
      `${this.apiUrl}/api/planned-workout/${plannedWorkoutId}/items`
    );
  }

  getPlannedWorkoutById(plannedWorkoutId: number): Observable<PlannedWorkout> {
    return this.http.get<PlannedWorkout>(
      `${this.apiUrl}/api/planned-workout/${plannedWorkoutId}`
    );
  }

  createPlannedWorkout(payload: PlannedWorkoutCreateRequest): Observable<PlannedWorkout> {
    return this.http.post<PlannedWorkout>(
      `${this.apiUrl}/api/planned-workout`,
      payload
    );
  }

  updatePlannedWorkout(plannedWorkoutId: number, payload: PlannedWorkoutCreateRequest): Observable<PlannedWorkout> {
    return this.http.put<PlannedWorkout>(
      `${this.apiUrl}/api/planned-workout/${plannedWorkoutId}`,
      payload
    );
  }

  deletePlannedWorkout(plannedWorkoutId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/api/planned-workout/${plannedWorkoutId}`
    );
  }

  start(plannedWorkoutId: number): Observable<CompletedWorkout> {
    return this.http.post<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/start/${plannedWorkoutId}`,
      {}
    );
  }

  startManual(description?: string): Observable<CompletedWorkout> {
    return this.http.post<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/start`,
      { description: description ?? 'Treino Livre' }
    );
  }
}
