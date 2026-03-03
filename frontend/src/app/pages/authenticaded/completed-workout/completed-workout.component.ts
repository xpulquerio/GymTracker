import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  CompletedWorkoutItemUpsertRequest,
  CompletedWorkoutService
} from '../../../services/completed-workout.service';
import { CompletedWorkout } from '../../../models/completed-workout.model';
import { FormsModule } from '@angular/forms';
import { ExerciseOption, PlannedWorkoutService } from '../../../services/planned-workout.service';
import { take } from 'rxjs/operators';
import { finalize, timeout } from 'rxjs';

@Component({
  selector: 'app-completed-workout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './completed-workout.component.html',
  styleUrls: ['./completed-workout.component.css'],
})
export class CompletedWorkoutComponent implements OnInit {
  workout: CompletedWorkout | null = null;
  loading = true;
  saving = false;
  finishing = false;
  errorMessage = '';
  successMessage = '';
  infoMessage = '';
  notes = '';

  exercises: ExerciseOption[] = [];
  addItemForm: CompletedWorkoutItemUpsertRequest = {
    exerciseId: undefined,
    repetitions: undefined,
    weight: undefined,
    distance: undefined,
    durationSeconds: undefined,
  };

  constructor(
    public readonly completedWorkoutService: CompletedWorkoutService,
    private readonly plannedWorkoutService: PlannedWorkoutService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
    private readonly zone: NgZone
  ) {}

  private getApiErrorMessage(error: HttpErrorResponse, fallback: string): string {
    return error?.error?.error || fallback;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const alreadyInProgress = this.route.snapshot.queryParamMap.get('alreadyInProgress');
    if (alreadyInProgress === '1') {
      this.infoMessage = 'Ja existe um treino em andamento.';
    }
    this.loadWorkout(id);
    this.loadExercises();
  }

  loadWorkout(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.completedWorkoutService
      .findById(id)
      .pipe(
        take(1),
        timeout(10000),
        finalize(() => {
          this.zone.run(() => {
            this.loading = false;
            this.cdr.detectChanges();
          });
        })
      )
      .subscribe({
        next: (workout) => {
          this.zone.run(() => {
            this.workout = workout;
            this.notes = workout.notes || '';
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel carregar o treino.');
            this.cdr.detectChanges();
          });
        }
      });
  }

  loadExercises(): void {
    this.plannedWorkoutService
      .getExercises()
      .pipe(
        take(1),
        timeout(10000)
      )
      .subscribe({
        next: (list) => {
          this.zone.run(() => {
            this.exercises = list;
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel carregar lista de exercicios.');
            this.cdr.detectChanges();
          });
        },
      });
  }

  get isFinished(): boolean {
    return !!this.workout?.finishedAt;
  }

  saveItem(itemId: number, payload: CompletedWorkoutItemUpsertRequest): void {
    if (!this.workout || this.isFinished || this.saving) {
      return;
    }

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.completedWorkoutService
      .updateItem(this.workout.id, itemId, payload)
      .pipe(take(1))
      .subscribe({
        next: (updated) => {
          this.zone.run(() => {
            this.workout = updated;
            this.saving = false;
            this.successMessage = 'Item salvo.';
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.saving = false;
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel salvar o item.');
            this.cdr.detectChanges();
          });
        }
      });
  }

  addExerciseItem(): void {
    if (!this.workout || this.isFinished || this.saving) {
      return;
    }

    if (!this.addItemForm.exerciseId) {
      this.errorMessage = 'Selecione um exercicio para adicionar.';
      return;
    }

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.completedWorkoutService
      .addItem(this.workout.id, this.addItemForm)
      .pipe(take(1))
      .subscribe({
        next: (updated) => {
          this.zone.run(() => {
            this.workout = updated;
            this.saving = false;
            this.successMessage = 'Exercicio adicionado.';
            this.addItemForm = {
              exerciseId: undefined,
              repetitions: undefined,
              weight: undefined,
              distance: undefined,
              durationSeconds: undefined,
            };
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.saving = false;
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel adicionar exercicio.');
            this.cdr.detectChanges();
          });
        }
      });
  }

  finishWorkout(): void {
    if (!this.workout || this.isFinished || this.finishing) {
      return;
    }

    this.finishing = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.completedWorkoutService
      .finish(this.workout.id, this.notes)
      .pipe(take(1))
      .subscribe({
        next: (updated) => {
          this.zone.run(() => {
            this.workout = updated;
            this.finishing = false;
            this.successMessage = 'Treino finalizado com sucesso.';
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.finishing = false;
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel finalizar o treino.');
            this.cdr.detectChanges();
          });
        }
      });
  }

  registerSetFromItem(item: { exerciseId: number; repetitions?: number; weight?: number; distance?: number; durationSeconds?: number }): void {
    if (!this.workout || this.isFinished || this.saving) {
      return;
    }

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.completedWorkoutService
      .addItem(this.workout.id, {
        exerciseId: item.exerciseId,
        repetitions: item.repetitions,
        weight: item.weight,
        distance: item.distance,
        durationSeconds: item.durationSeconds,
      })
      .pipe(take(1))
      .subscribe({
        next: (updated) => {
          this.zone.run(() => {
            this.workout = updated;
            this.saving = false;
            this.successMessage = 'Serie registrada no treino.';
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.saving = false;
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel registrar a serie.');
            this.cdr.detectChanges();
          });
        }
      });
  }

  cancelWorkout(): void {
    if (!this.workout || this.isFinished || this.finishing) {
      return;
    }

    this.finishing = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.completedWorkoutService
      .cancel(this.workout.id)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.zone.run(() => {
            this.finishing = false;
            this.router.navigate(['/train']);
            this.cdr.detectChanges();
          });
        },
        error: (error: HttpErrorResponse) => {
          this.zone.run(() => {
            this.finishing = false;
            this.errorMessage = this.getApiErrorMessage(error, 'Nao foi possivel cancelar o treino.');
            this.cdr.detectChanges();
          });
        }
      });
  }

  goHistory(): void {
    this.router.navigate(['/history']);
  }
}
