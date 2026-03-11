package ru.unn.edtech.rubric.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RubricCriterionPayload(
        @NotBlank String name,
        String description,
        @Min(0) @Max(100) int weight
) {}
