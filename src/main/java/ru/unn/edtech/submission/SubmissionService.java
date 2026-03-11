package ru.unn.edtech.submission;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.unn.edtech.decision.TeacherDecisionEntity;
import ru.unn.edtech.decision.TeacherDecisionRepository;
import ru.unn.edtech.rubric.RubricNotFoundException;
import ru.unn.edtech.rubric.RubricRepository;
import ru.unn.edtech.support.UserRole;
import ru.unn.edtech.support.exception.BadRequestException;
import ru.unn.edtech.support.exception.ForbiddenException;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final RubricRepository rubricRepository;
    private final TeacherDecisionRepository teacherDecisionRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                             RubricRepository rubricRepository,
                             TeacherDecisionRepository teacherDecisionRepository) {
        this.submissionRepository = submissionRepository;
        this.rubricRepository = rubricRepository;
        this.teacherDecisionRepository = teacherDecisionRepository;
    }

    @Transactional
    public SubmissionEntity createSubmission(CreateSubmissionCommand command) {
        validate(command);

        if (!rubricRepository.existsById(command.rubricId())) {
            throw new RubricNotFoundException(command.rubricId());
        }

        Instant now = Instant.now();

        SubmissionEntity submission = new SubmissionEntity();
        submission.setId(UUID.randomUUID());
        submission.setRubricId(command.rubricId());
        submission.setStudentId(command.studentId().trim());
        submission.setText(command.text());
        submission.setCreatedAt(now);

        return submissionRepository.save(submission);
    }

    public SubmissionView getSubmission(UUID submissionId, String viewerUserId, UserRole viewerRole) {
        SubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));

        if (viewerRole == null) {
            throw new ForbiddenException();
        }

        if (viewerRole == UserRole.STUDENT && !submission.getStudentId().equals(viewerUserId)) {
            throw new ForbiddenException("Students can only access their own submissions");
        }

        TeacherDecisionEntity decision = teacherDecisionRepository.findBySubmissionId(submissionId)
                .orElse(null);

        SubmissionDecisionView decisionView = decision != null
                ? new SubmissionDecisionView(
                decision.getMode(),
                decision.getCriteriaResults(),
                decision.getComment(),
                decision.getTotalScoreNormalized(),
                decision.getFinalGrade(),
                decision.getDecidedAt())
                : null;

        SubmissionVisibleStatus status = decisionView != null
                ? SubmissionVisibleStatus.FINAL_READY
                : SubmissionVisibleStatus.UNDER_REVIEW;

        return new SubmissionView(
                submission.getId(),
                submission.getRubricId(),
                status,
                submission.getCreatedAt(),
                decisionView
        );
    }

    private void validate(CreateSubmissionCommand command) {
        if (command.rubricId() == null) {
            throw new BadRequestException("rubricId must not be null");
        }

        if (command.studentId() == null || command.studentId().isBlank()) {
            throw new BadRequestException("studentId must not be blank");
        }

        if (command.text() == null || command.text().isBlank()) {
            throw new BadRequestException("text must not be blank");
        }
    }
}
