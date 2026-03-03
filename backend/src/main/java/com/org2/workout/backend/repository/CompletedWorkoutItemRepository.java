package com.org2.workout.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.CompletedWorkout;
import com.org2.workout.backend.model.CompletedWorkoutItem;

public interface CompletedWorkoutItemRepository
        extends JpaRepository<CompletedWorkoutItem, Long> {

    List<CompletedWorkoutItem> findByCompletedWorkout(CompletedWorkout completedWorkout);

    List<CompletedWorkoutItem> findByCompletedWorkoutOrderByIdAsc(CompletedWorkout completedWorkout);

    List<CompletedWorkoutItem> findByCompletedWorkoutId(Long completedWorkoutId);

    Optional<CompletedWorkoutItem> findByIdAndCompletedWorkout(Long id, CompletedWorkout completedWorkout);

    void deleteByCompletedWorkout(CompletedWorkout completedWorkout);

    boolean existsByExerciseId(Long exerciseId);
}
