package org.pzsp2.eye_tracking.storage;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "STUDIES") public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "study_id") private UUID studyId;

    @Column(name = "researcher_id") private UUID researcherId;

    private String title;
    private String description;

    @Column(columnDefinition = "TEXT") private String settings;

    @Column(name = "created_at") private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_active") private Boolean isActive = true;

    @Lob
    @Column(name = "aggregate_heatmaps_json", columnDefinition = "TEXT") private String aggregateHeatmapsJson;

    public UUID getStudyId() {
        return studyId;
    }

    public void setStudyId(UUID studyId) {
        this.studyId = studyId;
    }

    public UUID getResearcherId() {
        return researcherId;
    }

    public void setResearcherId(UUID researcherId) {
        this.researcherId = researcherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getAggregateHeatmapsJson() {
        return aggregateHeatmapsJson;
    }

    public void setAggregateHeatmapsJson(String aggregateHeatmapsJson) {
        this.aggregateHeatmapsJson = aggregateHeatmapsJson;
    }
}
