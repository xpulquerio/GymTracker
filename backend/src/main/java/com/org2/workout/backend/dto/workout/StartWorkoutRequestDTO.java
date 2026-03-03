package com.org2.workout.backend.dto.workout;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StartWorkoutRequestDTO {
    private Long plannedWorkoutId;

    @Size(max = 100)
    private String description;
}
