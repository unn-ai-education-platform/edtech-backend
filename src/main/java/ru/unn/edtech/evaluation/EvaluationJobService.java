package ru.unn.edtech.evaluation;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.unn.edtech.submission.SubmissionNotFoundException;
import ru.unn.edtech.submission.SubmissionRepository;
import ru.unn.edtech.support.exception.BadRequestException;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EvaluationJobService {

    private static final EnumSet<EvaluationJobStatus> ACTIVE_STATUSES = EnumSet.of(
            EvaluationJobStatus.QUEUED,
            EvaluationJobStatus.RUNNING
    );

    private final EvaluationJobRepository evaluationJobRepository;
    private final SubmissionRepository submissionRepository;

    public EvaluationJobService(EvaluationJobRepository evaluationJobRepository,
                                SubmissionRepository submissionRepository) {
        this.evaluationJobRepository = evaluationJobRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional
    public EvaluationJobEntity createQueuedJob(UUID submissionId, String traceId) {
        if (!submissionRepository.existsById(submissionId)) {
            throw new SubmissionNotFoundException(submissionId);
        }

        boolean hasActiveJob = evaluationJobRepository.existsBySubmissionIdAndStatusIn(submissionId, ACTIVE_STATUSES);
        if (hasActiveJob) {
            throw new BadRequestException("An active evaluation job already exists for this submission");
        }

        Instant now = Instant.now();

        EvaluationJobEntity job = new EvaluationJobEntity();
        job.setId(UUID.randomUUID());
        job.setSubmissionId(submissionId);
        job.setStatus(EvaluationJobStatus.QUEUED);
        job.setAttempts(0);
        job.setTraceId(traceId);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);

        try {
            return evaluationJobRepository.save(job);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("An active evaluation job already exists for this submission");
        }
    }

    public EvaluationJobEntity getJob(UUID jobId) {
        return evaluationJobRepository.findById(jobId)
                .orElseThrow(() -> new EvaluationJobNotFoundException(jobId));
    }
}
