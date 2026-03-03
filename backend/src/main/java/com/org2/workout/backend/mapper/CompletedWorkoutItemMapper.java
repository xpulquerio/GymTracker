package com.org2.workout.backend.mapper;

import com.org2.workout.backend.dto.workout.CompletedWorkoutItemDTO;
import com.org2.workout.backend.model.CompletedWorkoutItem;

import lombok.Data;

@Data
public class CompletedWorkoutItemMapper {

    private CompletedWorkoutItemMapper() {
    }

    public static CompletedWorkoutItemDTO toDTO(CompletedWorkoutItem item) {

        CompletedWorkoutItemDTO dto = new CompletedWorkoutItemDTO();
        dto.setId(item.getId());
        dto.setExerciseId(item.getExercise().getId());
        dto.setExerciseDescription(item.getExercise().getDescription());
        dto.setExerciseType(item.getExercise().getType().name());
        dto.setWeight(item.getWeight());
        dto.setRepetitions(item.getRepititions());
        dto.setDurationSeconds(item.getDurationSeconds());
        dto.setDistance(item.getDistance());

        return dto;
    }
}
