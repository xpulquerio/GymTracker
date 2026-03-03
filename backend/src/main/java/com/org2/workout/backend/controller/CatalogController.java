package com.org2.workout.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org2.workout.backend.dto.catalog.EquipmentDTO;
import com.org2.workout.backend.dto.catalog.ExerciseCatalogDTO;
import com.org2.workout.backend.dto.catalog.ExerciseCatalogUpsertDTO;
import com.org2.workout.backend.dto.catalog.MuscleDTO;
import com.org2.workout.backend.dto.catalog.NameOnlyUpsertDTO;
import com.org2.workout.backend.service.CatalogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/equipments")
    public List<EquipmentDTO> listEquipments() {
        return catalogService.listEquipments();
    }

    @PostMapping("/equipments")
    public ResponseEntity<EquipmentDTO> createEquipment(@Valid @RequestBody NameOnlyUpsertDTO request) {
        return ResponseEntity.ok(catalogService.createEquipment(request));
    }

    @PutMapping("/equipments/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody NameOnlyUpsertDTO request) {
        return ResponseEntity.ok(catalogService.updateEquipment(id, request));
    }

    @DeleteMapping("/equipments/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        catalogService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/muscles")
    public List<MuscleDTO> listMuscles() {
        return catalogService.listMuscles();
    }

    @PostMapping("/muscles")
    public ResponseEntity<MuscleDTO> createMuscle(@Valid @RequestBody NameOnlyUpsertDTO request) {
        return ResponseEntity.ok(catalogService.createMuscle(request));
    }

    @PutMapping("/muscles/{id}")
    public ResponseEntity<MuscleDTO> updateMuscle(
            @PathVariable Long id,
            @Valid @RequestBody NameOnlyUpsertDTO request) {
        return ResponseEntity.ok(catalogService.updateMuscle(id, request));
    }

    @DeleteMapping("/muscles/{id}")
    public ResponseEntity<Void> deleteMuscle(@PathVariable Long id) {
        catalogService.deleteMuscle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exercises")
    public List<ExerciseCatalogDTO> listExercises() {
        return catalogService.listExercises();
    }

    @PostMapping("/exercises")
    public ResponseEntity<ExerciseCatalogDTO> createExercise(@Valid @RequestBody ExerciseCatalogUpsertDTO request) {
        return ResponseEntity.ok(catalogService.createExercise(request));
    }

    @PutMapping("/exercises/{id}")
    public ResponseEntity<ExerciseCatalogDTO> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseCatalogUpsertDTO request) {
        return ResponseEntity.ok(catalogService.updateExercise(id, request));
    }

    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        catalogService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }
}

