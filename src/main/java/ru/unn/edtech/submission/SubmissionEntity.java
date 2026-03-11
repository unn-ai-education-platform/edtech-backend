package ru.unn.edtech.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
public class SubmissionEntity {

    @Id
    private UUID id;

    @Column(name = "rubric_id", nullable = false)
    private UUID rubricId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
