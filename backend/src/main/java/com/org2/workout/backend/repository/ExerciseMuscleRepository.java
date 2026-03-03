package com.org2.workout.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.ExerciseMuscle;

public interface ExerciseMuscleRepository extends JpaRepository<ExerciseMuscle, Long> {
    void deleteByExerciseId(Long exerciseId);

    void deleteByMuscleId(Long muscleId);
}

