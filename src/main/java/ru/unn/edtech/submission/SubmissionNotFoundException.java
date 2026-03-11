package ru.unn.edtech.submission;

import ru.unn.edtech.support.exception.NotFoundException;

import java.util.UUID;

public class SubmissionNotFoundException extends NotFoundException {

    public SubmissionNotFoundException(UUID submissionId) {
        super("SUBMISSION_NOT_FOUND", "Submission not found: " + submissionId);
    }
}
