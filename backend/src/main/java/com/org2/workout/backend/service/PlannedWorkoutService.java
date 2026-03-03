package com.org2.workout.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.org2.workout.backend.dto.workout.ExerciseOptionDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutCreateDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutItemDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutItemCreateDTO;
import com.org2.workout.backend.dto.workout.PlannedWorkoutDTO;
import com.org2.workout.backend.mapper.PlannedWorkoutMapper;
import com.org2.workout.backend.model.Exercise;
import com.org2.workout.backend.model.PlannedWorkout;
import com.org2.workout.backend.model.PlannedWorkoutItem;
import com.org2.workout.backend.model.User;
import com.org2.workout.backend.repository.ExerciseRepository;
import com.org2.workout.backend.repository.PlannedWorkoutItemRepository;
import com.org2.workout.backend.repository.PlannedWorkoutRepository;

@Service
public class PlannedWorkoutService {
    private final PlannedWorkoutRepository plannedWorkoutRepository;
    private final PlannedWorkoutItemRepository plannedWorkoutItemRepository;
    private final ExerciseRepository exerciseRepository;
    private static final Logger log = LoggerFactory.getLogger(PlannedWorkoutService.class);

    public PlannedWorkoutService(
            PlannedWorkoutRepository plannedWorkoutRepository,
            PlannedWorkoutItemRepository plannedWorkoutItemRepository,
            ExerciseRepository exerciseRepository) {
        this.plannedWorkoutRepository = plannedWorkoutRepository;
        this.plannedWorkoutItemRepository = plannedWorkoutItemRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public List<PlannedWorkoutDTO> findAllByUser(User user) {
        return plannedWorkoutRepository.findByUserAndActiveTrueOrderByUpdatedAtDesc(user)
                .stream()
                .map(PlannedWorkoutMapper::toDTO)
                .toList();
    }

    public PlannedWorkoutDTO create(PlannedWorkoutCreateDTO dto, User user) {
        PlannedWorkout plannedWorkout = new PlannedWorkout();
        plannedWorkout.setUser(user);
        plannedWorkout.setDescription(dto.getDescription().trim());
        plannedWorkout.setActive(true);

        PlannedWorkout savedWorkout = plannedWorkoutRepository.save(plannedWorkout);
        saveItems(savedWorkout, dto.getItems());

        return PlannedWorkoutMapper.toDTO(savedWorkout);
    }

    public PlannedWorkoutDTO update(Long plannedWorkoutId, PlannedWorkoutCreateDTO dto, User user) {
        PlannedWorkout workout = findOwnedWorkout(plannedWorkoutId, user);

        workout.setDescription(dto.getDescription().trim());
        workout.setActive(true);
        plannedWorkoutRepository.save(workout);

        List<PlannedWorkoutItem> existingItems = plannedWorkoutItemRepository.findByPlannedWorkoutOrderBySequence(workout);
        if (!existingItems.isEmpty()) {
            plannedWorkoutItemRepository.deleteAll(existingItems);
        }

        saveItems(workout, dto.getItems());

        return PlannedWorkoutMapper.toDTO(workout);
    }

    public PlannedWorkoutDTO findById(Long plannedWorkoutId, User user) {
        return PlannedWorkoutMapper.toDTO(findOwnedWorkout(plannedWorkoutId, user));
    }

    public void delete(Long plannedWorkoutId, User user) {
        PlannedWorkout workout = findOwnedWorkout(plannedWorkoutId, user);
        workout.setActive(false);
        plannedWorkoutRepository.save(workout);
    }

    private void saveItems(PlannedWorkout workout, List<PlannedWorkoutItemCreateDTO> requestItems) {
        List<PlannedWorkoutItemCreateDTO> items = requestItems == null ? new ArrayList<>() : requestItems;
        for (int i = 0; i < items.size(); i++) {
            PlannedWorkoutItemCreateDTO itemDto = items.get(i);

            Exercise exercise = exerciseRepository.findById(itemDto.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("Exercicio nao encontrado"));

            PlannedWorkoutItem item = new PlannedWorkoutItem();
            item.setPlannedWorkout(workout);
            item.setExercise(exercise);
            item.setSequence(itemDto.getSequence() != null ? itemDto.getSequence() : (i + 1));
            item.setRepetitions(itemDto.getRepetitions());
            item.setDistance(itemDto.getDistance());
            item.setDurationSeconds(itemDto.getDurationSeconds());

            plannedWorkoutItemRepository.save(item);
        }
    }

    private PlannedWorkout findOwnedWorkout(Long plannedWorkoutId, User user) {
        PlannedWorkout workout = plannedWorkoutRepository.findById(plannedWorkoutId)
                .orElseThrow(() -> new RuntimeException("Treino planejado nao encontrado"));

        if (workout.getUser() == null || user == null || user.getId() == null
                || !workout.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Treino planejado nao encontrado");
        }

        return workout;
    }

    public List<ExerciseOptionDTO> listExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(exercise -> {
                    ExerciseOptionDTO dto = new ExerciseOptionDTO();
                    dto.setId(exercise.getId());
                    dto.setDescription(exercise.getDescription());
                    dto.setType(exercise.getType().name());
                    dto.setEquipment(exercise.getEquipment().getDescription());
                    return dto;
                })
                .toList();
    }

    public List<PlannedWorkoutItemDTO> listItemsByWorkout(Long plannedWorkoutId, User user) {
        findOwnedWorkout(plannedWorkoutId, user);

        return plannedWorkoutItemRepository.findByPlannedWorkoutIdOrderBySequence(plannedWorkoutId)
                .stream()
                .map(item -> {
                    PlannedWorkoutItemDTO dto = new PlannedWorkoutItemDTO();
                    dto.setId(item.getId());
                    dto.setExerciseId(item.getExercise().getId());
                    dto.setExerciseDescription(item.getExercise().getDescription());
                    dto.setExerciseType(item.getExercise().getType().name());
                    dto.setSequence(item.getSequence());
                    dto.setRepetitions(item.getRepetitions());
                    dto.setDistance(item.getDistance());
                    dto.setDurationSeconds(item.getDurationSeconds());
                    return dto;
                })
                .toList();
    }
}
