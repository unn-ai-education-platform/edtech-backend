package ru.unn.edtech.rubric.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.unn.edtech.support.Access;
import ru.unn.edtech.support.RequestContext;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;
import ru.unn.edtech.rubric.CreateRubricCommand;
import ru.unn.edtech.rubric.RubricCriterion;
import ru.unn.edtech.rubric.RubricEntity;
import ru.unn.edtech.rubric.RubricService;
import ru.unn.edtech.rubric.UpdateRubricCommand;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rubrics")
public class RubricController {

    private final RubricService rubricService;
    private final JsonMapper jsonMapper;

    public RubricController(RubricService rubricService, JsonMapper jsonMapper) {
        this.rubricService = rubricService;
        this.jsonMapper = jsonMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RubricResponse createRubric(@Valid @RequestBody CreateRubricRequest request, HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);
        Access.requireTeacher(ctx);

        RubricEntity rubric = rubricService.createRubric(new CreateRubricCommand(
                request.name(),
                request.criteria().stream()
                        .map(criterion -> new RubricCriterion(criterion.name(), criterion.description(), criterion.weight()))
                        .toList(),
                request.gradeScheme(),
                ctx.getUserId()
        ));

        return toResponse(rubric);
    }

    @GetMapping("/{rubricId}")
    public RubricResponse getRubric(@PathVariable UUID rubricId, HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);
        Access.requireTeacher(ctx);

        return toResponse(rubricService.getRubric(rubricId));
    }

    @PatchMapping("/{rubricId}")
    public RubricResponse updateRubric(@PathVariable UUID rubricId,
                                       @Valid @RequestBody UpdateRubricRequest request,
                                       HttpServletRequest servletRequest) {
        RequestContext ctx = requestContext(servletRequest);
        Access.requireTeacher(ctx);

        RubricEntity rubric = rubricService.updateRubric(
                rubricId,
                new UpdateRubricCommand(
                        request.name(),
                        request.criteria() != null
                                ? request.criteria().stream()
                                .map(criterion -> new RubricCriterion(
                                        criterion.name(),
                                        criterion.description(),
                                        criterion.weight()))
                                .toList()
                                : null,
                        request.gradeScheme()
                )
        );

        return toResponse(rubric);
    }

    private RequestContext requestContext(HttpServletRequest request) {
        return (RequestContext) request.getAttribute(HeaderRequestContextFilter.REQUEST_CONTEXT_ATTR);
    }

    private RubricResponse toResponse(RubricEntity rubric) {
        List<RubricCriterionPayload> criteria = jsonMapper.convertValue(
                rubric.getCriteria(),
                new TypeReference<List<RubricCriterionPayload>>() {}
        );

        return new RubricResponse(
                rubric.getId(),
                rubric.getName(),
                criteria,
                rubric.getGradeScheme(),
                rubric.getCreatedAt(),
                rubric.getUpdatedAt()
        );
    }
}
