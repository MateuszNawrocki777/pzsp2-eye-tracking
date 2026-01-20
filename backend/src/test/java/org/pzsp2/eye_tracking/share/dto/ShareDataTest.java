package org.pzsp2.eye_tracking.share.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.share.StudyShareLink;
import org.pzsp2.eye_tracking.storage.Study;

class ShareDataTest {

  @Test
  void testStudyShareLinkEntity() {
    StudyShareLink link = new StudyShareLink();
    Study study = new Study();
    LocalDateTime now = LocalDateTime.now();

    link.setAccessLink("uuid-link");
    link.setStudy(study);
    link.setMaxUses(10);
    link.setExpiresAt(now);
    link.setUseCounter(5);

    assertEquals("uuid-link", link.getAccessLink());
    assertEquals(study, link.getStudy());
    assertEquals(10, link.getMaxUses());
    assertEquals(now, link.getExpiresAt());
    assertEquals(5, link.getUseCounter());
    assertNotNull(link.getCreatedAt());
  }

  @Test
  void testCreateRequest() {
    StudyShareLinkCreateRequest req = new StudyShareLinkCreateRequest();
    LocalDateTime expiry = LocalDateTime.now().plusDays(1);

    req.setMaxUses(50);
    req.setExpiresAt(expiry);

    assertEquals(50, req.getMaxUses());
    assertEquals(expiry, req.getExpiresAt());
  }

  @Test
  void testResponse() {
    StudyShareLinkResponse resp = new StudyShareLinkResponse();
    LocalDateTime now = LocalDateTime.now();

    resp.setAccessLink("abc");
    resp.setMaxUses(100);
    resp.setExpiresAt(now);
    resp.setCreatedAt(now);
    resp.setUseCounter(1);
    resp.setAccessUrl("http://localhost/abc");

    assertEquals("abc", resp.getAccessLink());
    assertEquals(100, resp.getMaxUses());
    assertEquals(now, resp.getExpiresAt());
    assertEquals(now, resp.getCreatedAt());
    assertEquals(1, resp.getUseCounter());
    assertEquals("http://localhost/abc", resp.getAccessUrl());
  }
}
