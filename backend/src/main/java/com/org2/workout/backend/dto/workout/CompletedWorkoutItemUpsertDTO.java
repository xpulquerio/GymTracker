package com.org2.workout.backend.dto.workout;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CompletedWorkoutItemUpsertDTO {
    private Long exerciseId;
    private Integer repetitions;
    private BigDecimal weight;
    private BigDecimal distance;
    private Long durationSeconds;
}
