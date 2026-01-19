package org.pzsp2.eye_tracking.share.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StudyShareLinkResponse {

    @JsonProperty("access_link")
    private String accessLink;

    @JsonProperty("max_uses")
    private Integer maxUses;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("use_counter")
    private Integer useCounter;

    @JsonProperty("access_url")
    private String accessUrl;

}