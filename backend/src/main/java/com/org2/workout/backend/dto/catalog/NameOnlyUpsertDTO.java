package com.org2.workout.backend.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NameOnlyUpsertDTO {
    @NotBlank
    private String description;
}

