package ru.unn.edtech.rubric.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.unn.edtech.rubric.CreateRubricCommand;
import ru.unn.edtech.rubric.RubricEntity;
import ru.unn.edtech.rubric.RubricService;
import ru.unn.edtech.rubric.RubricNotFoundException;
import ru.unn.edtech.rubric.RubricRepository;
import ru.unn.edtech.support.web.ApiExceptionHandler;
import ru.unn.edtech.support.web.HeaderRequestContextFilter;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RubricControllerWebTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final StubRubricService rubricService = new StubRubricService(jsonMapper);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new RubricController(rubricService, jsonMapper))
            .setControllerAdvice(new ApiExceptionHandler())
            .addFilters(new HeaderRequestContextFilter(jsonMapper))
            .build();

    @Test
    void createRubricReturnsCreatedForTeacher() throws Exception {
        UUID rubricId = UUID.randomUUID();
        rubricService.createResult = rubric(rubricId);

        mockMvc.perform(post("/api/v1/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .header("X-Request-Id", "req-rubric-create")
                        .content("""
                                {
                                  "name": "Essay rubric",
                                  "criteria": [
                                    {"name": "Content", "description": null, "weight": 60},
                                    {"name": "Style", "description": "Clarity", "weight": 40}
                                  ],
                                  "gradeScheme": {
                                    "bands": [
                                      {"grade": "A", "minScore": 90}
                                    ]
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Request-Id", "req-rubric-create"))
                .andExpect(jsonPath("$.id").value(rubricId.toString()))
                .andExpect(jsonPath("$.name").value("Essay rubric"))
                .andExpect(jsonPath("$.criteria[0].weight").value(60));
    }

    @Test
    void createRubricReturnsForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/v1/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "student-1")
                        .header("X-User-Role", "STUDENT")
                        .content("""
                                {
                                  "name": "Essay rubric",
                                  "criteria": [{"name": "Content", "description": null, "weight": 100}],
                                  "gradeScheme": {"bands": []}
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void createRubricReturnsBadRequestWhenHeadersMissing() throws Exception {
        mockMvc.perform(post("/api/v1/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Request-Id", "req-missing")
                        .content("""
                                {
                                  "name": "Essay rubric",
                                  "criteria": [{"name": "Content", "description": null, "weight": 100}],
                                  "gradeScheme": {"bands": []}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_HEADER"))
                .andExpect(jsonPath("$.traceId").value("req-missing"));
    }

    @Test
    void getRubricReturnsTeacherOnlyView() throws Exception {
        UUID rubricId = UUID.randomUUID();
        rubricService.getResult = rubric(rubricId);

        mockMvc.perform(get("/api/v1/rubrics/{rubricId}", rubricId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rubricId.toString()))
                .andExpect(jsonPath("$.criteria[1].name").value("Style"));
    }

    @Test
    void getRubricReturnsNotFoundWhenMissing() throws Exception {
        UUID rubricId = UUID.randomUUID();
        rubricService.notFoundId = rubricId;

        mockMvc.perform(get("/api/v1/rubrics/{rubricId}", rubricId)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .header("X-Request-Id", "req-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RUBRIC_NOT_FOUND"))
                .andExpect(jsonPath("$.traceId").value("req-not-found"));
    }

    @Test
    void updateRubricReturnsUpdatedRubricForTeacher() throws Exception {
        UUID rubricId = UUID.randomUUID();
        RubricEntity updated = rubric(rubricId);
        updated.setName("Updated rubric");
        rubricService.updateResult = updated;

        mockMvc.perform(patch("/api/v1/rubrics/{rubricId}", rubricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", "teacher-1")
                        .header("X-User-Role", "TEACHER")
                        .content("""
                                {
                                  "name": "Updated rubric"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rubricId.toString()))
                .andExpect(jsonPath("$.name").value("Updated rubric"));
    }

    private RubricEntity rubric(UUID rubricId) {
        RubricEntity rubric = new RubricEntity();
        rubric.setId(rubricId);
        rubric.setName("Essay rubric");
        rubric.setCreatedBy("teacher-1");
        rubric.setCreatedAt(Instant.parse("2026-03-11T12:00:00Z"));
        rubric.setUpdatedAt(Instant.parse("2026-03-11T12:00:00Z"));
        rubric.setCriteria(jsonMapper.valueToTree(java.util.List.of(
                new RubricCriterionPayload("Content", null, 60),
                new RubricCriterionPayload("Style", "Clarity", 40)
        )));
        ObjectNode gradeScheme = JsonNodeFactory.instance.objectNode();
        gradeScheme.putArray("bands").addObject().put("grade", "A").put("minScore", 90);
        rubric.setGradeScheme(gradeScheme);
        return rubric;
    }

    private static final class StubRubricService extends RubricService {
        private RubricEntity createResult;
        private RubricEntity getResult;
        private RubricEntity updateResult;
        private UUID notFoundId;

        private StubRubricService(JsonMapper jsonMapper) {
            super(unsupportedRepository(), jsonMapper);
        }

        @Override
        public RubricEntity createRubric(CreateRubricCommand command) {
            return createResult;
        }

        @Override
        public RubricEntity getRubric(UUID rubricId) {
            if (rubricId.equals(notFoundId)) {
                throw new RubricNotFoundException(rubricId);
            }
            return getResult;
        }

        @Override
        public RubricEntity updateRubric(UUID rubricId, ru.unn.edtech.rubric.UpdateRubricCommand command) {
            if (rubricId.equals(notFoundId)) {
                throw new RubricNotFoundException(rubricId);
            }
            return updateResult;
        }

        private static RubricRepository unsupportedRepository() {
            return (RubricRepository) java.lang.reflect.Proxy.newProxyInstance(
                    RubricRepository.class.getClassLoader(),
                    new Class[]{RubricRepository.class},
                    (proxy, method, args) -> {
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}
