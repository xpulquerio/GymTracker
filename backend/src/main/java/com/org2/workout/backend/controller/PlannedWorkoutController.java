package com.org2.workout.backend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org2.workout.backend.dto.workout.ExerciseOptionDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutCreateDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutItemDTO;
import com.org2.workout.backend.model.User;
import com.org2.workout.backend.service.PlannedWorkoutService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/planned-workout")
public class PlannedWorkoutController {
    private final PlannedWorkoutService plannedWorkoutService;
    private static final Logger log = LoggerFactory.getLogger(PlannedWorkoutController.class);

    public PlannedWorkoutController(PlannedWorkoutService plannedWorkoutService) {
        this.plannedWorkoutService = plannedWorkoutService;
    }

    @GetMapping("/list")
    public List<PlannedWorkoutDTO> listAll(@AuthenticationPrincipal User user) {
        log.info("GET /api/planned-workout/list");
        return plannedWorkoutService.findAllByUser(user);
    }

    @GetMapping("/exercises")
    public List<ExerciseOptionDTO> listExercises() {
        log.info("GET /api/planned-workout/exercises");
        return plannedWorkoutService.listExercises();
    }

    @GetMapping("/{plannedWorkoutId}/items")
    public List<PlannedWorkoutItemDTO> listItemsByWorkout(
            @PathVariable Long plannedWorkoutId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/planned-workout/{plannedWorkoutId}/items");
        return plannedWorkoutService.listItemsByWorkout(plannedWorkoutId, user);
    }

    @GetMapping("/{plannedWorkoutId}")
    public PlannedWorkoutDTO getById(
            @PathVariable Long plannedWorkoutId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/planned-workout/{plannedWorkoutId}");
        return plannedWorkoutService.findById(plannedWorkoutId, user);
    }

    @PostMapping
    public ResponseEntity<PlannedWorkoutDTO> create(
            @Valid @RequestBody PlannedWorkoutCreateDTO request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/planned-workout");
        PlannedWorkoutDTO response = plannedWorkoutService.create(request, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{plannedWorkoutId}")
    public ResponseEntity<PlannedWorkoutDTO> update(
            @PathVariable Long plannedWorkoutId,
            @Valid @RequestBody PlannedWorkoutCreateDTO request,
            @AuthenticationPrincipal User user) {
        log.info("PUT /api/planned-workout/{plannedWorkoutId}");
        PlannedWorkoutDTO response = plannedWorkoutService.update(plannedWorkoutId, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{plannedWorkoutId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long plannedWorkoutId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/planned-workout/{plannedWorkoutId}");
        plannedWorkoutService.delete(plannedWorkoutId, user);
        return ResponseEntity.noContent().build();
    }
}
