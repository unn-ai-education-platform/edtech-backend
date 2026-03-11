package ru.unn.edtech.submission;

import java.util.UUID;

public record CreateSubmissionCommand(
        UUID rubricId,
        String studentId,
        String text
) {}
