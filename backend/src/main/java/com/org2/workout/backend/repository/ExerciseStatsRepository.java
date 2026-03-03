package com.org2.workout.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.ExerciseStats;

public interface ExerciseStatsRepository extends JpaRepository<ExerciseStats, Long> {
    void deleteByExerciseId(Long exerciseId);
}

