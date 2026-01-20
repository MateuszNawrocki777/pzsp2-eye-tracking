package org.pzsp2.eye_tracking.session.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudySessionDtoTest {

  @Test
  void testGazePointDto() {
    GazePointDto g1 = new GazePointDto(1.5, 2.5);

    assertEquals(1.5, g1.getX());
    assertEquals(2.5, g1.getY());

    GazePointDto g2 = new GazePointDto();
    g2.setX(1.5);
    g2.setY(2.5);

    assertEquals(g1, g2);
    assertEquals(g1.hashCode(), g2.hashCode());
    assertNotNull(g1.toString());
  }

  @Test
  void testHeatmapPointDto() {
    HeatmapPointDto p1 = new HeatmapPointDto(10, 20, 0.5);

    assertEquals(10, p1.getX());
    assertEquals(20, p1.getY());
    assertEquals(0.5, p1.getVal());

    HeatmapPointDto p2 = new HeatmapPointDto();
    p2.setX(10);
    p2.setY(20);
    p2.setVal(0.5);

    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());

    assertNotNull(p1.toString());
  }

  @Test
  void testStudySessionAggregateHeatmapDto() {
    UUID studyId = UUID.randomUUID();
    List<List<HeatmapPointDto>> heatmaps = new ArrayList<>();

    StudySessionAggregateHeatmapDto agg1 = new StudySessionAggregateHeatmapDto();
    agg1.setStudyId(studyId);
    agg1.setHeatmaps(heatmaps);

    assertEquals(studyId, agg1.getStudyId());
    assertEquals(heatmaps, agg1.getHeatmaps());

    agg1.setHeatmaps(null);
    assertNull(agg1.getHeatmaps());

    StudySessionAggregateHeatmapDto agg2 = new StudySessionAggregateHeatmapDto();
    agg2.setStudyId(studyId);
    agg2.setHeatmaps(null);

    assertEquals(agg1, agg2);
    assertEquals(agg1.hashCode(), agg2.hashCode());
    assertNotNull(agg1.toString());
  }

  @Test
  void testStudySessionCreateRequest() {
    UUID studyId = UUID.randomUUID();
    List<List<List<Double>>> points = new ArrayList<>();

    StudySessionCreateRequest req1 = new StudySessionCreateRequest();
    req1.setStudyId(studyId);
    req1.setName("Session 1");
    req1.setPointsPerImage(points);

    assertEquals(studyId, req1.getStudyId());
    assertEquals("Session 1", req1.getName());
    assertEquals(points, req1.getPointsPerImage());

    assertNotSame(points, req1.getPointsPerImage());

    req1.setPointsPerImage(null);
    assertNull(req1.getPointsPerImage());

    StudySessionCreateRequest req2 = new StudySessionCreateRequest();
    req2.setStudyId(studyId);
    req2.setName("Session 1");
    req2.setPointsPerImage(null);

    assertEquals(req1, req2);
    assertEquals(req1.hashCode(), req2.hashCode());
    assertNotNull(req1.toString());
  }

  @Test
  void testStudySessionCreateResponse() {
    UUID sessionId = UUID.randomUUID();
    StudySessionCreateResponse res1 = new StudySessionCreateResponse(sessionId);

    assertEquals(sessionId, res1.getSessionId());

    StudySessionCreateResponse res2 = new StudySessionCreateResponse();
    res2.setSessionId(sessionId);

    assertEquals(res1, res2);
    assertEquals(res1.hashCode(), res2.hashCode());
    assertNotNull(res1.toString());
  }

  @Test
  void testStudySessionDetailsDto() {
    UUID sessionId = UUID.randomUUID();
    UUID studyId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    List<List<HeatmapPointDto>> heatmaps = new ArrayList<>();

    StudySessionDetailsDto d1 = new StudySessionDetailsDto();
    d1.setSessionId(sessionId);
    d1.setStudyId(studyId);
    d1.setName("Details");
    d1.setCompletedAt(now);
    d1.setHeatmaps(heatmaps);

    assertEquals(sessionId, d1.getSessionId());
    assertEquals(studyId, d1.getStudyId());
    assertEquals("Details", d1.getName());
    assertEquals(now, d1.getCompletedAt());
    assertEquals(heatmaps, d1.getHeatmaps());

    assertNotSame(heatmaps, d1.getHeatmaps());

    d1.setHeatmaps(null);
    assertNull(d1.getHeatmaps());

    StudySessionDetailsDto d2 = new StudySessionDetailsDto();
    d2.setSessionId(sessionId);
    d2.setStudyId(studyId);
    d2.setName("Details");
    d2.setCompletedAt(now);
    d2.setHeatmaps(null);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
    assertNotNull(d1.toString());
  }
}