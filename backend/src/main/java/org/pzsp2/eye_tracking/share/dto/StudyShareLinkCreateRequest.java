package org.pzsp2.eye_tracking.share.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class StudyShareLinkCreateRequest {

    @JsonProperty("max_uses")
    private Integer maxUses;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}