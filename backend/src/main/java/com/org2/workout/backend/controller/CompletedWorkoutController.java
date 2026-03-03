package com.org2.workout.backend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.org2.workout.backend.dto.workout.CompletedWorkoutDTO;
import com.org2.workout.backend.dto.workout.CompletedWorkoutItemUpsertDTO;
import com.org2.workout.backend.dto.workout.FinishWorkoutRequestDTO;
import com.org2.workout.backend.dto.workout.StartWorkoutRequestDTO;
import com.org2.workout.backend.model.User;
import com.org2.workout.backend.service.CompletedWorkoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/completed-workout")
public class CompletedWorkoutController {
    private final CompletedWorkoutService completedWorkoutService;
    private static final Logger log = LoggerFactory.getLogger(CompletedWorkoutController.class);

    public CompletedWorkoutController(CompletedWorkoutService completedWorkoutService) {
        this.completedWorkoutService = completedWorkoutService;
    }

    @GetMapping("/list")
    public List<CompletedWorkoutDTO> listAll(@AuthenticationPrincipal User user) {
        log.info("GET /api/completed-workout/list");
        return completedWorkoutService.findAllByUser(user);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<CompletedWorkoutDTO> getInProgress(@AuthenticationPrincipal User user) {
        log.info("GET /api/completed-workout/in-progress");
        return completedWorkoutService.findInProgress(user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/start")
    public ResponseEntity<CompletedWorkoutDTO> start(
            @Valid @RequestBody(required = false) StartWorkoutRequestDTO request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/completed-workout/start");
        CompletedWorkoutDTO dto = completedWorkoutService.start(request, user);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/start/{plannedWorkoutId}")
    public ResponseEntity<CompletedWorkoutDTO> startFromPlanned(
            @PathVariable Long plannedWorkoutId,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/completed-workout/start/{plannedWorkoutId}");
        CompletedWorkoutDTO dto = completedWorkoutService.startFromPlanned(plannedWorkoutId, user);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{completedWorkoutId}/items/{itemId}")
    public ResponseEntity<CompletedWorkoutDTO> updateItem(
            @PathVariable Long completedWorkoutId,
            @PathVariable Long itemId,
            @RequestBody CompletedWorkoutItemUpsertDTO request,
            @AuthenticationPrincipal User user) {
        log.info("PUT /api/completed-workout/{completedWorkoutId}/items/{itemId}");
        CompletedWorkoutDTO dto = completedWorkoutService.updateItem(completedWorkoutId, itemId, request, user);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{completedWorkoutId}/items")
    public ResponseEntity<CompletedWorkoutDTO> addItem(
            @PathVariable Long completedWorkoutId,
            @RequestBody CompletedWorkoutItemUpsertDTO request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/completed-workout/{completedWorkoutId}/items");
        CompletedWorkoutDTO dto = completedWorkoutService.addItem(completedWorkoutId, request, user);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{completedWorkoutId}/finish")
    public ResponseEntity<CompletedWorkoutDTO> finish(
            @PathVariable Long completedWorkoutId,
            @RequestBody(required = false) FinishWorkoutRequestDTO request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/completed-workout/{completedWorkoutId}/finish");
        CompletedWorkoutDTO dto = completedWorkoutService.finish(completedWorkoutId, request, user);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{completedWorkoutId}/cancel")
    public ResponseEntity<Void> cancelInProgress(
            @PathVariable Long completedWorkoutId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/completed-workout/{completedWorkoutId}/cancel");
        completedWorkoutService.cancelInProgress(completedWorkoutId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{completedWorkoutId}")
    public ResponseEntity<CompletedWorkoutDTO> getById(
            @PathVariable Long completedWorkoutId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/completed-workout/{completedWorkoutId}");
        CompletedWorkoutDTO dto = completedWorkoutService.findById(completedWorkoutId, user);
        return ResponseEntity.ok(dto);
    }
}
