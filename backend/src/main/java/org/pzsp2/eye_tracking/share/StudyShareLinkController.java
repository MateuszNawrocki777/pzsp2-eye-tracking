package org.pzsp2.eye_tracking.share;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.UUID;
import org.pzsp2.eye_tracking.auth.AuthenticatedUser;
import org.pzsp2.eye_tracking.share.dto.StudyShareLinkCreateRequest;
import org.pzsp2.eye_tracking.share.dto.StudyShareLinkResponse;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring Dependency Injection")
public class StudyShareLinkController {

  private final StudyShareLinkService shareLinkService;

  public StudyShareLinkController(StudyShareLinkService shareLinkService) {
    this.shareLinkService = shareLinkService;
  }

  @PostMapping("/{testId}/share")
  public ResponseEntity<StudyShareLinkResponse> createShareLink(
      @PathVariable UUID testId,
      @RequestBody(required = false) StudyShareLinkCreateRequest request,
      @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

    if (authenticatedUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    StudyShareLinkResponse response =
        shareLinkService.createShareLinkForResearcher(testId, authenticatedUser.userId(), request);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/share/{accessLink}")
  public ResponseEntity<TestDetailsDto> getShareLinkDetails(@PathVariable String accessLink) {
    TestDetailsDto details = shareLinkService.getTestDetailsForShareLink(accessLink);
    return ResponseEntity.ok(details);
  }
}