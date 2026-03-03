import { Component } from '@angular/core';
import { NavigationStart, Router, RouterModule } from '@angular/router';
import { PlannedWorkoutService } from '../../services/planned-workout.service';
import { take } from 'rxjs/operators';
import { CompletedWorkoutService } from '../../services/completed-workout.service';

@Component({
  selector: 'app-footer',
  imports: [RouterModule],
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css',
})
export class FooterComponent {
  fabOpen = false;
  startingManual = false;

  constructor(
    private readonly plannedWorkoutService: PlannedWorkoutService,
    private readonly completedWorkoutService: CompletedWorkoutService,
    private readonly router: Router
  ) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.closeFab();
      }
    });
  }

  toggleFab(): void {
    this.fabOpen = !this.fabOpen;
  }

  closeFab(): void {
    this.fabOpen = false;
  }

  openTrainList(): void {
    this.closeFab();
    this.router.navigate(['/train']);
  }

  openCreatePlannedWorkout(): void {
    this.closeFab();
    this.router.navigate(['/planned-workout/new']);
  }

  startManualWorkout(): void {
    if (this.startingManual) {
      return;
    }

    this.startingManual = true;
    this.closeFab();

    this.completedWorkoutService
      .findInProgress()
      .pipe(take(1))
      .subscribe({
        next: (inProgress) => {
          if (inProgress) {
            this.startingManual = false;
            this.router.navigate(['/completed-workout', inProgress.id], {
              queryParams: { alreadyInProgress: '1' }
            });
            return;
          }

          this.plannedWorkoutService
            .startManual('Treino Livre')
            .pipe(take(1))
            .subscribe({
              next: (completedWorkout) => {
                this.startingManual = false;
                this.router.navigate(['/completed-workout', completedWorkout.id]);
              },
              error: () => {
                this.startingManual = false;
                this.router.navigate(['/train']);
              }
            });
        },
        error: () => {
          this.startingManual = false;
          this.router.navigate(['/train']);
        }
      });
  }
}
