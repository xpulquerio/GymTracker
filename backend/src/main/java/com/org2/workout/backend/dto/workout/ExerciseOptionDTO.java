package com.org2.workout.backend.dto.workout;

import lombok.Data;

@Data
public class ExerciseOptionDTO {
    private Long id;
    private String description;
    private String type;
    private String equipment;
}
