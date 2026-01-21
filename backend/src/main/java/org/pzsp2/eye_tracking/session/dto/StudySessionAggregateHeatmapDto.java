package org.pzsp2.eye_tracking.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
                "EI_EXPOSE_REP2"}, justification = "DTO with nested lists - suppressing for simplicity") public class StudySessionAggregateHeatmapDto {

    @JsonProperty("study_id") private UUID studyId;
    @JsonProperty("heatmaps") private List<List<HeatmapPointDto>> heatmaps;
}