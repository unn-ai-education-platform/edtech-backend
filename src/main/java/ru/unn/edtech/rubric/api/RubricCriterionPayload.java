package ru.unn.edtech.rubric.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RubricCriterionPayload(
        @NotBlank String name,
        String description,
        @NotNull @Min(0) @Max(100) Integer weight
) {}
