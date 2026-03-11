package ru.unn.edtech.submission;

import java.time.Instant;
import java.util.UUID;

public record SubmissionView(
        UUID id,
        UUID rubricId,
        SubmissionVisibleStatus status,
        Instant createdAt,
        SubmissionDecisionView decision
) {}
