package org.pzsp2.eye_tracking.session;

import jakarta.persistence.*;
import lombok.Data;
import org.pzsp2.eye_tracking.storage.Study;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "study_sessions")
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