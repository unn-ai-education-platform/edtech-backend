package ru.unn.edtech.evaluation.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.unn.edtech.evaluation.EvaluationJobEntity;
import ru.unn.edtech.evaluation.EvaluationJobService;
import ru.unn.edtech.support.Access;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class EvaluationJobController {

    private final EvaluationJobService evaluationJobService;

    public EvaluationJobController(EvaluationJobService evaluationJobService) {
        this.evaluationJobService = evaluationJobService;
    }

    @GetMapping("/{jobId}")
    public EvaluationJobResponse getJob(@PathVariable UUID jobId, HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);
        Access.requireTeacher(ctx);

        EvaluationJobEntity job = evaluationJobService.getJob(jobId);
        return new EvaluationJobResponse(job.getId(), job.getStatus());
    }

    private RequestContext requestContext(HttpServletRequest request) {
        return (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);
    }
}
