package org.pzsp2.eye_tracking.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class StudySessionAggregateHeatmapDto {

  @JsonProperty("study_id")
  private UUID studyId;

  @JsonProperty("heatmaps")
  private List<List<HeatmapPointDto>> heatmaps;
}
