package ru.unn.edtech.rubric;

import org.junit.jupiter.api.Test;
import ru.unn.edtech.support.exception.BadRequestException;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RubricServiceTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void createRubricPersistsRubricWhenWeightsSumToHundred() {
        RecordingRepository recordingRepository = new RecordingRepository();
        RubricService rubricService = new RubricService(recordingRepository.repository(), jsonMapper);

        RubricEntity saved = rubricService.createRubric(new CreateRubricCommand(
                "Essay rubric",
                List.of(
                        new RubricCriterion("Content", null, 60),
                        new RubricCriterion("Style", "Clarity and grammar", 40)
                ),
                gradeScheme(),
                "teacher-1"
        ));

        RubricEntity persisted = recordingRepository.lastSaved();
        assertThat(saved.getId()).isNotNull();
        assertThat(persisted).isNotNull();
        assertThat(persisted.getName()).isEqualTo("Essay rubric");
        assertThat(persisted.getCreatedBy()).isEqualTo("teacher-1");
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(persisted.getCriteria().get(0).get("name").asText()).isEqualTo("Content");
    }

    @Test
    void createRubricRejectsInvalidWeightSum() {
        RubricService rubricService = new RubricService(new RecordingRepository().repository(), jsonMapper);

        assertThatThrownBy(() -> rubricService.createRubric(new CreateRubricCommand(
                "Essay rubric",
                List.of(
                        new RubricCriterion("Content", null, 70),
                        new RubricCriterion("Style", null, 20)
                ),
                gradeScheme(),
                "teacher-1"
        ))).isInstanceOf(BadRequestException.class)
                .hasMessage("Criterion weights must sum to 100");
    }

    @Test
    void getRubricThrowsWhenRubricIsMissing() {
        RubricService rubricService = new RubricService(missingRepository(), jsonMapper);
        UUID rubricId = UUID.randomUUID();

        assertThatThrownBy(() -> rubricService.getRubric(rubricId))
                .isInstanceOf(RubricNotFoundException.class)
                .hasMessageContaining(rubricId.toString());
    }

    @Test
    void updateRubricChangesOnlyProvidedFields() {
        RecordingRepository recordingRepository = new RecordingRepository();
        recordingRepository.store(existingRubric());
        RubricService rubricService = new RubricService(recordingRepository.repository(), jsonMapper);

        RubricEntity updated = rubricService.updateRubric(
                recordingRepository.lastSaved().getId(),
                new UpdateRubricCommand("Updated rubric", null, null)
        );

        assertThat(updated.getName()).isEqualTo("Updated rubric");
        assertThat(updated.getCriteria().get(0).get("name").asText()).isEqualTo("Content");
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
    }

    private RubricRepository missingRepository() {
        return (RubricRepository) Proxy.newProxyInstance(
                RubricRepository.class.getClassLoader(),
                new Class[]{RubricRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.empty();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private ObjectNode gradeScheme() {
        ObjectNode gradeScheme = JsonNodeFactory.instance.objectNode();
        ArrayNode bands = gradeScheme.putArray("bands");
        bands.addObject().put("grade", "A").put("minScore", 90);
        return gradeScheme;
    }

    private RubricEntity existingRubric() {
        RubricEntity rubric = new RubricEntity();
        rubric.setId(UUID.randomUUID());
        rubric.setName("Existing rubric");
        rubric.setCriteria(jsonMapper.valueToTree(List.of(
                new RubricCriterion("Content", null, 60),
                new RubricCriterion("Style", null, 40)
        )));
        rubric.setGradeScheme(gradeScheme());
        rubric.setCreatedBy("teacher-1");
        rubric.setCreatedAt(java.time.Instant.parse("2026-03-11T12:00:00Z"));
        rubric.setUpdatedAt(java.time.Instant.parse("2026-03-11T12:00:00Z"));
        return rubric;
    }

    private static final class RecordingRepository {
        private RubricEntity lastSaved;

        private RubricRepository repository() {
            return (RubricRepository) Proxy.newProxyInstance(
                    RubricRepository.class.getClassLoader(),
                    new Class[]{RubricRepository.class},
                    (proxy, method, args) -> {
                        if ("save".equals(method.getName())) {
                            lastSaved = (RubricEntity) args[0];
                            return lastSaved;
                        }
                        if ("findById".equals(method.getName())) {
                            return Optional.ofNullable(lastSaved);
                        }
                        throw new UnsupportedOperationException(method.getName());
                    }
            );
        }

        private RubricEntity lastSaved() {
            return lastSaved;
        }

        private void store(RubricEntity rubric) {
            this.lastSaved = rubric;
        }
    }
}
