package org.pzsp2.eye_tracking.session.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor public class GazePointDto {
    private double x;
    private double y;
}
