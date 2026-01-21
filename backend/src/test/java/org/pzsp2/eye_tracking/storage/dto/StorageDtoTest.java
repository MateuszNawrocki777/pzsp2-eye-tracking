package org.pzsp2.eye_tracking.storage.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StorageDtoTest {

    @Test void testTestCreateRequest() {
        TestCreateRequest request = new TestCreateRequest();
        String title = "New Test";
        String desc = "Description";
        Integer time = 5000;

        request.setTitle(title);
        request.setDescription(desc);
        request.setDispGazeTracking(true);
        request.setDispTimeLeft(false);
        request.setTimePerImageMs(time);
        request.setRandomizeOrder(true);

        assertEquals(title, request.getTitle());
        assertEquals(desc, request.getDescription());
        assertTrue(request.getDispGazeTracking());
        assertFalse(request.getDispTimeLeft());
        assertEquals(time, request.getTimePerImageMs());
        assertTrue(request.getRandomizeOrder());

        assertNotNull(request.toString());
    }

    @Test void testTestDetailsDto() {
        TestDetailsDto dto = new TestDetailsDto();
        UUID id = UUID.randomUUID();
        List<String> links = new ArrayList<>();
        links.add("http://link1.com");
        links.add("http://link2.com");

        dto.setId(id);
        dto.setTitle("Title");
        dto.setDescription("Desc");
        dto.setDispGazeTracking(true);
        dto.setDispTimeLeft(true);
        dto.setTimePerImageMs(100);
        dto.setRandomizeOrder(false);
        dto.setFileLinks(links);

        assertEquals(id, dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Desc", dto.getDescription());
        assertTrue(dto.getDispGazeTracking());
        assertTrue(dto.getDispTimeLeft());
        assertEquals(100, dto.getTimePerImageMs());
        assertFalse(dto.getRandomizeOrder());

        assertEquals(links, dto.getFileLinks());
        assertNotSame(links, dto.getFileLinks());

        dto.setFileLinks(null);
        assertNull(dto.getFileLinks());
    }

    @Test void testTestListItemDto() {
        UUID id = UUID.randomUUID();
        String title = "List Item";
        String link = "http://image.png";

        TestListItemDto dto = new TestListItemDto(id, title, link);

        assertEquals(id, dto.getId());
        assertEquals(title, dto.getTitle());
        assertEquals(link, dto.getFirstImageLink());

        UUID newId = UUID.randomUUID();
        String newTitle = "New Title";
        String newLink = "http://new-link.com";

        dto.setId(newId);
        dto.setTitle(newTitle);
        dto.setFirstImageLink(newLink);

        assertEquals(newId, dto.getId());
        assertEquals(newTitle, dto.getTitle());
        assertEquals(newLink, dto.getFirstImageLink());

        assertNotNull(dto.toString());
    }
}