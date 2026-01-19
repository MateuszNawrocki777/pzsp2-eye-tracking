package org.pzsp2.eye_tracking.session;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {
  List<StudySession> findAllByStudy(org.pzsp2.eye_tracking.storage.Study study);
}
