package org.pzsp2.eye_tracking.storage;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, UUID> {
}
