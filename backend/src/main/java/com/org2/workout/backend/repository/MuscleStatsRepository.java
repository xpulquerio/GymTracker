package com.org2.workout.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.MuscleStats;

public interface MuscleStatsRepository extends JpaRepository<MuscleStats, Long> {
    void deleteByMuscleId(Long muscleId);
}

