import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  ExerciseOption,
  PlannedWorkoutCreateRequest,
  PlannedWorkoutService
} from '../../../services/planned-workout.service';
import { take } from 'rxjs/operators';
import { finalize, timeout } from 'rxjs';

interface WorkoutItemForm {
  exerciseId: number | null;
  repetitions: number | null;
  distance: number | null;
  durationSeconds: number | null;
}

@Component({
  selector: 'app-planned-workout-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './planned-workout-create.component.html',
  styleUrls: ['./planned-workout-create.component.css']
})
export class PlannedWorkoutCreateComponent implements OnInit {
  plannedWorkoutId: number | null = null;
  isEditMode = false;
  description = '';
  exercises: ExerciseOption[] = [];
  items: WorkoutItemForm[] = [];
  loadingExercises = true;
  saving = false;
  errorMessage = '';

  constructor(
    private readonly plannedWorkoutService: PlannedWorkoutService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly zone: NgZone
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isNaN(id) && id > 0) {
      this.plannedWorkoutId = id;
      this.isEditMode = true;
    }

    if (!this.isEditMode) {
      this.addItem();
    }

    this.loadExercises();

    if (this.isEditMode && this.plannedWorkoutId) {
      this.loadPlannedWorkout(this.plannedWorkoutId);
    }
  }

  addItem(): void {
    this.items.push({
      exerciseId: null,
      repetitions: null,
      distance: null,
      durationSeconds: null
    });
  }

  removeItem(index: number): void {
    this.items.splice(index, 1);
    if (this.items.length === 0) {
      this.addItem();
    }
  }

  loadExercises(): void {
    this.loadingExercises = true;
    this.errorMessage = '';

    this.plannedWorkoutService
      .getExercises()
      .pipe(
        take(1),
        timeout(10000),
        finalize(() => {
          this.loadingExercises = false;
        })
      )
      .subscribe({
        next: (response) => {
          this.zone.run(() => {
            this.exercises = response;
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.errorMessage = 'Nao foi possivel carregar exercicios.';
            this.cdr.detectChanges();
          });
        }
      });
  }

  save(): void {
    if (this.saving) {
      return;
    }

    if (!this.description.trim()) {
      this.errorMessage = 'Informe o nome do treino.';
      return;
    }

    const validItems = this.items.filter((item) => item.exerciseId !== null);
    if (validItems.length === 0) {
      this.errorMessage = 'Adicione pelo menos 1 exercicio.';
      return;
    }

    const payload: PlannedWorkoutCreateRequest = {
      description: this.description.trim(),
      items: validItems.map((item, index) => ({
        exerciseId: item.exerciseId as number,
        sequence: index + 1,
        repetitions: item.repetitions ?? undefined,
        distance: item.distance ?? undefined,
        durationSeconds: item.durationSeconds ?? undefined
      }))
    };

    this.saving = true;
    this.errorMessage = '';

    const request$ = this.isEditMode && this.plannedWorkoutId
      ? this.plannedWorkoutService.updatePlannedWorkout(this.plannedWorkoutId, payload)
      : this.plannedWorkoutService.createPlannedWorkout(payload);

    request$.pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.saving = false;
          this.cdr.detectChanges();
          this.router.navigate(['/train']);
        });
      },
      error: () => {
        this.zone.run(() => {
          this.saving = false;
          this.errorMessage = this.isEditMode
            ? 'Nao foi possivel atualizar o treino planejado.'
            : 'Nao foi possivel salvar o treino planejado.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  private loadPlannedWorkout(plannedWorkoutId: number): void {
    this.plannedWorkoutService
      .getPlannedWorkoutById(plannedWorkoutId)
      .pipe(take(1))
      .subscribe({
        next: (workout) => {
          this.zone.run(() => {
            this.description = workout.description ?? '';
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.errorMessage = 'Nao foi possivel carregar treino planejado.';
            this.cdr.detectChanges();
          });
        }
      });

    this.plannedWorkoutService
      .getWorkoutItems(plannedWorkoutId)
      .pipe(take(1))
      .subscribe({
        next: (items) => {
          this.zone.run(() => {
            this.items = (items ?? []).map((item) => ({
              exerciseId: item.exerciseId,
              repetitions: item.repetitions ?? null,
              distance: item.distance ?? null,
              durationSeconds: item.durationSeconds ?? null
            }));

            if (this.items.length === 0) {
              this.addItem();
            }

            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.errorMessage = 'Nao foi possivel carregar itens do treino.';
            if (this.items.length === 0) {
              this.addItem();
            }
            this.cdr.detectChanges();
          });
        }
      });
  }
}
