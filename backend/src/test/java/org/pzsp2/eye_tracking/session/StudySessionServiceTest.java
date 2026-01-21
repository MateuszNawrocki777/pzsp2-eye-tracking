package org.pzsp2.eye_tracking.session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.session.dto.HeatmapPointDto;
import org.pzsp2.eye_tracking.session.dto.StudySessionCreateRequest;
import org.pzsp2.eye_tracking.session.dto.StudySessionDetailsDto;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyMaterial;
import org.pzsp2.eye_tracking.storage.StudyMaterialRepository;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class) class StudySessionServiceTest {

    @Mock private StudySessionRepository sessionRepository;
    @Mock private StudyRepository studyRepository;
    @Mock private StudyMaterialRepository materialRepository;

    @InjectMocks private StudySessionService sessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test void createSession_countMismatch_throwsBadRequest() {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial(), new StudyMaterial()));

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        request.setPointsPerImage(List.of(List.of()));

        assertThrows(ResponseStatusException.class, () -> sessionService.createSession(request));
    }

    @Test void createSession_calculatesCoordinates_and_updatesAggregate()
                    throws JsonProcessingException {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);
        List<List<HeatmapPointDto>> oldAgg = List.of(List.of(new HeatmapPointDto(0, 0, 1.0)));
        study.setAggregateHeatmapsJson(objectMapper.writeValueAsString(oldAgg));

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> {
            StudySession s = i.getArgument(0);
            s.setSessionId(UUID.randomUUID());
            return s;
        });

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        request.setName("New Session");

        List<Double> p1 = List.of(0.0, 0.0);
        request.setPointsPerImage(List.of(List.of(p1)));

        sessionService.createSession(request);

        ArgumentCaptor<StudySession> sessionCaptor = ArgumentCaptor.forClass(StudySession.class);
        verify(sessionRepository).save(sessionCaptor.capture());
        String sessionJson = sessionCaptor.getValue().getHeatmapsJson();
        assertTrue(sessionJson.contains("\"x\":0,\"y\":0"), "Session should contain (0,0) point");

        ArgumentCaptor<Study> studyCaptor = ArgumentCaptor.forClass(Study.class);
        verify(studyRepository).save(studyCaptor.capture());
        String aggJson = studyCaptor.getValue().getAggregateHeatmapsJson();

        assertTrue(aggJson.contains("\"val\":2.0"),
                        "Aggregation should sum values (1.0 + 1.0 = 2.0)");
    }

    @Test void getSession_parsesJsonCorrectly() throws JsonProcessingException {
        UUID sessionId = UUID.randomUUID();
        StudySession session = new StudySession();
        session.setSessionId(sessionId);
        session.setStudy(new Study());
        session.setName("Parsed Session");

        List<List<HeatmapPointDto>> storedData = List.of(List.of(new HeatmapPointDto(10, 20, 0.5)));
        session.setHeatmapsJson(objectMapper.writeValueAsString(storedData));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        StudySessionDetailsDto result = sessionService.getSession(sessionId);

        assertNotNull(result.getHeatmaps());
        assertEquals(1, result.getHeatmaps().size());
        assertEquals(10, result.getHeatmaps().get(0).get(0).getX());
        assertEquals(0.5, result.getHeatmaps().get(0).get(0).getVal());
    }

    @Test void getAggregateHeatmaps_normalizesValues() throws JsonProcessingException {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();

        List<List<HeatmapPointDto>> rawSums = List.of(List.of(new HeatmapPointDto(50, 50, 10.0)));
        study.setAggregateHeatmapsJson(objectMapper.writeValueAsString(rawSums));

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));

        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        List<List<HeatmapPointDto>> result = sessionService.getAggregateHeatmapsForStudy(studyId);

        assertFalse(result.isEmpty());
        HeatmapPointDto point = result.get(0).get(0);

        assertEquals(1.0, point.getVal(), 0.0001);
        assertEquals(50, point.getX());
    }

    @Test void getAggregateHeatmaps_handlesEmptyStudy() {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setAggregateHeatmapsJson(null);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial(), new StudyMaterial()));

        List<List<HeatmapPointDto>> result = sessionService.getAggregateHeatmapsForStudy(studyId);

        assertEquals(2, result.size());
        assertTrue(result.get(0).isEmpty());
        assertTrue(result.get(1).isEmpty());
    }

    @Test void createSession_studyNotFound_throwsNotFound() {
        UUID studyId = UUID.randomUUID();
        when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);

        assertThrows(ResponseStatusException.class, () -> sessionService.createSession(request));
    }

    @Test void getSession_notFound_throwsNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> sessionService.getSession(sessionId));
    }

    @Test void getSessionsForStudy_studyNotFound_throwsNotFound() {
        UUID studyId = UUID.randomUUID();
        when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                        () -> sessionService.getSessionsForStudy(studyId));
    }

    @Test void getAggregateHeatmaps_studyNotFound_throwsNotFound() {
        UUID studyId = UUID.randomUUID();
        when(studyRepository.findById(studyId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                        () -> sessionService.getAggregateHeatmapsForStudy(studyId));
    }

    @Test void getSessionsForStudy_returnsMappedDtos() {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        StudySession s1 = new StudySession();
        s1.setSessionId(UUID.randomUUID());
        s1.setName("S1");
        s1.setStudy(study);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(sessionRepository.findAllByStudy(study)).thenReturn(List.of(s1));

        List<StudySessionDetailsDto> result = sessionService.getSessionsForStudy(studyId);

        assertEquals(1, result.size());
        assertEquals("S1", result.get(0).getName());
        assertEquals(s1.getSessionId(), result.get(0).getSessionId());
    }

    @Test void getAggregateHeatmaps_fillsMissingImages() throws JsonProcessingException {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();

        List<List<HeatmapPointDto>> oldData = List.of(List.of(new HeatmapPointDto(0, 0, 1.0)));
        study.setAggregateHeatmapsJson(objectMapper.writeValueAsString(oldData));

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial(), new StudyMaterial()));

        List<List<HeatmapPointDto>> result = sessionService.getAggregateHeatmapsForStudy(studyId);

        assertEquals(2, result.size());
        assertFalse(result.get(0).isEmpty());
        assertTrue(result.get(1).isEmpty());
    }

    @Test void createSession_handlesNullPointsGracefully() {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> i.getArgument(0));

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        List<List<Double>> pointsWithNull = new java.util.ArrayList<>();
        pointsWithNull.add(null);
        request.setPointsPerImage(List.of(pointsWithNull));

        assertDoesNotThrow(() -> sessionService.createSession(request));
    }

    @Test void createSession_ignoresMalformedPointsInList() {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> i.getArgument(0));

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);

        List<List<Double>> garbagePoints = new java.util.ArrayList<>();

        garbagePoints.add(null);

        garbagePoints.add(List.of(0.5));

        List<Double> nullValPoint = new java.util.ArrayList<>();
        nullValPoint.add(0.5);
        nullValPoint.add(null);
        garbagePoints.add(nullValPoint);

        garbagePoints.add(List.of(0.0, 0.0));

        request.setPointsPerImage(List.of(garbagePoints));

        sessionService.createSession(request);

        ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
        verify(sessionRepository).save(captor.capture());
        String json = captor.getValue().getHeatmapsJson();

        assertTrue(json.contains("\"x\":0,\"y\":0"));
    }

    @Test void createSession_nullPointsList_handlesGracefully() {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study)).thenReturn(List.of());

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> i.getArgument(0));

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        request.setPointsPerImage(null);

        assertDoesNotThrow(() -> sessionService.createSession(request));
    }

    @Test void updateAggregateHeatmaps_filtersOutInvalidExistingData()
                    throws JsonProcessingException {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        String corruptedJson = "[[{\"x\":-1,\"y\":0,\"val\":1.0}, {\"x\":0,\"y\":9999,\"val\":1.0},"
                        + " {\"x\":0,\"y\":0,\"val\":-5.0}, {\"x\":10,\"y\":10,\"val\":0.0}]]";
        study.setAggregateHeatmapsJson(corruptedJson);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> {
            StudySession s = i.getArgument(0);
            s.setSessionId(UUID.randomUUID());
            return s;
        });

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        request.setPointsPerImage(List.of(List.of()));

        sessionService.createSession(request);

        ArgumentCaptor<Study> captor = ArgumentCaptor.forClass(Study.class);
        verify(studyRepository).save(captor.capture());
        String newJson = captor.getValue().getAggregateHeatmapsJson();

        assertFalse(newJson.contains("\"x\":-1"));
        assertFalse(newJson.contains("\"val\":-5.0"));
    }

    @Test void normalizeHeatmaps_handlesZeroTotal_andNullsInList() throws JsonProcessingException {
        UUID studyId = UUID.randomUUID();
        Study study = new Study();

        List<HeatmapPointDto> zeroList = new java.util.ArrayList<>();
        zeroList.add(new HeatmapPointDto(10, 10, 0.0));
        zeroList.add(null);

        List<List<HeatmapPointDto>> rawData = List.of(zeroList);
        study.setAggregateHeatmapsJson(objectMapper.writeValueAsString(rawData));

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        List<List<HeatmapPointDto>> result = sessionService.getAggregateHeatmapsForStudy(studyId);

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).isEmpty());
    }

    @Test void getSession_handlesNullHeatmapsJson() {
        UUID sessionId = UUID.randomUUID();
        StudySession session = new StudySession();
        session.setSessionId(sessionId);
        session.setStudy(new Study());
        session.setName("Session without heatmaps");
        session.setHeatmapsJson(null);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        StudySessionDetailsDto result = sessionService.getSession(sessionId);

        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
        assertNull(result.getHeatmaps(), "Heatmaps should be null if JSON was null");
    }

    @Test void createSession_firstSession_createsNewAggregate() {

        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);
        study.setAggregateHeatmapsJson(null);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> {
            StudySession s = i.getArgument(0);
            s.setSessionId(UUID.randomUUID());
            return s;
        });

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        request.setPointsPerImage(List.of(List.of(List.of(0.5, 0.5))));

        sessionService.createSession(request);

        ArgumentCaptor<Study> captor = ArgumentCaptor.forClass(Study.class);
        verify(studyRepository).save(captor.capture());

        String newAggJson = captor.getValue().getAggregateHeatmapsJson();
        assertNotNull(newAggJson);
        assertTrue(newAggJson.contains("\"val\":1.0"),
                        "New aggregation should be created with value 1.0");
    }

    @Test void createSession_allPointsInvalid_resultsInEmptyHeatmapList() {

        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> i.getArgument(0));

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);

        List<List<Double>> pointsForImage = new java.util.ArrayList<>();

        pointsForImage.add(null);

        pointsForImage.add(List.of(0.5));

        List<Double> pointWithNull = new java.util.ArrayList<>();
        pointWithNull.add(0.5);
        pointWithNull.add(null);
        pointsForImage.add(pointWithNull);

        request.setPointsPerImage(List.of(pointsForImage));

        sessionService.createSession(request);

        ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
        verify(sessionRepository).save(captor.capture());

        String json = captor.getValue().getHeatmapsJson();
        assertEquals("[[]]", json);
    }

    @Test void toGrid_clampsNegativeValues() {

        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(studyId);

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> i.getArgument(0));

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);

        List<Double> negativePoint = List.of(-0.5, 0.5);
        request.setPointsPerImage(List.of(List.of(negativePoint)));

        sessionService.createSession(request);

        ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
        verify(sessionRepository).save(captor.capture());
        String json = captor.getValue().getHeatmapsJson();

        assertTrue(json.contains("\"x\":0"), "Negative values should be truncated to 0");
    }

    @Test void updateAggregateHeatmaps_handlesNullListInsideJson() throws JsonProcessingException {

        UUID studyId = UUID.randomUUID();
        Study study = new Study();
        study.setAggregateHeatmapsJson("[null, []]");

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial(), new StudyMaterial()));

        when(sessionRepository.save(any(StudySession.class))).thenAnswer(i -> {
            StudySession s = i.getArgument(0);
            s.setSessionId(UUID.randomUUID());
            return s;
        });

        StudySessionCreateRequest request = new StudySessionCreateRequest();
        request.setStudyId(studyId);
        request.setPointsPerImage(List.of(List.of(), List.of()));

        assertDoesNotThrow(() -> sessionService.createSession(request));
    }

    @Test void normalizeHeatmaps_skipsZeroValues_evenIfTotalIsPositive()
                    throws JsonProcessingException {

        UUID studyId = UUID.randomUUID();
        Study study = new Study();

        List<HeatmapPointDto> points = List.of(new HeatmapPointDto(10, 10, 10.0),
                        new HeatmapPointDto(20, 20, 0.0));
        List<List<HeatmapPointDto>> rawData = List.of(points);
        study.setAggregateHeatmapsJson(objectMapper.writeValueAsString(rawData));

        when(studyRepository.findById(studyId)).thenReturn(Optional.of(study));
        when(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .thenReturn(List.of(new StudyMaterial()));

        List<List<HeatmapPointDto>> result = sessionService.getAggregateHeatmapsForStudy(studyId);

        assertFalse(result.isEmpty());
        List<HeatmapPointDto> heatmap = result.get(0);

        assertEquals(1, heatmap.size());
        assertEquals(10, heatmap.get(0).getX());
    }
}
