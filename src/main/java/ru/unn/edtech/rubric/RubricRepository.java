package ru.unn.edtech.rubric;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RubricRepository extends JpaRepository<RubricEntity, UUID> {
}
