package org.pzsp2.eye_tracking.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.pzsp2.eye_tracking.storage.dto.TestListItemDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;

import java.util.stream.Collectors;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final StudyMaterialRepository materialRepository;
    private final StudyRepository studyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // to JSON

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            StudyMaterialRepository materialRepository,
            StudyRepository studyRepository) {

        this.materialRepository = materialRepository;
        this.studyRepository = studyRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Can't create directory", ex);
        }
    }

    public java.util.List<TestListItemDto> getAllTestsForResearcher(UUID researcherId) {
        return studyRepository.findAll().stream()
                .filter(study -> researcherId.equals(study.getResearcherId()))
                .map(study -> {
                    String imageUrl = materialRepository.findFirstByStudyOrderByDisplayOrderAsc(study)
                            .map(material -> {
                                return org.springframework.web.servlet.support.ServletUriComponentsBuilder
                                        .fromCurrentContextPath()
                                        .path("/api/tests/files/")
                                        .path(material.getMaterialId().toString())
                                        .toUriString();
                            })
                            .orElse(null);

                    return new TestListItemDto(study.getStudyId(), study.getTitle(), imageUrl);
                }).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UUID createFullTest(TestCreateRequest request, MultipartFile[] files, UUID researcherId) {
        try {
            Study study = new Study();
            study.setTitle(request.getTitle());
            study.setDescription(request.getDescription());
            study.setResearcherId(researcherId);
            study.setSettings(objectMapper.writeValueAsString(request));

            Study savedStudy = studyRepository.save(study);

            int order = 1;
            for (MultipartFile file : files) {
                storeSingleFile(file, savedStudy, order++);
            }

            return savedStudy.getStudyId();

        } catch (IOException e) {
            throw new RuntimeException("Error occured while saving JSON settings", e);
        }
    }

    private void storeSingleFile(MultipartFile file, Study study, int order) {
        String originalFileName = StringUtils
                .cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");

        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0)
            extension = originalFileName.substring(i);
        String storedFileName = UUID.randomUUID().toString() + extension;

        try {
            Files.copy(file.getInputStream(), this.fileStorageLocation.resolve(storedFileName),
                    StandardCopyOption.REPLACE_EXISTING);

            StudyMaterial material = new StudyMaterial();
            material.setStudy(study);
            material.setFileName(originalFileName);
            material.setFilePath(storedFileName);
            material.setDisplayOrder(order);
            material.setContentType(file.getContentType());

            materialRepository.save(material);

        } catch (IOException ex) {
            throw new RuntimeException("Couldn't save file " + originalFileName, ex);
        }
    }

    public Resource loadFileAsResource(UUID fileId) {
        StudyMaterial material = materialRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File does not exist"));

        try {
            Path filePath = this.fileStorageLocation.resolve(material.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists())
                return resource;
            else
                throw new RuntimeException("File not found");
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Wrong path", ex);
        }
    }

    public String getContentType(UUID fileId) {
        return materialRepository.findById(fileId).map(StudyMaterial::getContentType)
                .orElse("application/octet-stream");
    }

    public String getOriginalName(UUID fileId) {
        return materialRepository.findById(fileId).map(StudyMaterial::getFileName).orElse("file");
    }

    public TestDetailsDto getTestDetailsForResearcher(UUID testId, UUID researcherId) {
        Study study = studyRepository.findById(testId).orElseThrow(
                () -> new ResponseStatusException(NOT_FOUND, "Couldn't find study with the following ID: " + testId));

        if (!researcherId.equals(study.getResearcherId())) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        List<StudyMaterial> materials = materialRepository.findAllByStudyOrderByDisplayOrderAsc(study);

        TestDetailsDto dto = new TestDetailsDto();
        dto.setId(study.getStudyId());
        dto.setTitle(study.getTitle());
        dto.setDescription(study.getDescription());

        try {
            TestCreateRequest settings = objectMapper.readValue(study.getSettings(), TestCreateRequest.class);
            dto.setDispGazeTracking(settings.getDispGazeTracking());
            dto.setDispTimeLeft(settings.getDispTimeLeft());
            dto.setTimePerImageMs(settings.getTimePerImageMs());
            dto.setRandomizeOrder(settings.getRandomizeOrder());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while reading settings", e);
        }

        List<String> links = materials.stream()
                .map(m -> org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/tests/files/")
                        .path(m.getMaterialId().toString())
                        .toUriString())
                .collect(Collectors.toList());

        dto.setFileLinks(links);
        return dto;
    }

    @Transactional
    public void deleteTestForResearcher(UUID testId, UUID researcherId) {
        Study study = studyRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Test does not exist"));

        if (!researcherId.equals(study.getResearcherId())) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        List<StudyMaterial> materials = materialRepository.findAllByStudyOrderByDisplayOrderAsc(study);

        for (StudyMaterial material : materials) {
            try {
                Path path = this.fileStorageLocation.resolve(material.getFilePath());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Couldn't delete file: " + material.getFilePath());
            }
        }

        materialRepository.deleteAll(materials);
        studyRepository.delete(study);
    }

    @Transactional
    public void updateTestSettingsForResearcher(UUID testId, TestCreateRequest newSettings, UUID researcherId) {
        Study study = studyRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Test does not exist"));

        if (!researcherId.equals(study.getResearcherId())) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        study.setTitle(newSettings.getTitle());
        study.setDescription(newSettings.getDescription());

        try {
            study.setSettings(objectMapper.writeValueAsString(newSettings));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error occurred while saving settings", e);
        }

        studyRepository.save(study);
    }

    @Transactional
    public void addFileToTestForResearcher(UUID testId, MultipartFile file, UUID researcherId) {
        Study study = studyRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Test does not exist"));

        if (!researcherId.equals(study.getResearcherId())) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        int currentCount = materialRepository.findAllByStudyOrderByDisplayOrderAsc(study).size();

        storeSingleFile(file, study, currentCount + 1);
    }

    @Transactional
    public void deleteSingleFileForResearcher(UUID fileId, UUID researcherId) {
        StudyMaterial material = materialRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "File does not exist"));

        if (material.getStudy() == null || !researcherId.equals(material.getStudy().getResearcherId())) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied");
        }

        try {
            Path path = this.fileStorageLocation.resolve(material.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
        }

        materialRepository.delete(material);
    }
}