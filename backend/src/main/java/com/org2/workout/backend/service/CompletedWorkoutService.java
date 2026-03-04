package com.org2.workout.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.org2.workout.backend.dto.workout.CompletedWorkoutDTO;
import com.org2.workout.backend.dto.workout.CompletedWorkoutItemUpsertDTO;
import com.org2.workout.backend.dto.workout.FinishWorkoutRequestDTO;
import com.org2.workout.backend.dto.workout.StartWorkoutRequestDTO;
import com.org2.workout.backend.mapper.CompletedWorkoutMapper;
import com.org2.workout.backend.model.CompletedWorkout;
import com.org2.workout.backend.model.CompletedWorkoutItem;
import com.org2.workout.backend.model.Exercise;
import com.org2.workout.backend.model.PlannedWorkout;
import com.org2.workout.backend.model.PlannedWorkoutItem;
import com.org2.workout.backend.model.User;
import com.org2.workout.backend.repository.CompletedWorkoutItemRepository;
import com.org2.workout.backend.repository.CompletedWorkoutRespository;
import com.org2.workout.backend.repository.ExerciseRepository;
import com.org2.workout.backend.repository.PlannedWorkoutItemRepository;
import com.org2.workout.backend.repository.PlannedWorkoutRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CompletedWorkoutService {
    private final CompletedWorkoutRespository completedWorkoutRepository;
    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final PlannedWorkoutItemRepository plannedWorkoutItemRepository;
    private final CompletedWorkoutItemRepository completedWorkoutItemRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutDayService workoutDayService;
    private static final Logger log = LoggerFactory.getLogger(CompletedWorkoutService.class);

    @PersistenceContext
    private EntityManager entityManager;

    public CompletedWorkoutService(
            CompletedWorkoutRespository completedWorkoutRepository,
            PlannedWorkoutRepository plannedWorkoutRepository,
            CompletedWorkoutItemRepository completedWorkoutItemRepository,
            PlannedWorkoutItemRepository plannedWorkoutItemRepository,
            ExerciseRepository exerciseRepository,
            WorkoutDayService workoutDayService) {
        this.completedWorkoutRepository = completedWorkoutRepository;
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.completedWorkoutItemRepository = completedWorkoutItemRepository;
        this.plannedWorkoutItemRepository = plannedWorkoutItemRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutDayService = workoutDayService;
    }

    public List<CompletedWorkoutDTO> findAllByUser(User user) {
        return completedWorkoutRepository
                .findByUserOrderByStartedAtDesc(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<CompletedWorkoutDTO> findInProgress(User user) {
        return completedWorkoutRepository
                .findFirstByUserAndFinishedAtIsNullOrderByStartedAtDesc(user)
                .map(this::toDTO);
    }

    public CompletedWorkoutDTO start(StartWorkoutRequestDTO request, User user) {
        Optional<CompletedWorkoutDTO> inProgress = findInProgress(user);
        if (inProgress.isPresent()) {
            return inProgress.get();
        }

        if (request != null && request.getPlannedWorkoutId() != null) {
            return startFromPlanned(request.getPlannedWorkoutId(), user);
        }

        String description = request == null ? null : request.getDescription();
        return startManual(description, user);
    }

    public CompletedWorkoutDTO startFromPlanned(Long plannedWorkoutId, User user) {
        Optional<CompletedWorkoutDTO> inProgress = findInProgress(user);
        if (inProgress.isPresent()) {
            return inProgress.get();
        }

        PlannedWorkout plannedWorkout = plannedWorkoutRepository.findByIdAndUser(plannedWorkoutId, user);

        if (plannedWorkout == null) {
            throw new RuntimeException("Treino planejado nao encontrado");
        }

        CompletedWorkout completedWorkout = new CompletedWorkout();
        completedWorkout.setUser(user);
        completedWorkout.setPlannedWorkout(plannedWorkout);
        completedWorkout.setDescription(plannedWorkout.getDescription());
        completedWorkout.setStartedAt(LocalDateTime.now());

        completedWorkoutRepository.save(completedWorkout);

        List<PlannedWorkoutItem> plannedItems = plannedWorkoutItemRepository.findByPlannedWorkoutOrderBySequence(plannedWorkout);

        for (PlannedWorkoutItem planned : plannedItems) {
            CompletedWorkoutItem item = new CompletedWorkoutItem();
            item.setCompletedWorkout(completedWorkout);
            item.setExercise(planned.getExercise());
            item.setRepititions(planned.getRepetitions());
            item.setDistance(planned.getDistance());
            item.setDurationSeconds(planned.getDurationSeconds());
            item.setWeight(null);
            item.setPerformed(false);

            completedWorkoutItemRepository.save(item);
        }

        return toDTO(completedWorkout);
    }

    public CompletedWorkoutDTO startManual(String description, User user) {
        Optional<CompletedWorkoutDTO> inProgress = findInProgress(user);
        if (inProgress.isPresent()) {
            return inProgress.get();
        }

        CompletedWorkout completedWorkout = new CompletedWorkout();
        completedWorkout.setUser(user);
        completedWorkout.setPlannedWorkout(null);
        completedWorkout.setDescription(description == null || description.isBlank() ? "Treino Livre" : description.trim());
        completedWorkout.setStartedAt(LocalDateTime.now());

        completedWorkoutRepository.save(completedWorkout);

        return toDTO(completedWorkout);
    }

    public CompletedWorkoutDTO findById(Long id, User user) {
        CompletedWorkout completedWorkout = loadOwnedWorkout(id, user);

        return toDTO(completedWorkout);
    }

    @Transactional
    public CompletedWorkoutDTO updateItem(Long workoutId, Long itemId, CompletedWorkoutItemUpsertDTO request, User user) {
        CompletedWorkout workout = loadOwnedWorkout(workoutId, user);

        if (workout.getFinishedAt() != null) {
            throw new RuntimeException("Treino ja finalizado");
        }

        CompletedWorkoutItem item = completedWorkoutItemRepository
                .findByIdAndCompletedWorkout(itemId, workout)
                .orElseThrow(() -> new RuntimeException("Item do treino nao encontrado"));

        applyItemValues(item, request);
        item.setPerformed(true);
        completedWorkoutItemRepository.saveAndFlush(item);
        entityManager.flush();
        entityManager.clear();

        return findById(workoutId, user);
    }

    @Transactional
    public CompletedWorkoutDTO addItem(Long workoutId, CompletedWorkoutItemUpsertDTO request, User user) {
        CompletedWorkout workout = loadOwnedWorkout(workoutId, user);

        if (workout.getFinishedAt() != null) {
            throw new RuntimeException("Treino ja finalizado");
        }

        if (request.getExerciseId() == null) {
            throw new RuntimeException("exerciseId eh obrigatorio");
        }

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new RuntimeException("Exercicio nao encontrado"));

        CompletedWorkoutItem item = new CompletedWorkoutItem();
        item.setCompletedWorkout(workout);
        item.setExercise(exercise);
        applyItemValues(item, request);
        boolean isFreeWorkout = workout.getPlannedWorkout() == null;
        item.setPerformed(isFreeWorkout || hasAnyExecutionValue(request));

        completedWorkoutItemRepository.saveAndFlush(item);
        entityManager.flush();
        entityManager.clear();

        return findById(workoutId, user);
    }

    @Transactional
    public CompletedWorkoutDTO removeItem(Long workoutId, Long itemId, User user) {
        CompletedWorkout workout = loadOwnedWorkout(workoutId, user);

        if (workout.getFinishedAt() != null) {
            throw new RuntimeException("Treino ja finalizado");
        }

        CompletedWorkoutItem item = completedWorkoutItemRepository
                .findByIdAndCompletedWorkout(itemId, workout)
                .orElseThrow(() -> new RuntimeException("Item do treino nao encontrado"));

        completedWorkoutItemRepository.delete(item);
        completedWorkoutItemRepository.flush();
        entityManager.clear();

        return findById(workoutId, user);
    }

    public CompletedWorkoutDTO finish(Long workoutId, FinishWorkoutRequestDTO request, User user) {
        CompletedWorkout workout = loadOwnedWorkout(workoutId, user);

        if (workout.getFinishedAt() == null) {
            workout.setFinishedAt(LocalDateTime.now());
        }

        if (request != null && request.getNotes() != null) {
            workout.setNotes(request.getNotes());
        }

        completedWorkoutRepository.save(workout);
        workoutDayService.markToday(user);

        return toDTO(workout);
    }

    public void cancelInProgress(Long workoutId, User user) {
        CompletedWorkout workout = loadOwnedWorkout(workoutId, user);

        if (workout.getFinishedAt() != null) {
            throw new RuntimeException("Treino ja finalizado, nao pode cancelar");
        }

        List<CompletedWorkoutItem> items = completedWorkoutItemRepository.findByCompletedWorkout(workout);
        if (!items.isEmpty()) {
            completedWorkoutItemRepository.deleteAll(items);
        }
        completedWorkoutRepository.delete(workout);
    }

    private CompletedWorkout loadOwnedWorkout(Long workoutId, User user) {
        CompletedWorkout workout = completedWorkoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout nao encontrado"));

        if (workout.getUser() == null || user == null || workout.getUser().getId() == null || user.getId() == null
                || !workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Workout nao pertence ao usuario atual");
        }

        return workout;
    }

    private void applyItemValues(CompletedWorkoutItem item, CompletedWorkoutItemUpsertDTO request) {
        if (request == null) {
            return;
        }

        if (request.getRepetitions() != null) {
            item.setRepititions(request.getRepetitions());
        }

        if (request.getWeight() != null) {
            item.setWeight(request.getWeight());
        }

        if (request.getDistance() != null) {
            item.setDistance(request.getDistance());
        }

        if (request.getDurationSeconds() != null) {
            item.setDurationSeconds(request.getDurationSeconds());
        }
    }

    private boolean hasAnyExecutionValue(CompletedWorkoutItemUpsertDTO request) {
        if (request == null) {
            return false;
        }

        return request.getRepetitions() != null
                || request.getWeight() != null
                || request.getDistance() != null
                || request.getDurationSeconds() != null;
    }

    private CompletedWorkoutDTO toDTO(CompletedWorkout workout) {
        List<CompletedWorkoutItem> items = completedWorkoutItemRepository.findByCompletedWorkoutOrderByIdAsc(workout);
        return CompletedWorkoutMapper.toDTO(workout, items);
    }
}
