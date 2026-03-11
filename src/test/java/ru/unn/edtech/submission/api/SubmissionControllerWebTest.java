package ru.unn.edtech.submission.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.unn.edtech.rubric.RubricNotFoundException;
import ru.unn.edtech.evaluation.EvaluationJobEntity;
import ru.unn.edtech.evaluation.EvaluationJobService;
import ru.unn.edtech.evaluation.EvaluationJobStatus;
import ru.unn.edtech.submission.CreateSubmissionCommand;
import ru.unn.edtech.submission.SubmissionEntity;
import ru.unn.edtech.submission.SubmissionNotFoundException;
import ru.unn.edtech.submission.SubmissionService;
import ru.unn.edtech.submission.SubmissionView;
import ru.unn.edtech.submission.SubmissionVisibleStatus;
import ru.unn.edtech.support.exception.ForbiddenException;
import ru.unn.edtech.support.web.ApiExceptionHandler;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubmissionControllerWebTest {

    private final StubSubmissionService submissionService = new StubSubmissionService();
    private final StubEvaluationJobService evaluationJobService = new StubEvaluationJobService();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SubmissionController(submissionService, evaluationJobService))
            .setControllerAdvice(new ApiExceptionHandler())
            .addFilters(new HeaderRequestContextFilter(tools.jackson.databind.json.JsonMapper.builder().build()))
            .build();

    @Test
    void createSubmissionReturnsCreatedForStudent() throws Exception {
        SubmissionEntity submission = submission(UUID.randomUUID(), "student-1");
        submissionService.createResult = submission;

        mockMvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT")
                        .header("X-Request-Id", "req-submission-create")
                        .content("""
                                {
                                  "rubricId": "%s",
                                  "text": "Essay text"
                                }
                                """.formatted(submission.getRubricId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Request-Id", "req-submission-create"))
                .andExpect(jsonPath("$.submissionId").value(submission.getId().toString()));
    }

    @Test
    void createSubmissionReturnsForbiddenForTeacher() throws Exception {
        mockMvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .content("""
                                {
                                  "rubricId": "%s",
                                  "text": "Essay text"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void createSubmissionReturnsRubricNotFoundWithoutLeakingText() throws Exception {
        UUID rubricId = UUID.randomUUID();
        submissionService.missingRubricId = rubricId;

        mockMvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT")
                        .header("X-Request-Id", "req-rubric-not-found")
                        .content("""
                                {
                                  "rubricId": "%s",
                                  "text": "secret essay text"
                                }
                                """.formatted(rubricId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RUBRIC_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId").value("req-rubric-not-found"))
                .andExpect(jsonPath("$.message", not(containsString("secret essay text"))));
    }

    @Test
    void getSubmissionReturnsUnderReviewForOwningStudent() throws Exception {
        UUID submissionId = UUID.randomUUID();
        submissionService.getResult = new SubmissionView(
                submissionId,
                UUID.randomUUID(),
                SubmissionVisibleStatus.UNDER_REVIEW,
                Instant.parse("2026-03-11T12:00:00Z"),
                null
        );

        mockMvc.perform(get("/api/v1/submissions/{submissionId}", submissionId)
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.decision").value(nullValue()));
    }

    @Test
    void getSubmissionReturnsFinalReadyWithDecisionPayload() throws Exception {
        UUID submissionId = UUID.randomUUID();
        submissionService.getResult = new SubmissionView(
                submissionId,
                UUID.randomUUID(),
                SubmissionVisibleStatus.FINAL_READY,
                Instant.parse("2026-03-11T12:00:00Z"),
                new ru.unn.edtech.submission.SubmissionDecisionView(
                        "OVERRIDE",
                        tools.jackson.databind.node.JsonNodeFactory.instance.arrayNode(),
                        "Teacher comment",
                        new java.math.BigDecimal("84.50"),
                        tools.jackson.databind.node.JsonNodeFactory.instance.objectNode().put("grade", "B"),
                        Instant.parse("2026-03-11T15:00:00Z")
                )
        );

        mockMvc.perform(get("/api/v1/submissions/{submissionId}", submissionId)
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINAL_READY"))
                .andExpect(jsonPath("$.decision.mode").value("OVERRIDE"))
                .andExpect(jsonPath("$.decision.comment").value("Teacher comment"))
                .andExpect(jsonPath("$.decision.totalScoreNormalized").value(84.50));
    }

    @Test
    void getSubmissionReturnsForbiddenForForeignStudent() throws Exception {
        UUID submissionId = UUID.randomUUID();
        submissionService.forbiddenSubmissionId = submissionId;

        mockMvc.perform(get("/api/v1/submissions/{submissionId}", submissionId)
                        .header("X-User-Id", "student-2")
                        .header("X-User-Role", "STUDENT")
                        .header("X-Request-Id", "req-foreign-submission"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.traceId").value("req-foreign-submission"));
    }

    @Test
    void getSubmissionReturnsNotFoundWhenMissing() throws Exception {
        UUID submissionId = UUID.randomUUID();
        submissionService.missingSubmissionId = submissionId;

        mockMvc.perform(get("/api/v1/submissions/{submissionId}", submissionId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBMISSION_NOT_FOUND"));
    }

    @Test
    void startEvaluationReturnsQueuedJobForTeacher() throws Exception {
        UUID submissionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        EvaluationJobEntity job = new EvaluationJobEntity();
        job.setId(jobId);
        job.setSubmissionId(submissionId);
        job.setStatus(EvaluationJobStatus.QUEUED);
        evaluationJobService.createResult = job;

        mockMvc.perform(post("/api/v1/submissions/{submissionId}/evaluate", submissionId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .header("X-Request-Id", "trace-evaluate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void startEvaluationReturnsForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/v1/submissions/{submissionId}/evaluate", UUID.randomUUID())
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void startEvaluationReturnsNotFoundForMissingSubmission() throws Exception {
        UUID submissionId = UUID.randomUUID();
        evaluationJobService.missingSubmissionId = submissionId;

        mockMvc.perform(post("/api/v1/submissions/{submissionId}/evaluate", submissionId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBMISSION_NOT_FOUND"));
    }

    @Test
    void startEvaluationReturnsBadRequestWhenActiveJobExists() throws Exception {
        UUID submissionId = UUID.randomUUID();
        evaluationJobService.duplicateSubmissionId = submissionId;

        mockMvc.perform(post("/api/v1/submissions/{submissionId}/evaluate", submissionId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .header("X-Request-Id", "trace-duplicate-job"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.traceId").value("trace-duplicate-job"));
    }

    private SubmissionEntity submission(UUID submissionId, String studentId) {
        SubmissionEntity submission = new SubmissionEntity();
        submission.setId(submissionId);
        submission.setRubricId(UUID.randomUUID());
        submission.setStudentId(studentId);
        submission.setText("Essay text");
        submission.setCreatedAt(Instant.parse("2026-03-11T12:00:00Z"));
        return submission;
    }

    private static final class StubEvaluationJobService extends EvaluationJobService {
        private EvaluationJobEntity createResult;
        private UUID missingSubmissionId;
        private UUID duplicateSubmissionId;

        private StubEvaluationJobService() {
            super(unsupportedEvaluationJobRepository(), unsupportedSubmissionRepositoryForEvaluation());
        }

        @Override
        public EvaluationJobEntity createQueuedJob(UUID submissionId, String traceId) {
            if (submissionId.equals(missingSubmissionId)) {
                throw new SubmissionNotFoundException(submissionId);
            }
            if (submissionId.equals(duplicateSubmissionId)) {
                throw new ru.unn.edtech.support.exception.BadRequestException(
                        "An active evaluation job already exists for this submission");
            }
            return createResult;
        }

        private static ru.unn.edtech.evaluation.EvaluationJobRepository unsupportedEvaluationJobRepository() {
            return (ru.unn.edtech.evaluation.EvaluationJobRepository) java.lang.reflect.Proxy.newProxyInstance(
                    ru.unn.edtech.evaluation.EvaluationJobRepository.class.getClassLoader(),
                    new Class[]{ru.unn.edtech.evaluation.EvaluationJobRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private static ru.unn.edtech.submission.SubmissionRepository unsupportedSubmissionRepositoryForEvaluation() {
            return (ru.unn.edtech.submission.SubmissionRepository) java.lang.reflect.Proxy.newProxyInstance(
                    ru.unn.edtech.submission.SubmissionRepository.class.getClassLoader(),
                    new Class[]{ru.unn.edtech.submission.SubmissionRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class StubSubmissionService extends SubmissionService {
        private SubmissionEntity createResult;
        private SubmissionView getResult;
        private UUID missingRubricId;
        private UUID missingSubmissionId;
        private UUID forbiddenSubmissionId;

        private StubSubmissionService() {
            super(unsupportedSubmissionRepository(), unsupportedRubricRepository(), unsupportedDecisionRepository());
        }

        @Override
        public SubmissionEntity createSubmission(CreateSubmissionCommand command) {
            if (command.rubricId().equals(missingRubricId)) {
                throw new RubricNotFoundException(command.rubricId());
            }
            return createResult;
        }

        @Override
        public SubmissionView getSubmission(UUID submissionId, String viewerUserId, ru.unn.edtech.support.UserRole viewerRole) {
            if (submissionId.equals(missingSubmissionId)) {
                throw new SubmissionNotFoundException(submissionId);
            }
            if (submissionId.equals(forbiddenSubmissionId)) {
                throw new ForbiddenException("Students can only access their own submissions");
            }
            return getResult;
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

        private static ru.unn.edtech.rubric.RubricRepository unsupportedRubricRepository() {
            return (ru.unn.edtech.rubric.RubricRepository) java.lang.reflect.Proxy.newProxyInstance(
                    ru.unn.edtech.rubric.RubricRepository.class.getClassLoader(),
                    new Class[]{ru.unn.edtech.rubric.RubricRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private static ru.unn.edtech.decision.TeacherDecisionRepository unsupportedDecisionRepository() {
            return (ru.unn.edtech.decision.TeacherDecisionRepository) java.lang.reflect.Proxy.newProxyInstance(
                    ru.unn.edtech.decision.TeacherDecisionRepository.class.getClassLoader(),
                    new Class[]{ru.unn.edtech.decision.TeacherDecisionRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
