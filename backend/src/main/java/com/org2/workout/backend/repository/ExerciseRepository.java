package com.org2.workout.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    boolean existsByEquipmentId(Long equipmentId);
}
