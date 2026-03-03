import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { take } from 'rxjs/operators';
import { finalize, timeout } from 'rxjs';
import { CalendarBarComponent } from '../calendar-bar/calendar-bar.component';
import { PlannedWorkout } from '../../../models/planned-workout.model';
import { PlannedWorkoutService } from '../../../services/planned-workout.service';
import { PlannedWorkoutItem } from '../../../models/planned-workout-item.model';
import { CompletedWorkoutService } from '../../../services/completed-workout.service';

@Component({
  selector: 'app-train',
  imports: [CalendarBarComponent, CommonModule, RouterModule],
  standalone: true,
  templateUrl: './train.component.html',
  styleUrls: ['./train.component.css'],
})
export class TrainComponent implements OnInit {
  plannedWorkouts: PlannedWorkout[] = [];
  loading = true;
  errorMessage = '';
  startingWorkoutId: number | null = null;
  expandedWorkoutId: number | null = null;
  loadingItemsWorkoutId: number | null = null;
  workoutItems: Record<number, PlannedWorkoutItem[]> = {};

  constructor(
    private readonly plannedWorkoutService: PlannedWorkoutService,
    private readonly completedWorkoutService: CompletedWorkoutService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly zone: NgZone
  ) {}

  ngOnInit(): void {
    this.loadPlannedWorkouts();
  }

  loadPlannedWorkouts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.plannedWorkoutService
      .getAllPlannedWorkouts()
      .pipe(take(1))
      .subscribe({
        next: (workouts) => {
          this.zone.run(() => {
            this.plannedWorkouts = workouts ?? [];
            this.loading = false;
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.loading = false;
            this.errorMessage =
              error.status === 401
                ? 'Sua sessao expirou. Faca login novamente.'
                : 'Nao foi possivel carregar seus treinos agora.';
            this.cdr.detectChanges();
          });
        },
      });
  }

  toggleWorkoutDetails(workoutId: number): void {
    if (this.expandedWorkoutId === workoutId) {
      this.expandedWorkoutId = null;
      return;
    }

    this.expandedWorkoutId = workoutId;

    if (this.workoutItems[workoutId]) {
      return;
    }

    this.loadingItemsWorkoutId = workoutId;
    this.plannedWorkoutService
      .getWorkoutItems(workoutId)
      .pipe(
        take(1),
        timeout(10000),
        finalize(() => {
          this.zone.run(() => {
            this.loadingItemsWorkoutId = null;
            this.cdr.detectChanges();
          });
        })
      )
      .subscribe({
        next: (items) => {
          this.zone.run(() => {
            this.workoutItems[workoutId] = items ?? [];
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.errorMessage = 'Nao foi possivel carregar os itens do treino.';
            this.cdr.detectChanges();
          });
        }
      });
  }

  startWorkout(plannedWorkoutId: number): void {
    if (this.startingWorkoutId !== null) {
      return;
    }

    this.startingWorkoutId = plannedWorkoutId;
    this.errorMessage = '';

    this.completedWorkoutService
      .findInProgress()
      .pipe(take(1))
      .subscribe({
        next: (inProgress) => {
          if (inProgress) {
            this.startingWorkoutId = null;
            this.router.navigate(['/completed-workout', inProgress.id], {
              queryParams: { alreadyInProgress: '1' }
            });
            return;
          }

          this.plannedWorkoutService
            .start(plannedWorkoutId)
            .pipe(take(1))
            .subscribe({
              next: (completed) => {
                this.router.navigate(['/completed-workout', completed.id]);
              },
              error: () => {
                this.errorMessage = 'Nao foi possivel iniciar o treino.';
                this.startingWorkoutId = null;
              },
            });
        },
        error: () => {
          this.errorMessage = 'Nao foi possivel validar treino em andamento.';
          this.startingWorkoutId = null;
        }
      });
  }

  startFirstWorkout(): void {
    const firstWorkout = this.plannedWorkouts[0];
    if (!firstWorkout) {
      return;
    }

    this.startWorkout(firstWorkout.id);
  }

  editPlannedWorkout(plannedWorkoutId: number): void {
    this.router.navigate(['/planned-workout', plannedWorkoutId, 'edit']);
  }

  deletePlannedWorkout(plannedWorkoutId: number): void {
    const shouldDelete = window.confirm('Deseja remover este treino planejado?');
    if (!shouldDelete) {
      return;
    }

    this.plannedWorkoutService
      .deletePlannedWorkout(plannedWorkoutId)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.plannedWorkouts = this.plannedWorkouts.filter((w) => w.id !== plannedWorkoutId);
          if (this.expandedWorkoutId === plannedWorkoutId) {
            this.expandedWorkoutId = null;
          }
          delete this.workoutItems[plannedWorkoutId];
          this.cdr.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Nao foi possivel remover o treino planejado.';
        }
      });
  }

  startManualWorkout(): void {
    if (this.startingWorkoutId !== null) {
      return;
    }

    this.startingWorkoutId = -1;
    this.errorMessage = '';

    this.completedWorkoutService
      .findInProgress()
      .pipe(take(1))
      .subscribe({
        next: (inProgress) => {
          if (inProgress) {
            this.startingWorkoutId = null;
            this.router.navigate(['/completed-workout', inProgress.id], {
              queryParams: { alreadyInProgress: '1' }
            });
            return;
          }

          this.plannedWorkoutService
            .startManual('Treino Livre')
            .pipe(take(1))
            .subscribe({
              next: (completed) => {
                this.router.navigate(['/completed-workout', completed.id]);
              },
              error: () => {
                this.errorMessage = 'Nao foi possivel iniciar treino livre.';
                this.startingWorkoutId = null;
              }
            });
        },
        error: () => {
          this.errorMessage = 'Nao foi possivel validar treino em andamento.';
          this.startingWorkoutId = null;
        }
      });
  }
}
