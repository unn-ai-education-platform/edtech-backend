package ru.unn.edtech.submission.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.unn.edtech.evaluation.EvaluationJobEntity;
import ru.unn.edtech.evaluation.EvaluationJobService;
import ru.unn.edtech.submission.CreateSubmissionCommand;
import ru.unn.edtech.submission.SubmissionEntity;
import ru.unn.edtech.submission.SubmissionService;
import ru.unn.edtech.submission.SubmissionView;
import ru.unn.edtech.support.Access;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final EvaluationJobService evaluationJobService;

    public SubmissionController(SubmissionService submissionService, EvaluationJobService evaluationJobService) {
        this.submissionService = submissionService;
        this.evaluationJobService = evaluationJobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSubmissionResponse createSubmission(@Valid @RequestBody CreateSubmissionRequest request,
                                                     HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);
        Access.requireStudent(ctx);

        SubmissionEntity submission = submissionService.createSubmission(new CreateSubmissionCommand(
                request.rubricId(),
                ctx.getUserId(),
                request.text()
        ));

        return new CreateSubmissionResponse(submission.getId(), submission.getCreatedAt());
    }

    @GetMapping("/{submissionId}")
    public SubmissionResponse getSubmission(@PathVariable UUID submissionId, HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);

        SubmissionView submission = submissionService.getSubmission(submissionId, ctx.getUserId(), ctx.getRole());

        return SubmissionResponse.from(submission);
    }

    @PostMapping("/{submissionId}/evaluate")
    public StartEvaluationResponse startEvaluation(@PathVariable UUID submissionId, HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);
        Access.requireTeacher(ctx);

        EvaluationJobEntity job = evaluationJobService.createQueuedJob(submissionId, ctx.getTraceId());
        return new StartEvaluationResponse(job.getId(), job.getStatus());
    }

    private RequestContext requestContext(HttpServletRequest request) {
        return (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);
    }
}
