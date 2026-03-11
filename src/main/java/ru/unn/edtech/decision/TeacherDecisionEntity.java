package ru.unn.edtech.decision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teacher_decisions")
@Getter
@Setter
@NoArgsConstructor
public class TeacherDecisionEntity {

    @Id
    private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "ai_result_id")
    private UUID aiResultId;

    @Column(nullable = false)
    private String mode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "criteria_results", nullable = false, columnDefinition = "jsonb")
    private JsonNode criteriaResults;

    @Column
    private String comment;

    @Column(name = "total_score_normalized", nullable = false)
    private BigDecimal totalScoreNormalized;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_grade", nullable = false, columnDefinition = "jsonb")
    private JsonNode finalGrade;

    @Column(name = "decided_by", nullable = false)
    private String decidedBy;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;
}
