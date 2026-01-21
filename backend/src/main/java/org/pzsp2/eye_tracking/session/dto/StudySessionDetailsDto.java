package org.pzsp2.eye_tracking.session.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data public class StudySessionDetailsDto {

    @JsonProperty("session_id") private UUID sessionId;

    @JsonProperty("study_id") private UUID studyId;

    private String name;

    @JsonProperty("completed_at") private LocalDateTime completedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("heatmaps") private List<List<HeatmapPointDto>> heatmaps;

    public List<List<HeatmapPointDto>> getHeatmaps() {
        return heatmaps == null ? null : new ArrayList<>(heatmaps);
    }

    public void setHeatmaps(List<List<HeatmapPointDto>> heatmaps) {
        this.heatmaps = heatmaps == null ? null : new ArrayList<>(heatmaps);
    }
}