package org.pzsp2.eye_tracking.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class StudySessionDetailsDto {

    @JsonProperty("session_id")
    private UUID sessionId;

    @JsonProperty("study_id")
    private UUID studyId;

    private String name;

    @JsonProperty("completed_at")
    private LocalDateTime completedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("heatmaps")
    private List<List<HeatmapPointDto>> heatmaps;

}