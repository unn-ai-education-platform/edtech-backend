package ru.unn.edtech.rubric;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.unn.edtech.support.exception.BadRequestException;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RubricService {

    private final RubricRepository rubricRepository;
    private final JsonMapper jsonMapper;

    public RubricService(RubricRepository rubricRepository, JsonMapper jsonMapper) {
        this.rubricRepository = rubricRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public RubricEntity createRubric(CreateRubricCommand command) {
        validate(command);

        Instant now = Instant.now();

        RubricEntity rubric = new RubricEntity();
        rubric.setId(UUID.randomUUID());
        rubric.setName(command.name().trim());
        rubric.setCriteria(jsonMapper.valueToTree(command.criteria()));
        rubric.setGradeScheme(command.gradeScheme().deepCopy());
        rubric.setCreatedBy(command.createdBy());
        rubric.setCreatedAt(now);
        rubric.setUpdatedAt(now);

        return rubricRepository.save(rubric);
    }

    public RubricEntity getRubric(UUID rubricId) {
        return rubricRepository.findById(rubricId)
                .orElseThrow(() -> new RubricNotFoundException(rubricId));
    }

    private void validate(CreateRubricCommand command) {
        if (command.name() == null || command.name().isBlank()) {
            throw new BadRequestException("Rubric name must not be blank");
        }

        if (command.createdBy() == null || command.createdBy().isBlank()) {
            throw new BadRequestException("createdBy must not be blank");
        }

        if (command.criteria() == null || command.criteria().isEmpty()) {
            throw new BadRequestException("Rubric must contain at least one criterion");
        }

        if (command.gradeScheme() == null) {
            throw new BadRequestException("gradeScheme must not be null");
        }

        if (!(command.gradeScheme() instanceof ArrayNode || command.gradeScheme().isObject())) {
            throw new BadRequestException("gradeScheme must be a JSON object or array");
        }

        boolean hasInvalidWeight = command.criteria().stream()
                .mapToInt(RubricCriterion::weight)
                .anyMatch(weight -> weight < 0 || weight > 100);

        if (hasInvalidWeight) {
            throw new BadRequestException("Criterion weight must be in range 0..100");
        }

        int totalWeight = command.criteria().stream()
                .mapToInt(RubricCriterion::weight)
                .sum();

        if (totalWeight != 100) {
            throw new BadRequestException("Criterion weights must sum to 100");
        }

        boolean hasBlankCriterionName = command.criteria().stream()
                .map(RubricCriterion::name)
                .anyMatch(name -> name == null || name.isBlank());

        if (hasBlankCriterionName) {
            throw new BadRequestException("Criterion name must not be blank");
        }
    }
}
