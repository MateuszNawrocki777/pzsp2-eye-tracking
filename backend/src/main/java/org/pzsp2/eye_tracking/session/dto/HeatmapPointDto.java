package org.pzsp2.eye_tracking.session.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor public class HeatmapPointDto {
    private int x;
    private int y;
    private double val;
}
