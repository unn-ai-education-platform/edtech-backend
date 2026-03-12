package ru.unn.edtech.evaluation;

import ru.unn.edtech.support.exception.NotFoundException;

import java.util.UUID;

public class EvaluationJobNotFoundException extends NotFoundException {

    public EvaluationJobNotFoundException(UUID jobId) {
        super("JOB_NOT_FOUND", "Evaluation job not found: " + jobId);
    }
}
