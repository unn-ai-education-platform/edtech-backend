package ru.unn.edtech.submission.api;

import ru.unn.edtech.evaluation.EvaluationJobStatus;

import java.util.UUID;

public record StartEvaluationResponse(
        UUID jobId,
        EvaluationJobStatus status
) {}
