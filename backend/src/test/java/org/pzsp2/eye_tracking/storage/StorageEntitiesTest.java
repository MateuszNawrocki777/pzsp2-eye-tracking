package org.pzsp2.eye_tracking.storage;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StorageEntitiesTest {

    @Test
    void testStudyEntity() {
        Study study = new Study();

        UUID id = UUID.randomUUID();
        UUID researcherId = UUID.randomUUID();
        String title = "Title";
        String desc = "Desc";
        String settings = "{}";
        String aggJson = "[]";

        study.setStudyId(id);
        study.setResearcherId(researcherId);
        study.setTitle(title);
        study.setDescription(desc);
        study.setSettings(settings);
        study.setAggregateHeatmapsJson(aggJson);

        study.setIsActive(false);

        assertEquals(id, study.getStudyId());
        assertEquals(researcherId, study.getResearcherId());
        assertEquals(title, study.getTitle());
        assertEquals(desc, study.getDescription());
        assertEquals(settings, study.getSettings());
        assertEquals(aggJson, study.getAggregateHeatmapsJson());
    }

    @Test
    void testStudyMaterialEntity() {
        StudyMaterial material = new StudyMaterial();

        UUID matId = UUID.randomUUID();
        Study study = new Study();
        String fileName = "file.jpg";
        String filePath = "/path/to/file.jpg";
        String contentType = "image/jpeg";
        Integer order = 1;

        material.setMaterialId(matId);
        material.setStudy(study);
        material.setFileName(fileName);
        material.setFilePath(filePath);
        material.setDisplayOrder(order);
        material.setContentType(contentType);

        assertEquals(matId, material.getMaterialId());
        assertEquals(study, material.getStudy());
        assertEquals(fileName, material.getFileName());
        assertEquals(filePath, material.getFilePath());
        assertEquals(order, material.getDisplayOrder());
        assertEquals(contentType, material.getContentType());
    }
}