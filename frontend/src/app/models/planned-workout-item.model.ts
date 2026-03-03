export interface PlannedWorkoutItem {
  id: number;
  exerciseId: number;
  exerciseDescription: string;
  exerciseType: string;
  sequence: number;
  repetitions?: number;
  distance?: number;
  durationSeconds?: number;
}
