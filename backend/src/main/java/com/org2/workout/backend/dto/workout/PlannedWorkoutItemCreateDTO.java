package com.org2.workout.backend.dto.workout;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PlannedWorkoutItemCreateDTO {

    @NotNull
    @Positive
    private Long exerciseId;

    @Positive
    private Integer sequence;

    private Integer repetitions;

    private BigDecimal distance;

    private Long durationSeconds;
}
