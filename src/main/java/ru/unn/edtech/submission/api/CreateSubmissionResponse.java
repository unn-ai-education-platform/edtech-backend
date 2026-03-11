package ru.unn.edtech.submission.api;

import java.time.Instant;
import java.util.UUID;

public record CreateSubmissionResponse(
        UUID submissionId,
        Instant createdAt
) {}
