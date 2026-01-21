package org.pzsp2.eye_tracking.storage.dto;

import java.util.UUID;

public class TestListItemDto {
    private UUID id;
    private String title;
    private String firstImageLink;

    public TestListItemDto(UUID id, String title, String firstImageLink) {
        this.id = id;
        this.title = title;
        this.firstImageLink = firstImageLink;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstImageLink() {
        return firstImageLink;
    }

    public void setFirstImageLink(String firstImageLink) {
        this.firstImageLink = firstImageLink;
    }
}
