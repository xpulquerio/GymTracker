package com.org2.workout.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org2.workout.backend.model.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByDescriptionIgnoreCase(String description);
}
