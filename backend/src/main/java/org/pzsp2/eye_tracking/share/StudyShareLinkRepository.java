package org.pzsp2.eye_tracking.share;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyShareLinkRepository extends JpaRepository<StudyShareLink, String> {
    List<StudyShareLink> findAllByStudy(org.pzsp2.eye_tracking.storage.Study study);
}