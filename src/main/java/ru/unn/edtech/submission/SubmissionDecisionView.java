package ru.unn.edtech.submission;

import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;

public record SubmissionDecisionView(
        String mode,
        JsonNode criteriaResults,
        String comment,
        BigDecimal totalScoreNormalized,
        JsonNode finalGrade,
        Instant decidedAt
) {}
