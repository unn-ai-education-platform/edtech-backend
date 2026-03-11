package ru.unn.edtech.submission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, UUID> {
}
