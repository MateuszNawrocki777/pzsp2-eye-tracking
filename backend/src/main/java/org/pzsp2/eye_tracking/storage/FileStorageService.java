package org.pzsp2.eye_tracking.storage;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.pzsp2.eye_tracking.share.StudyShareLinkService;
import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;
import org.pzsp2.eye_tracking.storage.dto.TestListItemDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("null")
@Service
public class FileStorageService {

  private final Path fileStorageLocation;
  private final StudyMaterialRepository materialRepository;
  private final StudyRepository studyRepository;
  private final StudyShareLinkService shareLinkService;
  private final ObjectMapper objectMapper = new ObjectMapper(); // to JSON

  public FileStorageService(
      @Value("${file.upload-dir}") String uploadDir,
      StudyMaterialRepository materialRepository,
      StudyRepository studyRepository,
      StudyShareLinkService shareLinkService) {

    this.materialRepository = materialRepository;
    this.studyRepository = studyRepository;
    this.shareLinkService = shareLinkService;
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new RuntimeException("Can't create directory", ex);
    }
  }

  @SuppressWarnings("null")
  public java.util.List<TestListItemDto> getAllTestsForResearcher(UUID researcherId) {
    return studyRepository.findAll().stream()
        .filter(study -> researcherId.equals(study.getResearcherId()))
        .map(
            study -> {
              String imageUrl =
                  materialRepository
                      .findFirstByStudyOrderByDisplayOrderAsc(study)
                      .map(
                          material -> {
                            String materialId =
                                Objects.requireNonNull(material.getMaterialId()).toString();
                            return org.springframework.web.servlet.support
                                .ServletUriComponentsBuilder.fromCurrentContextPath()
                                .path("/api/tests/files/")
                                .path(materialId)
                                .toUriString();
                          })
                      .orElse(null);

              return new TestListItemDto(study.getStudyId(), study.getTitle(), imageUrl);
            })
        .collect(java.util.stream.Collectors.toList());
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

  @SuppressWarnings("null")
  private void storeSingleFile(MultipartFile file, Study study, int order) {
    String originalFileName =
        StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "unknown"));

    String extension = "";
    int i = originalFileName.lastIndexOf('.');
    if (i > 0) extension = originalFileName.substring(i);
    String storedFileName = UUID.randomUUID().toString() + extension;

    try {
      Files.copy(
          file.getInputStream(),
          this.fileStorageLocation.resolve(storedFileName),
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
    StudyMaterial material =
        materialRepository
            .findById(Objects.requireNonNull(fileId))
            .orElseThrow(() -> new RuntimeException("File does not exist"));

    try {
      Path filePath = this.fileStorageLocation.resolve(material.getFilePath()).normalize();
      Resource resource = new UrlResource(Objects.requireNonNull(filePath.toUri()));
      if (resource.exists()) return resource;
      else throw new RuntimeException("File not found");
    } catch (MalformedURLException ex) {
      throw new RuntimeException("Wrong path", ex);
    }
  }

  public String getContentType(UUID fileId) {
    return materialRepository
        .findById(Objects.requireNonNull(fileId))
        .map(StudyMaterial::getContentType)
        .orElse("application/octet-stream");
  }

  public String getOriginalName(UUID fileId) {
    return materialRepository
        .findById(Objects.requireNonNull(fileId))
        .map(StudyMaterial::getFileName)
        .orElse("file");
  }

  @SuppressWarnings("null")
  public TestDetailsDto getTestDetailsForResearcher(UUID testId, UUID researcherId) {
    Study study =
        studyRepository
            .findById(Objects.requireNonNull(testId))
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        NOT_FOUND, "Couldn't find study with the following ID: " + testId));

    if (!researcherId.equals(study.getResearcherId())) {
      throw new ResponseStatusException(FORBIDDEN, "Access denied");
    }

    List<StudyMaterial> materials = materialRepository.findAllByStudyOrderByDisplayOrderAsc(study);

    TestDetailsDto dto = new TestDetailsDto();
    dto.setId(study.getStudyId());
    dto.setTitle(study.getTitle());
    dto.setDescription(study.getDescription());

    try {
      TestCreateRequest settings =
          objectMapper.readValue(study.getSettings(), TestCreateRequest.class);
      dto.setDispGazeTracking(settings.getDispGazeTracking());
      dto.setDispTimeLeft(settings.getDispTimeLeft());
      dto.setTimePerImageMs(settings.getTimePerImageMs());
      dto.setRandomizeOrder(settings.getRandomizeOrder());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error occurred while reading settings", e);
    }

    List<String> links =
        materials.stream()
            .map(
                m -> {
                  String materialId = Objects.requireNonNull(m.getMaterialId()).toString();
                  return org.springframework.web.servlet.support.ServletUriComponentsBuilder
                      .fromCurrentContextPath()
                      .path("/api/tests/files/")
                      .path(materialId)
                      .toUriString();
                })
            .collect(Collectors.toList());

    dto.setFileLinks(links);
    return dto;
  }

  @Transactional
  public void deleteTestForResearcher(UUID testId, UUID researcherId) {
    Study study =
        studyRepository
            .findById(Objects.requireNonNull(testId))
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

    materialRepository.deleteAll(Objects.requireNonNull(materials));
    shareLinkService.deleteLinksForStudy(study);
    studyRepository.delete(study);
  }

  @Transactional
  public void updateTestSettingsForResearcher(
      UUID testId, TestCreateRequest newSettings, UUID researcherId) {
    Study study =
        studyRepository
            .findById(Objects.requireNonNull(testId))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Test does not exist"));

    if (!researcherId.equals(study.getResearcherId())) {
      throw new ResponseStatusException(FORBIDDEN, "Access denied");
    }

    if (newSettings.getTitle() != null) {
      study.setTitle(newSettings.getTitle());
    }
    if (newSettings.getDescription() != null) {
      study.setDescription(newSettings.getDescription());
    }

    TestCreateRequest mergedSettings = new TestCreateRequest();
    try {
      if (study.getSettings() != null && !study.getSettings().isBlank()) {
        mergedSettings = objectMapper.readValue(study.getSettings(), TestCreateRequest.class);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error occurred while reading settings", e);
    }

    if (newSettings.getDispGazeTracking() != null) {
      mergedSettings.setDispGazeTracking(newSettings.getDispGazeTracking());
    }
    if (newSettings.getDispTimeLeft() != null) {
      mergedSettings.setDispTimeLeft(newSettings.getDispTimeLeft());
    }
    if (newSettings.getTimePerImageMs() != null) {
      mergedSettings.setTimePerImageMs(newSettings.getTimePerImageMs());
    }
    if (newSettings.getRandomizeOrder() != null) {
      mergedSettings.setRandomizeOrder(newSettings.getRandomizeOrder());
    }

    if (newSettings.getTitle() != null) {
      mergedSettings.setTitle(newSettings.getTitle());
    }
    if (newSettings.getDescription() != null) {
      mergedSettings.setDescription(newSettings.getDescription());
    }

    try {
      study.setSettings(objectMapper.writeValueAsString(mergedSettings));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error occurred while saving settings", e);
    }

    studyRepository.save(study);
  }
}
