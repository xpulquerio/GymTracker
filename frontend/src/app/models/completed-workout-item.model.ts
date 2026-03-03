// completed-workout-item.model.ts
export interface CompletedWorkoutItem {
  id: number;

  exerciseId: number;
  exerciseDescription: string;
  exerciseType?: string;

  repetitions?: number;
  weight?: number;          // kg
  distance?: number;        // km ou m
  durationSeconds?: number; // cardio

  sequence?: number;
}
