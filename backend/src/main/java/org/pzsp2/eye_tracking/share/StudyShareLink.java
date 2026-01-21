package org.pzsp2.eye_tracking.share;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pzsp2.eye_tracking.storage.Study;

@Data
@NoArgsConstructor
@Entity
@Table(name = "study_share_links")
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
                "EI_EXPOSE_REP2"}, justification = "JPA entities are mutable by design") public class StudyShareLink {

    @Id
    @Column(name = "access_link", nullable = false, updatable = false, unique = true) private String accessLink;

    @ManyToOne
    @JoinColumn(name = "study_id", nullable = false) private Study study;

    @Column(name = "max_uses") private Integer maxUses;

    @Column(name = "expires_at") private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime
                    .now();

    @Column(name = "use_counter", nullable = false) private Integer useCounter = 0;
}