package ru.unn.edtech.evaluation.api;

import ru.unn.edtech.evaluation.EvaluationJobStatus;

import java.util.UUID;

public record EvaluationJobResponse(
        UUID jobId,
        EvaluationJobStatus status
) {}
