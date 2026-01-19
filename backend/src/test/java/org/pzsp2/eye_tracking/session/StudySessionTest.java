package org.pzsp2.eye_tracking.session;

import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.storage.Study;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StudySessionTest {

    @Test
    void testStudySessionEntity() {
        StudySession session1 = new StudySession();
        assertNotNull(session1.getCompletedAt(), "message");

        UUID id = UUID.randomUUID();
        Study study = new Study();
        String name = "Test Session";
        String json = "[]";
        LocalDateTime now = LocalDateTime.now();

        session1.setSessionId(id);
        session1.setStudy(study);
        session1.setName(name);
        session1.setHeatmapsJson(json);
        session1.setCompletedAt(now);

        assertEquals(id, session1.getSessionId());
        assertEquals(study, session1.getStudy());
        assertEquals(name, session1.getName());
        assertEquals(json, session1.getHeatmapsJson());
        assertEquals(now, session1.getCompletedAt());

        StudySession session2 = new StudySession();
        session2.setSessionId(id);
        session2.setStudy(study);
        session2.setName(name);
        session2.setHeatmapsJson(json);
        session2.setCompletedAt(now);

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());

        session2.setName("Different name");
        assertNotEquals(session1, session2);

        String stringResult = session1.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("sessionId"));
    }
}