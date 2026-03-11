package ru.unn.edtech.rubric;

import ru.unn.edtech.support.exception.NotFoundException;

import java.util.UUID;

public class RubricNotFoundException extends NotFoundException {

    public RubricNotFoundException(UUID rubricId) {
        super("RUBRIC_NOT_FOUND", "Rubric not found: " + rubricId);
    }
}
