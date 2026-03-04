import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { CompletedWorkout } from '../models/completed-workout.model';
import { catchError, map } from 'rxjs/operators';

export interface CompletedWorkoutItemUpsertRequest {
  exerciseId?: number;
  repetitions?: number;
  weight?: number;
  distance?: number;
  durationSeconds?: number;
}

@Injectable({
  providedIn: 'root'
})
export class CompletedWorkoutService {

  private readonly apiUrl = `${environment.apiUrl}`;

  constructor(private readonly http: HttpClient) {}

  getAllCompletedWorkouts(): Observable<CompletedWorkout[]> {
    return this.http.get<CompletedWorkout[]>(
      `${this.apiUrl}/api/completed-workout/list`
    );
  }

  findInProgress(): Observable<CompletedWorkout | null> {
    return this.http.get<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/in-progress`,
      { observe: 'response' }
    ).pipe(
      map((response: HttpResponse<CompletedWorkout>) => response.body ?? null),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 204 || error.status === 404) {
          return of(null);
        }

        return throwError(() => error);
      })
    );
  }

  findById(completedWorkout: number): Observable<CompletedWorkout> {
    return this.http.get<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/${completedWorkout}`
    );
  }

  updateItem(
    completedWorkoutId: number,
    itemId: number,
    payload: CompletedWorkoutItemUpsertRequest
  ): Observable<CompletedWorkout> {
    return this.http.put<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/${completedWorkoutId}/items/${itemId}`,
      payload
    );
  }

  addItem(
    completedWorkoutId: number,
    payload: CompletedWorkoutItemUpsertRequest
  ): Observable<CompletedWorkout> {
    return this.http.post<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/${completedWorkoutId}/items`,
      payload
    );
  }

  removeItem(completedWorkoutId: number, itemId: number): Observable<CompletedWorkout> {
    return this.http.delete<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/${completedWorkoutId}/items/${itemId}`
    );
  }

  finish(completedWorkoutId: number, notes?: string): Observable<CompletedWorkout> {
    return this.http.post<CompletedWorkout>(
      `${this.apiUrl}/api/completed-workout/${completedWorkoutId}/finish`,
      { notes: notes ?? '' }
    );
  }

  cancel(completedWorkoutId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/api/completed-workout/${completedWorkoutId}/cancel`
    );
  }
}
