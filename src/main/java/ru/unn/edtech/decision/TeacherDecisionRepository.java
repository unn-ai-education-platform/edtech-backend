package ru.unn.edtech.decision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeacherDecisionRepository extends JpaRepository<TeacherDecisionEntity, UUID> {

    Optional<TeacherDecisionEntity> findBySubmissionId(UUID submissionId);
}
