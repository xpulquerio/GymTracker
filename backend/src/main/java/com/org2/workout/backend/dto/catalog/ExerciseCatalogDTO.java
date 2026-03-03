package com.org2.workout.backend.dto.catalog;

import lombok.Data;

@Data
public class ExerciseCatalogDTO {
    private Long id;
    private String description;
    private String type;
    private Long equipmentId;
    private String equipmentDescription;
}

