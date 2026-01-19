package org.pzsp2.eye_tracking.session;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.pzsp2.eye_tracking.auth.AuthenticatedUser;
import org.pzsp2.eye_tracking.session.dto.StudySessionAggregateHeatmapDto;
import org.pzsp2.eye_tracking.session.dto.StudySessionCreateRequest;
import org.pzsp2.eye_tracking.session.dto.StudySessionCreateResponse;
import org.pzsp2.eye_tracking.session.dto.StudySessionDetailsDto;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {

  private final StudySessionService sessionService;
  private final StudyRepository studyRepository;

  public StudySessionController(
      StudySessionService sessionService, StudyRepository studyRepository) {
    this.sessionService = sessionService;
    this.studyRepository = studyRepository;
  }

  @PostMapping
  public ResponseEntity<StudySessionCreateResponse> createSession(
      @RequestBody StudySessionCreateRequest request) {
    UUID sessionId = sessionService.createSession(request);
    return ResponseEntity.ok(new StudySessionCreateResponse(sessionId));
  }

  @GetMapping("/{sessionId}")
  public ResponseEntity<StudySessionDetailsDto> getSession(
      @PathVariable UUID sessionId, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    if (authenticatedUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    StudySessionDetailsDto dto = sessionService.getSession(sessionId);
    var study =
        studyRepository
            .findById(Objects.requireNonNull(dto.getStudyId()))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study does not exist"));
    if (!authenticatedUser.userId().equals(study.getResearcherId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/test/{testId}")
  public ResponseEntity<List<StudySessionDetailsDto>> getSessionsForStudy(
      @PathVariable UUID testId, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    if (authenticatedUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    var study =
        studyRepository
            .findById(Objects.requireNonNull(testId))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study does not exist"));
    if (!authenticatedUser.userId().equals(study.getResearcherId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    return ResponseEntity.ok(sessionService.getSessionsForStudy(testId));
  }

  @GetMapping("/test/{testId}/heatmap")
  public ResponseEntity<StudySessionAggregateHeatmapDto> getAggregateHeatmapForStudy(
      @PathVariable UUID testId, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
    if (authenticatedUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    var study =
        studyRepository
            .findById(Objects.requireNonNull(testId))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Study does not exist"));
    if (!authenticatedUser.userId().equals(study.getResearcherId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    StudySessionAggregateHeatmapDto dto = new StudySessionAggregateHeatmapDto();
    dto.setStudyId(testId);
    dto.setHeatmaps(sessionService.getAggregateHeatmapsForStudy(testId));
    return ResponseEntity.ok(dto);
  }
}
