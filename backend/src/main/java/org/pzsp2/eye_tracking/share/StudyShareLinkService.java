package org.pzsp2.eye_tracking.share;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.pzsp2.eye_tracking.share.dto.StudyShareLinkCreateRequest;
import org.pzsp2.eye_tracking.share.dto.StudyShareLinkResponse;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyMaterial;
import org.pzsp2.eye_tracking.storage.StudyMaterialRepository;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service public class StudyShareLinkService {

    private final StudyShareLinkRepository shareLinkRepository;
    private final StudyRepository studyRepository;
    private final StudyMaterialRepository materialRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StudyShareLinkService(StudyShareLinkRepository shareLinkRepository,
                    StudyRepository studyRepository, StudyMaterialRepository materialRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.studyRepository = studyRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    @SuppressWarnings("null") public StudyShareLinkResponse createShareLinkForResearcher(
                    UUID testId, UUID researcherId, StudyShareLinkCreateRequest request) {
        Study study = studyRepository.findById(Objects.requireNonNull(testId)).orElseThrow(
                        () -> new ResponseStatusException(NOT_FOUND, "Test does not exist"));

        if (!researcherId.equals(study.getResearcherId())) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        String accessLink = UUID.randomUUID().toString();
        while (shareLinkRepository.existsById(Objects.requireNonNull(accessLink))) {
            accessLink = UUID.randomUUID().toString();
        }

        StudyShareLink link = new StudyShareLink();
        link.setAccessLink(accessLink);
        link.setStudy(study);
        if (request != null) {
            link.setMaxUses(request.getMaxUses());
            link.setExpiresAt(request.getExpiresAt());
        }

        StudyShareLink saved = shareLinkRepository.save(link);

        StudyShareLinkResponse resp = new StudyShareLinkResponse();
        resp.setAccessLink(saved.getAccessLink());
        resp.setMaxUses(saved.getMaxUses());
        resp.setExpiresAt(saved.getExpiresAt());
        resp.setCreatedAt(saved.getCreatedAt());
        resp.setUseCounter(saved.getUseCounter());
        resp.setAccessUrl(org.springframework.web.servlet.support.ServletUriComponentsBuilder
                        .fromCurrentContextPath().path("/api/tests/share/")
                        .path(Objects.requireNonNull(saved.getAccessLink())).toUriString());

        return resp;
    }

    @Transactional
    @SuppressWarnings("null") public TestDetailsDto getTestDetailsForShareLink(String accessLink) {
        StudyShareLink link = shareLinkRepository.findById(Objects.requireNonNull(accessLink))
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                                        "Share link not found"));

        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        if (link.getExpiresAt() != null && now.isAfter(link.getExpiresAt())) {
            throw new ResponseStatusException(NOT_FOUND, "Share link expired");
        }
        if (link.getMaxUses() != null && link.getUseCounter() != null
                        && link.getUseCounter() >= link.getMaxUses()) {
            throw new ResponseStatusException(NOT_FOUND, "Share link exhausted");
        }

        link.setUseCounter((link.getUseCounter() == null ? 0 : link.getUseCounter()) + 1);
        shareLinkRepository.save(link);

        Study study = link.getStudy();
        List<StudyMaterial> materials = materialRepository
                        .findAllByStudyOrderByDisplayOrderAsc(study);

        TestDetailsDto dto = new TestDetailsDto();
        dto.setId(study.getStudyId());
        dto.setTitle(study.getTitle());
        dto.setDescription(study.getDescription());

        try {
            TestCreateRequest settings = objectMapper.readValue(study.getSettings(),
                            TestCreateRequest.class);
            dto.setDispGazeTracking(settings.getDispGazeTracking());
            dto.setDispTimeLeft(settings.getDispTimeLeft());
            dto.setTimePerImageMs(settings.getTimePerImageMs());
            dto.setRandomizeOrder(settings.getRandomizeOrder());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while reading settings", e);
        }

        List<String> links = materials.stream().map(m -> {
            String materialId = Objects.requireNonNull(m.getMaterialId()).toString();
            return org.springframework.web.servlet.support.ServletUriComponentsBuilder
                            .fromCurrentContextPath().path("/api/tests/files/").path(materialId)
                            .toUriString();
        }).collect(Collectors.toList());

        dto.setFileLinks(links);
        return dto;
    }

    @Transactional public void deleteLinksForStudy(Study study) {
        shareLinkRepository.deleteAll(
                        Objects.requireNonNull(shareLinkRepository.findAllByStudy(study)));
    }
}
