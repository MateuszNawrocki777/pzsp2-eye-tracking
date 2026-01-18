package org.pzsp2.eye_tracking.session.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionCreateResponse {

    @JsonProperty("session_id")
    private UUID sessionId;

}