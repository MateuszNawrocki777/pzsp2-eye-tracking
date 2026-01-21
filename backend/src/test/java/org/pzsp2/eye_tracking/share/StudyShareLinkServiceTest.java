package org.pzsp2.eye_tracking.share;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.share.dto.StudyShareLinkCreateRequest;
import org.pzsp2.eye_tracking.share.dto.StudyShareLinkResponse;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyMaterial;
import org.pzsp2.eye_tracking.storage.StudyMaterialRepository;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class) class StudyShareLinkServiceTest {

    @Mock private StudyShareLinkRepository shareLinkRepository;
    @Mock private StudyRepository studyRepository;
    @Mock private StudyMaterialRepository materialRepository;

    private StudyShareLinkService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach void setUp() {
        service = new StudyShareLinkService(shareLinkRepository, studyRepository,
                        materialRepository);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test void createShareLink_success() {
        UUID testId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(ownerId);

        StudyShareLinkCreateRequest req = new StudyShareLinkCreateRequest();
        req.setMaxUses(5);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(shareLinkRepository.existsById(any())).willReturn(false);
        given(shareLinkRepository.save(any(StudyShareLink.class)))
                        .willAnswer(i -> i.getArgument(0));

        StudyShareLinkResponse response = service.createShareLinkForResearcher(testId, ownerId,
                        req);

        assertNotNull(response.getAccessLink());
        assertEquals(5, response.getMaxUses());
        assertNotNull(response.getAccessUrl());
        verify(shareLinkRepository).save(any(StudyShareLink.class));
    }

    @Test void createShareLink_throwsNotFound_whenStudyMissing() {
        UUID testId = UUID.randomUUID();
        given(studyRepository.findById(testId)).willReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service
                        .createShareLinkForResearcher(testId, UUID.randomUUID(), null));
    }

    @Test void createShareLink_throwsForbidden_whenNotOwner() {
        UUID testId = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(UUID.randomUUID());

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service
                        .createShareLinkForResearcher(testId, UUID.randomUUID(), null));
        assertEquals(FORBIDDEN, ex.getStatusCode());
    }

    @Test void getTestDetails_success_andIncrementsCounter() throws JsonProcessingException {
        String linkId = "valid-link";
        Study study = new Study();
        study.setTitle("Title");
        study.setSettings("{}");

        StudyShareLink link = new StudyShareLink();
        link.setAccessLink(linkId);
        link.setStudy(study);
        link.setUseCounter(0);
        link.setMaxUses(10);

        StudyMaterial mat = new StudyMaterial();
        mat.setMaterialId(UUID.randomUUID());
        mat.setStudy(study);

        given(shareLinkRepository.findById(linkId)).willReturn(Optional.of(link));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .willReturn(List.of(mat));

        TestDetailsDto dto = service.getTestDetailsForShareLink(linkId);

        assertNotNull(dto);
        assertEquals("Title", dto.getTitle());

        ArgumentCaptor<StudyShareLink> captor = ArgumentCaptor.forClass(StudyShareLink.class);
        verify(shareLinkRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getUseCounter());
    }

    @Test void getTestDetails_throwsNotFound_whenLinkMissing() {
        given(shareLinkRepository.findById("bad-link")).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.getTestDetailsForShareLink("bad-link"));
        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test void getTestDetails_throwsNotFound_whenExpired() {
        String linkId = "expired-link";
        StudyShareLink link = new StudyShareLink();
        link.setExpiresAt(LocalDateTime.now().minusDays(1));

        given(shareLinkRepository.findById(linkId)).willReturn(Optional.of(link));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.getTestDetailsForShareLink(linkId));
        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertEquals("Share link expired", ex.getReason());
    }

    @Test void getTestDetails_throwsNotFound_whenMaxUsesReached() {
        String linkId = "limit-link";
        StudyShareLink link = new StudyShareLink();
        link.setMaxUses(5);
        link.setUseCounter(5);

        given(shareLinkRepository.findById(linkId)).willReturn(Optional.of(link));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.getTestDetailsForShareLink(linkId));
        assertEquals(NOT_FOUND, ex.getStatusCode());
        assertEquals("Share link exhausted", ex.getReason());
    }

    @Test void getTestDetails_handlesNullUseCounter() throws JsonProcessingException {
        String linkId = "null-counter-link";
        Study study = new Study();
        study.setSettings("{}");

        StudyShareLink link = new StudyShareLink();
        link.setStudy(study);
        link.setUseCounter(null);
        link.setMaxUses(5);

        given(shareLinkRepository.findById(linkId)).willReturn(Optional.of(link));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study)).willReturn(List.of());

        service.getTestDetailsForShareLink(linkId);

        ArgumentCaptor<StudyShareLink> captor = ArgumentCaptor.forClass(StudyShareLink.class);
        verify(shareLinkRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getUseCounter(),
                        "Null should be treated like 0 and incremented to 1");
    }

    @Test void getTestDetails_handlesJsonError() {
        String linkId = "ok-link";
        Study study = new Study();
        study.setSettings("{ bad json");
        StudyShareLink link = new StudyShareLink();
        link.setStudy(study);

        given(shareLinkRepository.findById(linkId)).willReturn(Optional.of(link));

        assertThrows(RuntimeException.class, () -> service.getTestDetailsForShareLink(linkId));
    }

    @Test void deleteLinksForStudy_deletesAll() {
        Study study = new Study();
        given(shareLinkRepository.findAllByStudy(study)).willReturn(List.of(new StudyShareLink()));

        service.deleteLinksForStudy(study);

        verify(shareLinkRepository).deleteAll(anyList());
    }

    @Test void createShareLink_handlesCollision_andRetries() {
        UUID testId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(ownerId);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        given(shareLinkRepository.existsById(anyString())).willReturn(true).willReturn(false);

        given(shareLinkRepository.save(any(StudyShareLink.class)))
                        .willAnswer(i -> i.getArgument(0));

        StudyShareLinkResponse response = service.createShareLinkForResearcher(testId, ownerId,
                        new StudyShareLinkCreateRequest());

        assertNotNull(response.getAccessLink());
        verify(shareLinkRepository, times(2)).existsById(anyString());
    }

    @Test void createShareLink_handlesNullRequest() {
        UUID testId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(ownerId);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(shareLinkRepository.existsById(anyString())).willReturn(false);
        given(shareLinkRepository.save(any(StudyShareLink.class)))
                        .willAnswer(i -> i.getArgument(0));

        StudyShareLinkResponse response = service.createShareLinkForResearcher(testId, ownerId,
                        null);

        assertNotNull(response.getAccessLink());
        assertNull(response.getMaxUses());
        assertNull(response.getExpiresAt());
    }

    @Test void getTestDetails_allowsAccess_whenMaxUsesIsNull() throws JsonProcessingException {
        String linkId = "unlimited-link";
        Study study = new Study();
        study.setSettings("{}");

        StudyShareLink link = new StudyShareLink();
        link.setAccessLink(linkId);
        link.setStudy(study);
        link.setUseCounter(100);
        link.setMaxUses(null);

        given(shareLinkRepository.findById(linkId)).willReturn(Optional.of(link));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study)).willReturn(List.of());

        TestDetailsDto result = service.getTestDetailsForShareLink(linkId);

        assertNotNull(result);
        verify(shareLinkRepository).save(link);
        assertEquals(101, link.getUseCounter());
    }
}
