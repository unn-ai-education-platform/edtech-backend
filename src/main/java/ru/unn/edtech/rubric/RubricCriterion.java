package ru.unn.edtech.rubric;

public record RubricCriterion(
        String name,
        String description,
        int weight
) {}
