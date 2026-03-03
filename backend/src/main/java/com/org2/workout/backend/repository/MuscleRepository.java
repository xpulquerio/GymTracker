package com.org2.workout.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.Muscle;

public interface MuscleRepository extends JpaRepository<Muscle, Long> {
    Optional<Muscle> findByDescriptionIgnoreCase(String description);
}

