package ru.unn.edtech.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface EvaluationJobRepository extends JpaRepository<EvaluationJobEntity, UUID> {

    boolean existsBySubmissionIdAndStatusIn(UUID submissionId, Collection<EvaluationJobStatus> statuses);
}
