package org.pzsp2.eye_tracking.share;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyShareLinkRepository extends JpaRepository<StudyShareLink, String> {
  List<StudyShareLink> findAllByStudy(org.pzsp2.eye_tracking.storage.Study study);
}
