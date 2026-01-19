package org.pzsp2.eye_tracking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, UUID> {
    Optional<StudyMaterial> findFirstByStudyOrderByDisplayOrderAsc(Study study);
    List<StudyMaterial> findAllByStudyOrderByDisplayOrderAsc(Study study);
}