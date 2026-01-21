package org.pzsp2.eye_tracking.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data public class StudySessionCreateRequest {

    @JsonProperty("study_id") private UUID studyId;

    @JsonProperty("name") private String name;

    @JsonProperty("points_per_image") private List<List<List<Double>>> pointsPerImage;

    public List<List<List<Double>>> getPointsPerImage() {
        return pointsPerImage == null ? null : new ArrayList<>(pointsPerImage);
    }

    public void setPointsPerImage(List<List<List<Double>>> pointsPerImage) {
        this.pointsPerImage = pointsPerImage == null ? null : new ArrayList<>(pointsPerImage);
    }
}