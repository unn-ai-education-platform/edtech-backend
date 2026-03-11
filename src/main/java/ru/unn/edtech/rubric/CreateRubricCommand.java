package ru.unn.edtech.rubric;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record CreateRubricCommand(
        String name,
        List<RubricCriterion> criteria,
        JsonNode gradeScheme,
        String createdBy
) {}
