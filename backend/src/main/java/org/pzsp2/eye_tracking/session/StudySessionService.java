package org.pzsp2.eye_tracking.session;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.pzsp2.eye_tracking.session.dto.HeatmapPointDto;
import org.pzsp2.eye_tracking.session.dto.StudySessionCreateRequest;
import org.pzsp2.eye_tracking.session.dto.StudySessionDetailsDto;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyMaterialRepository;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service public class StudySessionService {

    private static final int GRID_WIDTH = 384;
    private static final int GRID_HEIGHT = 216;

    private final StudySessionRepository sessionRepository;
    private final StudyRepository studyRepository;
    private final StudyMaterialRepository materialRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudySessionService(StudySessionRepository sessionRepository,
                    StudyRepository studyRepository, StudyMaterialRepository materialRepository) {
        this.sessionRepository = sessionRepository;
        this.studyRepository = studyRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    @SuppressWarnings("null") public UUID createSession(StudySessionCreateRequest request) {
        Study study = studyRepository.findById(Objects.requireNonNull(request.getStudyId()))
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                                        "Study does not exist"));

        int imageCount = materialRepository.findAllByStudyOrderByDisplayOrderAsc(study).size();
        int providedCount = request.getPointsPerImage() == null
                        ? 0
                        : request.getPointsPerImage().size();
        if (providedCount != imageCount) {
            throw new ResponseStatusException(BAD_REQUEST,
                            "points_per_image size must match number of study images");
        }

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

        StudySession saved = sessionRepository.save(session);
        updateAggregateHeatmaps(study, heatmaps, imageCount);
        studyRepository.save(study);

        return saved.getSessionId();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null") public StudySessionDetailsDto getSession(UUID sessionId) {
        StudySession session = sessionRepository.findById(Objects.requireNonNull(sessionId))
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                                        "Session not found"));

        StudySessionDetailsDto dto = new StudySessionDetailsDto();
        dto.setSessionId(session.getSessionId());
        dto.setStudyId(session.getStudy().getStudyId());
        dto.setName(session.getName());
        dto.setCompletedAt(session.getCompletedAt());

        if (session.getHeatmapsJson() != null) {
            try {
                dto.setHeatmaps(parseHeatmaps(session.getHeatmapsJson()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse heatmaps", e);
            }
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null") public List<StudySessionDetailsDto> getSessionsForStudy(
                    UUID studyId) {
        Study study = studyRepository.findById(Objects.requireNonNull(studyId)).orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND, "Study does not exist"));

        List<StudySession> sessions = sessionRepository.findAllByStudy(study);
        List<StudySessionDetailsDto> result = new ArrayList<>();
        for (StudySession session : sessions) {
            StudySessionDetailsDto dto = new StudySessionDetailsDto();
            dto.setSessionId(session.getSessionId());
            dto.setStudyId(studyId);
            dto.setName(session.getName());
            dto.setCompletedAt(session.getCompletedAt());
            result.add(dto);
        }
        return result;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null") public List<List<HeatmapPointDto>> getAggregateHeatmapsForStudy(
                    UUID studyId) {
        Study study = studyRepository.findById(Objects.requireNonNull(studyId)).orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND, "Study does not exist"));

        int imageCount = materialRepository.findAllByStudyOrderByDisplayOrderAsc(study).size();
        if (study.getAggregateHeatmapsJson() == null) {
            return emptyHeatmaps(imageCount);
        }

        try {
            List<List<HeatmapPointDto>> sums = parseHeatmaps(study.getAggregateHeatmapsJson());
            if (sums.size() < imageCount) {
                sums.addAll(emptyHeatmaps(imageCount - sums.size()));
            }
            return normalizeHeatmaps(sums, imageCount);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse aggregate heatmaps", e);
        }
    }

    private List<List<HeatmapPointDto>> parseHeatmaps(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<List<List<HeatmapPointDto>>>() {
        });
    }

    private void updateAggregateHeatmaps(Study study, List<List<HeatmapPointDto>> sessionHeatmaps,
                    int imageCount) {
        double[][][] aggregateGrid = new double[imageCount][GRID_HEIGHT][GRID_WIDTH];

        if (study.getAggregateHeatmapsJson() != null) {
            try {
                List<List<HeatmapPointDto>> existing = parseHeatmaps(
                                study.getAggregateHeatmapsJson());
                applyHeatmapToGrid(aggregateGrid, existing, imageCount, 1);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse aggregate heatmaps", e);
            }
        }

        applyHeatmapToGrid(aggregateGrid, sessionHeatmaps, imageCount, 1);

        List<List<HeatmapPointDto>> aggregated = buildHeatmapsFromGridRaw(aggregateGrid,
                        imageCount);
        try {
            study.setAggregateHeatmapsJson(objectMapper.writeValueAsString(aggregated));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize aggregate heatmaps", e);
        }
    }

    private void applyHeatmapToGrid(double[][][] grid, List<List<HeatmapPointDto>> heatmaps,
                    int imageCount, int weight) {
        if (heatmaps == null) {
            return;
        }
        int limit = Math.min(imageCount, heatmaps.size());
        for (int i = 0; i < limit; i++) {
            List<HeatmapPointDto> heatmap = heatmaps.get(i);
            if (heatmap == null) {
                continue;
            }
            for (HeatmapPointDto point : heatmap) {
                if (point == null) {
                    continue;
                }
                int x = point.getX();
                int y = point.getY();
                if (x < 0 || x >= GRID_WIDTH || y < 0 || y >= GRID_HEIGHT) {
                    continue;
                }
                double val = point.getVal();
                if (val <= 0.0) {
                    continue;
                }
                grid[i][y][x] += val * weight;
            }
        }
    }

    private List<List<HeatmapPointDto>> buildHeatmapsFromGridRaw(double[][][] grid,
                    int imageCount) {
        List<List<HeatmapPointDto>> aggregated = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            List<HeatmapPointDto> heatmap = new ArrayList<>();
            for (int y = 0; y < GRID_HEIGHT; y++) {
                for (int x = 0; x < GRID_WIDTH; x++) {
                    double val = grid[i][y][x];
                    if (val > 0.0) {
                        heatmap.add(new HeatmapPointDto(x, y, val));
                    }
                }
            }
            aggregated.add(heatmap);
        }
        return aggregated;
    }

    private List<List<HeatmapPointDto>> normalizeHeatmaps(List<List<HeatmapPointDto>> sums,
                    int imageCount) {
        List<List<HeatmapPointDto>> normalized = new ArrayList<>();
        int limit = Math.min(imageCount, sums.size());
        for (int i = 0; i < limit; i++) {
            List<HeatmapPointDto> heatmap = sums.get(i);
            if (heatmap == null || heatmap.isEmpty()) {
                normalized.add(new ArrayList<>());
                continue;
            }
            double total = 0.0;
            for (HeatmapPointDto point : heatmap) {
                if (point != null) {
                    total += point.getVal();
                }
            }
            List<HeatmapPointDto> norm = new ArrayList<>();
            if (total > 0.0) {
                for (HeatmapPointDto point : heatmap) {
                    if (point == null) {
                        continue;
                    }
                    double val = point.getVal();
                    if (val > 0.0) {
                        norm.add(new HeatmapPointDto(point.getX(), point.getY(), val / total));
                    }
                }
            }
            normalized.add(norm);
        }
        if (imageCount > limit) {
            normalized.addAll(emptyHeatmaps(imageCount - limit));
        }
        return normalized;
    }

    private List<List<HeatmapPointDto>> emptyHeatmaps(int imageCount) {
        List<List<HeatmapPointDto>> heatmaps = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            heatmaps.add(new ArrayList<>());
        }
        return heatmaps;
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
