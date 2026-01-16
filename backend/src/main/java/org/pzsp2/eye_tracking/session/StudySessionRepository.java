package org.pzsp2.eye_tracking.session;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {
    List<StudySession> findAllByStudy(org.pzsp2.eye_tracking.storage.Study study);
}