package org.pzsp2.eye_tracking.storage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "StudyMaterials")
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
                "EI_EXPOSE_REP2"}, justification = "JPA entities are mutable by design") public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "material_id") private UUID materialId;

    @ManyToOne
    @JoinColumn(name = "study_id") private Study study;

    @Column(name = "file_name") private String fileName;

    @Column(name = "file_path") private String filePath;

    @Column(name = "display_order") private Integer displayOrder;

    @Column(name = "uploaded_at") private LocalDateTime uploadedAt = LocalDateTime.now();

    private String contentType;

    public UUID getMaterialId() {
        return materialId;
    }

    public void setMaterialId(UUID materialId) {
        this.materialId = materialId;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}