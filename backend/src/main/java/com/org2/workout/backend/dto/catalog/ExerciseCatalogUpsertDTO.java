package com.org2.workout.backend.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExerciseCatalogUpsertDTO {
    @NotBlank
    private String description;

    @NotBlank
    private String type;

    @NotNull
    private Long equipmentId;
}

