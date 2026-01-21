package org.pzsp2.eye_tracking.storage.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDetailsDto {
    private UUID id;
    private String title;
    private String description;

    private Boolean dispGazeTracking;
    private Boolean dispTimeLeft;
    private Integer timePerImageMs;
    private Boolean randomizeOrder;

    private List<String> fileLinks;

    public TestDetailsDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDispGazeTracking() {
        return dispGazeTracking;
    }

    public void setDispGazeTracking(Boolean dispGazeTracking) {
        this.dispGazeTracking = dispGazeTracking;
    }

    public Boolean getDispTimeLeft() {
        return dispTimeLeft;
    }

    public void setDispTimeLeft(Boolean dispTimeLeft) {
        this.dispTimeLeft = dispTimeLeft;
    }

    public Integer getTimePerImageMs() {
        return timePerImageMs;
    }

    public void setTimePerImageMs(Integer timePerImageMs) {
        this.timePerImageMs = timePerImageMs;
    }

    public Boolean getRandomizeOrder() {
        return randomizeOrder;
    }

    public void setRandomizeOrder(Boolean randomizeOrder) {
        this.randomizeOrder = randomizeOrder;
    }

    public List<String> getFileLinks() {
        return fileLinks == null ? null : new ArrayList<>(fileLinks);
    }

    public void setFileLinks(List<String> fileLinks) {
        this.fileLinks = fileLinks == null ? null : new ArrayList<>(fileLinks);
    }
}