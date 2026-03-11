package ru.unn.edtech.submission;

import org.junit.jupiter.api.Test;
import ru.unn.edtech.decision.TeacherDecisionEntity;
import ru.unn.edtech.decision.TeacherDecisionRepository;
import ru.unn.edtech.rubric.RubricRepository;
import ru.unn.edtech.support.UserRole;
import ru.unn.edtech.support.exception.ForbiddenException;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmissionServiceTest {

    @Test
    void createSubmissionPersistsSubmissionForExistingRubric() {
        RecordingSubmissionRepository submissionRepository = new RecordingSubmissionRepository();
        SubmissionService submissionService = new SubmissionService(
                submissionRepository.repository(),
                rubricRepository(true),
                teacherDecisionRepository(Optional.empty())
        );

        SubmissionEntity saved = submissionService.createSubmission(new CreateSubmissionCommand(
                UUID.randomUUID(),
                "student-1",
                "Essay text"
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStudentId()).isEqualTo("student-1");
        assertThat(saved.getText()).isEqualTo("Essay text");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void getSubmissionRejectsForeignStudent() {
        RecordingSubmissionRepository submissionRepository = new RecordingSubmissionRepository();
        submissionRepository.store(existingSubmission());
        SubmissionService submissionService = new SubmissionService(
                submissionRepository.repository(),
                rubricRepository(true),
                teacherDecisionRepository(Optional.empty())
        );

        assertThatThrownBy(() -> submissionService.getSubmission(
                submissionRepository.lastSaved().getId(),
                "student-2",
                UserRole.STUDENT))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Students can only access their own submissions");
    }

    @Test
    void getSubmissionReturnsFinalReadyWhenDecisionExists() {
        RecordingSubmissionRepository submissionRepository = new RecordingSubmissionRepository();
        SubmissionEntity submission = existingSubmission();
        submissionRepository.store(submission);

        TeacherDecisionEntity decision = new TeacherDecisionEntity();
        decision.setId(UUID.randomUUID());
        decision.setSubmissionId(submission.getId());
        decision.setMode("OVERRIDE");
        decision.setCriteriaResults(JsonNodeFactory.instance.arrayNode());
        decision.setComment("Final comment");
        decision.setTotalScoreNormalized(new BigDecimal("84.50"));
        decision.setFinalGrade(gradeNode());
        decision.setDecidedBy("teacher-1");
        decision.setDecidedAt(Instant.parse("2026-03-11T15:00:00Z"));

        SubmissionService submissionService = new SubmissionService(
                submissionRepository.repository(),
                rubricRepository(true),
                teacherDecisionRepository(Optional.of(decision))
        );

        SubmissionView view = submissionService.getSubmission(submission.getId(), "teacher-1", UserRole.TEACHER);

        assertThat(view.status()).isEqualTo(SubmissionVisibleStatus.FINAL_READY);
        assertThat(view.decision()).isNotNull();
        assertThat(view.decision().comment()).isEqualTo("Final comment");
    }

    private SubmissionEntity existingSubmission() {
        SubmissionEntity submission = new SubmissionEntity();
        submission.setId(UUID.randomUUID());
        submission.setRubricId(UUID.randomUUID());
        submission.setStudentId("student-1");
        submission.setText("Sensitive essay text");
        submission.setCreatedAt(Instant.parse("2026-03-11T12:00:00Z"));
        return submission;
    }

    private ObjectNode gradeNode() {
        ObjectNode grade = JsonNodeFactory.instance.objectNode();
        grade.put("grade", "B");
        return grade;
    }

    private RubricRepository rubricRepository(boolean exists) {
        return (RubricRepository) Proxy.newProxyInstance(
                RubricRepository.class.getClassLoader(),
                new Class[]{RubricRepository.class},
                (proxy, method, args) -> {
                    if ("existsById".equals(method.getName())) {
                        return exists;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private TeacherDecisionRepository teacherDecisionRepository(Optional<TeacherDecisionEntity> decision) {
        return (TeacherDecisionRepository) Proxy.newProxyInstance(
                TeacherDecisionRepository.class.getClassLoader(),
                new Class[]{TeacherDecisionRepository.class},
                (proxy, method, args) -> {
                    if ("findBySubmissionId".equals(method.getName())) {
                        return decision;
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static final class RecordingSubmissionRepository {
        private SubmissionEntity lastSaved;

        private SubmissionRepository repository() {
            return (SubmissionRepository) Proxy.newProxyInstance(
                    SubmissionRepository.class.getClassLoader(),
                    new Class[]{SubmissionRepository.class},
                    (proxy, method, args) -> {
                        if ("save".equals(method.getName())) {
                            lastSaved = (SubmissionEntity) args[0];
                            return lastSaved;
                        }
                        if ("findById".equals(method.getName())) {
                            return Optional.ofNullable(lastSaved);
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private SubmissionEntity lastSaved() {
            return lastSaved;
        }

        private void store(SubmissionEntity submission) {
            this.lastSaved = submission;
        }
    }
}
