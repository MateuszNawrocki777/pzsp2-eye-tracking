package org.pzsp2.eye_tracking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StudyRepository extends JpaRepository<Study, UUID> {
}
