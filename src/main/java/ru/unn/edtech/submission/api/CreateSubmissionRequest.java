package ru.unn.edtech.submission.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSubmissionRequest(
        @NotNull UUID rubricId,
        @NotBlank String text
) {}
