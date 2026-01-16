package org.pzsp2.eye_tracking.share;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_share_links")
public class StudyShareLink {

    @Id
    @Column(name = "access_link", nullable = false, updatable = false, unique = true)
    private String accessLink;

    @ManyToOne
    @JoinColumn(name = "study_id", nullable = false)
    private org.pzsp2.eye_tracking.storage.Study study;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "use_counter", nullable = false)
    private Integer useCounter = 0;

    public String getAccessLink() {
        return accessLink;
    }

    public void setAccessLink(String accessLink) {
        this.accessLink = accessLink;
    }

    public org.pzsp2.eye_tracking.storage.Study getStudy() {
        return study;
    }

    public void setStudy(org.pzsp2.eye_tracking.storage.Study study) {
        this.study = study;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUseCounter() {
        return useCounter;
    }

    public void setUseCounter(Integer useCounter) {
        this.useCounter = useCounter;
    }
}