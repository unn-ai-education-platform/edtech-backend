package ru.unn.edtech.submission.api;

import ru.unn.edtech.submission.SubmissionDecisionView;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;

public record SubmissionDecisionResponse(
        String mode,
        JsonNode criteriaResults,
        String comment,
        BigDecimal totalScoreNormalized,
        JsonNode finalGrade,
        Instant decidedAt
) {
    static SubmissionDecisionResponse from(SubmissionDecisionView view) {
        return new SubmissionDecisionResponse(
                view.mode(),
                view.criteriaResults(),
                view.comment(),
                view.totalScoreNormalized(),
                view.finalGrade(),
                view.decidedAt()
        );
    }
}
