package com.ecommerce.featureflag.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature flag definition (Aggregate Root).
 */
@Entity
@Table(name = "flags")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "flag_key", nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagStatus status;

    /**
     * Store variations as JSON. Hibernate 7 has native JSON support.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Builder.Default
    private Map<String, Object> variations = new HashMap<>();

    @Column(name = "default_variation")
    private String defaultVariation;

    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("priority ASC")
    @Builder.Default
    private List<Rule> rules = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flag_tags", joinColumns = @JoinColumn(name = "flag_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "track_events")
    private boolean trackEvents;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum FlagType {
        BOOLEAN, STRING, NUMBER, JSON
    }

    public enum FlagStatus {
        ENABLED, DISABLED
    }

    /**
     * DDD: Domain Logic
     */
    public boolean isEnabled() {
        return this.status == FlagStatus.ENABLED;
    }

    public Object defaultValue() {
        if (this.defaultVariation != null && this.variations != null) {
            return variations.get(defaultVariation);
        }
        return null;
    }

    public void addRule(Rule rule) {
        rules.add(rule);
        rule.setFlag(this);
    }

    public void removeRule(Rule rule) {
        rules.remove(rule);
        rule.setFlag(null);
    }
}
