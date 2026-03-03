package com.org2.workout.backend.dto.workout;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PlannedWorkoutItemDTO {
    private Long id;
    private Long exerciseId;
    private String exerciseDescription;
    private String exerciseType;
    private Integer sequence;
    private Integer repetitions;
    private BigDecimal distance;
    private Long durationSeconds;
}
