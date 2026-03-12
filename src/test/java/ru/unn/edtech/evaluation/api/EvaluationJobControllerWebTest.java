package ru.unn.edtech.evaluation.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.unn.edtech.evaluation.EvaluationJobEntity;
import ru.unn.edtech.evaluation.EvaluationJobNotFoundException;
import ru.unn.edtech.evaluation.EvaluationJobService;
import ru.unn.edtech.evaluation.EvaluationJobStatus;
import ru.unn.edtech.support.web.ApiExceptionHandler;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EvaluationJobControllerWebTest {

    private final StubEvaluationJobService evaluationJobService = new StubEvaluationJobService();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new EvaluationJobController(evaluationJobService))
            .setControllerAdvice(new ApiExceptionHandler())
            .addFilters(new HeaderRequestContextFilter(JsonMapper.builder().build()))
            .build();

    @Test
    void getJobReturnsInternalStatusForTeacher() throws Exception {
        UUID jobId = UUID.randomUUID();
        EvaluationJobEntity job = new EvaluationJobEntity();
        job.setId(jobId);
        job.setSubmissionId(UUID.randomUUID());
        job.setStatus(EvaluationJobStatus.QUEUED);
        job.setAttempts(0);
        job.setCreatedAt(Instant.parse("2026-03-11T12:00:00Z"));
        job.setUpdatedAt(Instant.parse("2026-03-11T12:00:00Z"));
        evaluationJobService.result = job;

        mockMvc.perform(get("/api/v1/jobs/{jobId}", jobId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void getJobReturnsForbiddenForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/{jobId}", UUID.randomUUID())
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void getJobReturnsNotFoundWhenMissing() throws Exception {
        UUID jobId = UUID.randomUUID();
        evaluationJobService.missingJobId = jobId;

        mockMvc.perform(get("/api/v1/jobs/{jobId}", jobId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    private static final class StubEvaluationJobService extends EvaluationJobService {
        private EvaluationJobEntity result;
        private UUID missingJobId;

        private StubEvaluationJobService() {
            super(unsupportedJobRepository(), unsupportedSubmissionRepository());
        }

        @Override
        public EvaluationJobEntity getJob(UUID jobId) {
            if (jobId.equals(missingJobId)) {
                throw new EvaluationJobNotFoundException(jobId);
            }
            return result;
        }

        private static ru.unn.edtech.evaluation.EvaluationJobRepository unsupportedJobRepository() {
            return (ru.unn.edtech.evaluation.EvaluationJobRepository) java.lang.reflect.Proxy.newProxyInstance(
                    ru.unn.edtech.evaluation.EvaluationJobRepository.class.getClassLoader(),
                    new Class[]{ru.unn.edtech.evaluation.EvaluationJobRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private static ru.unn.edtech.submission.SubmissionRepository unsupportedSubmissionRepository() {
            return (ru.unn.edtech.submission.SubmissionRepository) java.lang.reflect.Proxy.newProxyInstance(
                    ru.unn.edtech.submission.SubmissionRepository.class.getClassLoader(),
                    new Class[]{ru.unn.edtech.submission.SubmissionRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
