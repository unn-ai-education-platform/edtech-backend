package ru.unn.edtech.evaluation;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import ru.unn.edtech.submission.SubmissionRepository;
import ru.unn.edtech.support.exception.BadRequestException;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluationJobServiceTest {

    @Test
    void createQueuedJobPersistsQueuedJobWithTraceId() {
        RecordingEvaluationJobRepository evaluationJobRepository = new RecordingEvaluationJobRepository();
        EvaluationJobService service = new EvaluationJobService(
                evaluationJobRepository.repository(),
                submissionRepository(true)
        );

        EvaluationJobEntity job = service.createQueuedJob(UUID.randomUUID(), "trace-123");

        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo(EvaluationJobStatus.QUEUED);
        assertThat(job.getAttempts()).isZero();
        assertThat(job.getTraceId()).isEqualTo("trace-123");
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotNull();
    }

    @Test
    void createQueuedJobRejectsSecondActiveJob() {
        EvaluationJobService service = new EvaluationJobService(
                activeJobRepository(),
                submissionRepository(true)
        );

        assertThatThrownBy(() -> service.createQueuedJob(UUID.randomUUID(), "trace-123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("An active evaluation job already exists for this submission");
    }

    @Test
    void createQueuedJobMapsDbUniqueConflictToBadRequest() {
        EvaluationJobService service = new EvaluationJobService(
                conflictingSaveRepository(),
                submissionRepository(true)
        );

        assertThatThrownBy(() -> service.createQueuedJob(UUID.randomUUID(), "trace-123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("An active evaluation job already exists for this submission");
    }

    private EvaluationJobRepository activeJobRepository() {
        return (EvaluationJobRepository) Proxy.newProxyInstance(
                EvaluationJobRepository.class.getClassLoader(),
                new Class[]{EvaluationJobRepository.class},
                (proxy, method, args) -> {
                    if ("existsBySubmissionIdAndStatusIn".equals(method.getName())) {
                        return true;
                    }
                    if ("findById".equals(method.getName())) {
                        return Optional.empty();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private SubmissionRepository submissionRepository(boolean exists) {
        return (SubmissionRepository) Proxy.newProxyInstance(
                SubmissionRepository.class.getClassLoader(),
                new Class[]{SubmissionRepository.class},
                (proxy, method, args) -> {
                    if ("existsById".equals(method.getName())) {
                        return exists;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private EvaluationJobRepository conflictingSaveRepository() {
        return (EvaluationJobRepository) Proxy.newProxyInstance(
                EvaluationJobRepository.class.getClassLoader(),
                new Class[]{EvaluationJobRepository.class},
                (proxy, method, args) -> {
                    if ("existsBySubmissionIdAndStatusIn".equals(method.getName())) {
                        return false;
                    }
                    if ("save".equals(method.getName())) {
                        throw new DataIntegrityViolationException("duplicate key");
                    }
                    if ("findById".equals(method.getName())) {
                        return Optional.empty();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static final class RecordingEvaluationJobRepository {
        private EvaluationJobEntity lastSaved;

        private EvaluationJobRepository repository() {
            return (EvaluationJobRepository) Proxy.newProxyInstance(
                    EvaluationJobRepository.class.getClassLoader(),
                    new Class[]{EvaluationJobRepository.class},
                    (proxy, method, args) -> {
                        if ("save".equals(method.getName())) {
                            lastSaved = (EvaluationJobEntity) args[0];
                            return lastSaved;
                        }
                        if ("existsBySubmissionIdAndStatusIn".equals(method.getName())) {
                            return false;
                        }
                        if ("findById".equals(method.getName())) {
                            return Optional.ofNullable(lastSaved);
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
