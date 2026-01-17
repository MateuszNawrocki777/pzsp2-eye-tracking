package org.pzsp2.eye_tracking.share;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

}