package ru.unn.edtech.submission.api;

import ru.unn.edtech.submission.SubmissionView;
import ru.unn.edtech.submission.SubmissionVisibleStatus;

import java.time.Instant;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        UUID rubricId,
        SubmissionVisibleStatus status,
        Instant createdAt,
        SubmissionDecisionResponse decision
) {
    static SubmissionResponse from(SubmissionView view) {
        return new SubmissionResponse(
                view.id(),
                view.rubricId(),
                view.status(),
                view.createdAt(),
                view.decision() != null ? SubmissionDecisionResponse.from(view.decision()) : null
        );
    }
}
