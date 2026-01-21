package org.pzsp2.eye_tracking.session;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.session.dto.*;
import org.pzsp2.eye_tracking.storage.Study;

class StudySessionModelTest {
    @Test void testStudySessionEntity() {
        StudySession session1 = new StudySession();
        UUID id = UUID.randomUUID();
        Study study = new Study();
        session1.setSessionId(id);
        session1.setStudy(study);
        session1.setName("Name");
        session1.setHeatmapsJson("[]");
        session1.setCompletedAt(LocalDateTime.now());

        assertEquals(id, session1.getSessionId());
        assertEquals(study, session1.getStudy());
        assertEquals("Name", session1.getName());
        assertNotNull(session1.getCompletedAt());

        StudySession session2 = new StudySession();
        session2.setSessionId(id);
        session2.setStudy(study);
        session2.setName("Name");
        session2.setHeatmapsJson("[]");
        session2.setCompletedAt(session1.getCompletedAt());

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
        assertNotNull(session1.toString());
    }

    @Test void testHeatmapPointDto() {
        HeatmapPointDto p1 = new HeatmapPointDto(10, 20, 0.5);
        p1.setX(15);
        p1.setY(25);
        p1.setVal(0.8);

        assertEquals(15, p1.getX());
        assertEquals(25, p1.getY());
        assertEquals(0.8, p1.getVal());

        HeatmapPointDto p2 = new HeatmapPointDto(15, 25, 0.8);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotNull(p1.toString());
    }

    @Test void testCreateRequestDto() {
        StudySessionCreateRequest req1 = new StudySessionCreateRequest();
        UUID studyId = UUID.randomUUID();
        req1.setStudyId(studyId);
        req1.setName("Test");
        req1.setPointsPerImage(List.of());

        assertEquals(studyId, req1.getStudyId());
        assertEquals("Test", req1.getName());
        assertNotNull(req1.getPointsPerImage());

        StudySessionCreateRequest req2 = new StudySessionCreateRequest();
        req2.setStudyId(studyId);
        req2.setName("Test");
        req2.setPointsPerImage(List.of());

        assertEquals(req1, req2);
        assertNotNull(req1.toString());
    }

    @Test void testDetailsDto() {
        StudySessionDetailsDto dto = new StudySessionDetailsDto();
        UUID sessionId = UUID.randomUUID();
        dto.setSessionId(sessionId);
        dto.setHeatmaps(List.of());

        assertEquals(sessionId, dto.getSessionId());
        assertNotNull(dto.getHeatmaps());

        StudySessionDetailsDto dto2 = new StudySessionDetailsDto();
        dto2.setSessionId(sessionId);
        dto2.setHeatmaps(List.of());

        assertEquals(dto, dto2);
        assertNotNull(dto.toString());
    }
}
