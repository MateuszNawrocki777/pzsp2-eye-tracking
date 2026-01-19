package org.pzsp2.eye_tracking.storage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, UUID> {
  Optional<StudyMaterial> findFirstByStudyOrderByDisplayOrderAsc(Study study);

  List<StudyMaterial> findAllByStudyOrderByDisplayOrderAsc(Study study);
}
