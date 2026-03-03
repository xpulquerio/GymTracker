package com.org2.workout.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.PlannedWorkout;
import com.org2.workout.backend.model.PlannedWorkoutItem;

public interface PlannedWorkoutItemRepository
        extends JpaRepository<PlannedWorkoutItem, Long> {

    List<PlannedWorkoutItem> findByPlannedWorkoutOrderBySequence(PlannedWorkout plannedWorkout);

    List<PlannedWorkoutItem> findByPlannedWorkoutIdOrderBySequence(Long plannedWorkoutId);

    boolean existsByExerciseId(Long exerciseId);
}
