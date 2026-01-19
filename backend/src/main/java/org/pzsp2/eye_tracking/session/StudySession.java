package org.pzsp2.eye_tracking.session;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import org.pzsp2.eye_tracking.storage.Study;

@Data
@Entity
@Table(name = "study_sessions")
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = "JPA entities are mutable by design")
public class StudySession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "session_id")
  private UUID sessionId;

  @ManyToOne
  @JoinColumn(name = "study_id", nullable = false)
  private Study study;

  @Column(name = "session_name")
  private String name;

  @Column(name = "completed_at")
  private LocalDateTime completedAt = LocalDateTime.now();

  @Lob
  @Column(name = "heatmaps_json", columnDefinition = "TEXT")
  private String heatmapsJson;
}