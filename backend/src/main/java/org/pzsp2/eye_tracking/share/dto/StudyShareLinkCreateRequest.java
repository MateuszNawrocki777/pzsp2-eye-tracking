package org.pzsp2.eye_tracking.share.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StudyShareLinkCreateRequest {

  @JsonProperty("max_uses")
  private Integer maxUses;

  @JsonProperty("expires_at")
  private LocalDateTime expiresAt;
}
