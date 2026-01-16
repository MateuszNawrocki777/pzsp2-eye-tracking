package org.pzsp2.eye_tracking.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pzsp2.eye_tracking.session.dto.HeatmapPointDto;
import org.pzsp2.eye_tracking.session.dto.StudySessionCreateRequest;
import org.pzsp2.eye_tracking.session.dto.StudySessionDetailsDto;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudySessionService {

    private static final int GRID_WIDTH = 384;
    private static final int GRID_HEIGHT = 216;

    private final StudySessionRepository sessionRepository;
    private final StudyRepository studyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudySessionService(StudySessionRepository sessionRepository,
            StudyRepository studyRepository) {
        this.sessionRepository = sessionRepository;
        this.studyRepository = studyRepository;
    }

    @Transactional
    @SuppressWarnings("null")
    public UUID createSession(StudySessionCreateRequest request) {
        Study study = studyRepository.findById(Objects.requireNonNull(request.getStudyId()))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Study does not exist"));

        List<List<HeatmapPointDto>> heatmaps = buildHeatmaps(request.getPointsPerImage());

        StudySession session = new StudySession();
        session.setStudy(study);
        session.setName(request.getName());
        session.setCompletedAt(LocalDateTime.now());

        try {
            session.setHeatmapsJson(objectMapper.writeValueAsString(heatmaps));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize heatmaps", e);
        }

        return sessionRepository.save(session).getSessionId();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public StudySessionDetailsDto getSession(UUID sessionId) {
        StudySession session = sessionRepository.findById(Objects.requireNonNull(sessionId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Session not found"));

        StudySessionDetailsDto dto = new StudySessionDetailsDto();
        dto.setSessionId(session.getSessionId());
        dto.setStudyId(session.getStudy().getStudyId());
        dto.setName(session.getName());
        dto.setCompletedAt(session.getCompletedAt());
        dto.setCreatedAt(session.getCreatedAt());

        if (session.getHeatmapsJson() != null) {
            try {
                List<List<HeatmapPointDto>> heatmaps = objectMapper.readValue(
                        session.getHeatmapsJson(),
                        new TypeReference<List<List<HeatmapPointDto>>>() {
                        });
                dto.setHeatmaps(heatmaps);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse heatmaps", e);
            }
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<StudySessionDetailsDto> getSessionsForStudy(UUID studyId) {
        Study study = studyRepository.findById(Objects.requireNonNull(studyId))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Study does not exist"));

        List<StudySession> sessions = sessionRepository.findAllByStudy(study);
        List<StudySessionDetailsDto> result = new ArrayList<>();
        for (StudySession session : sessions) {
            StudySessionDetailsDto dto = new StudySessionDetailsDto();
            dto.setSessionId(session.getSessionId());
            dto.setStudyId(studyId);
            dto.setName(session.getName());
            dto.setCompletedAt(session.getCompletedAt());
            dto.setCreatedAt(session.getCreatedAt());
            result.add(dto);
        }
        return result;
    }

    private List<List<HeatmapPointDto>> buildHeatmaps(List<List<List<Double>>> pointsPerImage) {
        List<List<HeatmapPointDto>> heatmaps = new ArrayList<>();
        if (pointsPerImage == null) {
            return heatmaps;
        }

        for (List<List<Double>> points : pointsPerImage) {
            heatmaps.add(buildHeatmap(points));
        }

        return heatmaps;
    }

    private List<HeatmapPointDto> buildHeatmap(List<List<Double>> points) {
        int[][] counts = new int[GRID_HEIGHT][GRID_WIDTH];
        int total = 0;

        if (points != null) {
            for (List<Double> point : points) {
                if (point == null || point.size() < 2) {
                    continue;
                }
                Double xVal = point.get(0);
                Double yVal = point.get(1);
                if (xVal == null || yVal == null) {
                    continue;
                }
                int x = toGrid(xVal, GRID_WIDTH);
                int y = toGrid(yVal, GRID_HEIGHT);
                counts[y][x]++;
                total++;
            }
        }

        List<HeatmapPointDto> heatmap = new ArrayList<>();
        if (total == 0) {
            return heatmap;
        }

        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                int count = counts[y][x];
                if (count > 0) {
                    double val = (double) count / (double) total;
                    heatmap.add(new HeatmapPointDto(x, y, val));
                }
            }
        }

        return heatmap;
    }

    private int toGrid(double v, int size) {
        if (v < 0.0) {
            v = 0.0;
        } else if (v > 1.0) {
            v = 1.0;
        }
        int idx = (int) Math.floor(v * size);
        if (idx >= size) {
            idx = size - 1;
        }
        return idx;
    }
}