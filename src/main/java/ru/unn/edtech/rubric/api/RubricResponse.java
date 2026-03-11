package ru.unn.edtech.rubric.api;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RubricResponse(
        UUID id,
        String name,
        List<RubricCriterionPayload> criteria,
        JsonNode gradeScheme,
        Instant createdAt,
        Instant updatedAt
) {}
