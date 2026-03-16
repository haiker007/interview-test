package com.ecommerce.featureflag.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule for flag targeting (Entity within Flag Aggregate).
 */
@Entity
@Table(name = "rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id")
    private Flag flag;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type;

    /**
     * Store conditions as JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<Condition> conditions = new ArrayList<>();

    @Column(name = "variation_key")
    private String variation;

    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage;

    /**
     * Store targets (e.g., list of user IDs) as JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private List<String> targets = new ArrayList<>();

    @Column(nullable = false)
    private Integer priority;

    public enum RuleType {
        GRADUAL_ROLLOUT,
        TARGET,
        DEFAULT
    }
}
