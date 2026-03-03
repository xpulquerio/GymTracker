package com.org2.workout.backend.dto.workout;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlannedWorkoutCreateDTO {

    @NotBlank
    @Size(max = 100)
    private String description;

    @Valid
    @Size(min = 1)
    private List<PlannedWorkoutItemCreateDTO> items;
}
