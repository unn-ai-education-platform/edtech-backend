package ru.unn.edtech.rubric.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

import java.util.List;

public record CreateRubricRequest(
        @NotBlank String name,
        @NotEmpty List<@Valid RubricCriterionPayload> criteria,
        @NotNull JsonNode gradeScheme
) {}
