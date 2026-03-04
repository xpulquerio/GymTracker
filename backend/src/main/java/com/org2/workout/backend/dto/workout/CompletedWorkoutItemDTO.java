package com.org2.workout.backend.dto.workout;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CompletedWorkoutItemDTO {

    private Long id;
    private Long exerciseId;
    private String exerciseDescription;
    private String exerciseType;
    // Realizado
    private Integer repetitions;
    private BigDecimal weight;
    private BigDecimal distance;
    private Long durationSeconds;
    private Boolean performed;
}
