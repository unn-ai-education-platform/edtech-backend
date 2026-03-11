package ru.unn.edtech.rubric.api;

import jakarta.validation.Valid;
import tools.jackson.databind.JsonNode;

import java.util.List;

public record UpdateRubricRequest(
        String name,
        List<@Valid RubricCriterionPayload> criteria,
        JsonNode gradeScheme
) {}
