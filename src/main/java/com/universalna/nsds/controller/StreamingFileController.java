package com.universalna.nsds.controller;


import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universalna.nsds.component.AuthorizedPartyProvider;
import com.universalna.nsds.component.content.ContentDownloader;
import com.universalna.nsds.config.ApplicationConfigurationProperties;
import com.universalna.nsds.controller.dto.MetadataDTO;
import com.universalna.nsds.controller.dto.MetadataWithUrlDTO;
import com.universalna.nsds.controller.dto.TempFileMetadataDTO;
import com.universalna.nsds.exception.*;
import com.universalna.nsds.model.File;
import com.universalna.nsds.model.IdWithPreview;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.Source;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.service.postgres.PostgresMetaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONObject;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.universalna.nsds.Profiles.STREAMING;
import static com.universalna.nsds.controller.PathConstants.ID;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@Profile(STREAMING)
@Validated
@RestController
@Api(value = "Контроллер для работы с потоками контента")
public class StreamingFileController extends AbstractFileController
                                     implements IoExceptionHandler,
                                                StreamingController,
                                                Zipper {

    @Autowired
    private Mapper metadataMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    @Autowired
    private ContentDownloader contentDownloader;

    @Autowired
    private AuthorizedPartyProvider authorizedPartyProvider;

    @Autowired
    private ApplicationConfigurationProperties applicationConfigurationProperties;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private PostgresMetaFileService postgresMetaFileService;

    private Set<String> restrictedExtensions;

    private static final String ROTATE = "rotate";

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingFileController.class);

    // Максимальный размер пачки, которая за раз должна обрабатываться при обновлении файлов
    @Value("${filesMassUpdate.batchSize:50}")
    private int filesMassUpdate;

    @PostConstruct
    public void setRestrictedExtensions() {
        restrictedExtensions = applicationConfigurationProperties.getExtension().getRestricted().stream().map(ApplicationConfigurationProperties.Extension.Restricted::getExtension).collect(Collectors.toSet());
    }

    @ApiOperation(httpMethod = "GET",value = "Upload file using POST multipart/form-data", notes = "Swagger GET method is workaround, use POST to upload file")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "metadata", value = "Metadata json", required = true, paramType = "form", dataTypeClass = MetadataDTO.class, example = "{\n" +
                    "  \"name\": \"image.jpg\",\n" +
                    " \"relation\":\"SETTLEMENT_NOTIFICATION\",\n" +
                    "  \"relationId\": 1256,\n" +
                    "  \"description\": \"testDescription\",\n" +
                    "  \"origin\": \"GENERATED\",\n" +
                    "  \"tags\": [{\"tag\": \"tag1\"},  {\"tag\": \"tag2\"}]\n" +
                    "}"),
            @ApiImplicitParam(name = "file", required = true, paramType = "form"),
    })
    @PostMapping(value = ROOT, consumes = "multipart/form-data")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"api_nsds_upload\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"chatbot\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    public ResponseEntity<String> upload(final HttpServletRequest request) {
        InputStream firstFoundFileInputStream = null;
        InputStream firstFoundMetadataJsonInputStream = null;
        String metadataJson = null;
        String newFileId = null;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator items = upload.getItemIterator(request);
            while (items.hasNext()) {
                final FileItemStream item = items.next();
                if (item.isFormField() && firstFoundMetadataJsonInputStream == null && "metadata".equalsIgnoreCase(item.getFieldName())) {
                    firstFoundMetadataJsonInputStream = item.openStream();
                    metadataJson = collectInputStreamAsString(firstFoundMetadataJsonInputStream, Charset.forName(request.getCharacterEncoding()));
                }
                if (!item.isFormField() && firstFoundFileInputStream == null && "file".equalsIgnoreCase(item.getFieldName())) {
                    firstFoundFileInputStream = item.openStream();
                }
                if (firstFoundFileInputStream != null && metadataJson != null) {
                    final MetadataDTO metadataDTO = objectMapper.readValue(metadataJson, MetadataDTO.class);
                    validate(metadataDTO);
                    final Metadata metadata = metadataMapper.toModel(metadataDTO, resolveSource());
                    newFileId = fileService.create(firstFoundFileInputStream, metadata);
                    updateFieldsMetadata(newFileId, fileService.get(newFileId));
                }
            }
        } catch (IOException | FileUploadException e) {
            throw new SystemException("Exception caught while trying to putContent file, maybe incorrect order of incoming parameters, file should be the last parameter", e);
        }
        if (newFileId != null) {
            return new ResponseEntity<>(newFileId, OK);
        } else {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    @PostMapping(value = ROOT)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"api_nsds_upload\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"chatbot\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    @ApiOperation(value = "Метод для закачки файла по ссылке")
    public ResponseEntity<String> uploadFromUrl(@RequestBody @Valid final MetadataWithUrlDTO metadataWithUrlDTO) throws IOException {
        String id = "";
        try (InputStream contentStream = contentDownloader.getContentByUrl(metadataWithUrlDTO.getUrl())) {
            final Metadata metadata = metadataMapper.toModel(metadataWithUrlDTO, resolveSource());
            id = fileService.create(contentStream, metadata);
            updateFieldsMetadata(id, fileService.get(id));
        }
        return ResponseEntity.ok(id);
    }

    // method developed for profitsoft, do not touch.
    @PostMapping(value = ROOT + "/temp", consumes = "multipart/form-data")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"api_nsds_upload\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    public ResponseEntity<IdWithPreview> uploadAsTemporaryFile(final HttpServletRequest request) {
        InputStream firstFoundFileInputStream = null;
        InputStream firstFoundMetadataJsonInputStream = null;
        String metadataJson = null;
        String newFileId = null;
        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator items = upload.getItemIterator(request);
            while (items.hasNext()) {
                final FileItemStream item = items.next();
                if (item.isFormField() && firstFoundMetadataJsonInputStream == null && "metadata".equalsIgnoreCase(item.getFieldName())) {
                    firstFoundMetadataJsonInputStream = item.openStream();
                    metadataJson = collectInputStreamAsString(firstFoundMetadataJsonInputStream, Charset.forName(request.getCharacterEncoding()));
                }
                if (!item.isFormField() && firstFoundFileInputStream == null && "file".equalsIgnoreCase(item.getFieldName())) {
                    firstFoundFileInputStream = item.openStream();
                }
                if (firstFoundFileInputStream != null && metadataJson != null) {
                    final TempFileMetadataDTO tempFileMetadataDTO = objectMapper.readValue(metadataJson, TempFileMetadataDTO.class);
                    validate(tempFileMetadataDTO);
                    final Metadata metadata = metadataMapper.toModel(tempFileMetadataDTO, resolveSource());
                    newFileId = fileService.createTemporaryFile(firstFoundFileInputStream, metadata);
                    updateFieldsMetadata(newFileId, fileService.get(newFileId));
                }
            }
        } catch (IOException | FileUploadException e) {
            throw new SystemException("Exception caught while trying to putContent file, maybe incorrect order of incoming parameters, file should be the last parameter", e);
        }
        if (newFileId != null) {
            final String emptyPreviewValue = ""; // used for backward compatibility
            final IdWithPreview idWithPreview = IdWithPreview.builder().id(UUID.fromString(newFileId)).preview(emptyPreviewValue).build();
            return new ResponseEntity<>(idWithPreview, OK);
        } else {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    private String updateInfoAndExifByFileId(String fileId) {
        File file = fileService.get(fileId);
        Map<String, String> exifInfo = getImageExif(file);
        fileService.updateInfoAndExif(fileId, getRotateToJson(exifInfo), JSONObject.toJSONString(exifInfo));
        return JSONObject.toJSONString(exifInfo);
    }

    /** Метод обновляет поля info и exif в metadata*/
    private void updateFieldsMetadata(String id, File file) {
        Map<String, String> exifInfo = getImageExif(file);
        fileService.updateInfoAndExif(id, getRotateToJson(exifInfo), JSONObject.toJSONString(exifInfo));
    }

    /** Возвращает rotate и его значение в формате json*/
    private String getRotateToJson(Map<String, String> exif) {
        return "{\"rotate\": " + exif.get(ROTATE) + "}";
    }

    /**
     * Получает из картинки всю exif информацию и кладет в Map.
     * @param file File
     * @return Map<String, Integer>
     */
    private Map<String, String> getImageExif(File file) {
        Map<String, String> result = new HashMap<>();
        try (InputStream content = file.getContent()) {
            com.drew.metadata.Metadata imageMetadata = ImageMetadataReader.readMetadata(content);
            result.putAll(getExif(imageMetadata));
        } catch (MetadataException | ImageProcessingException | IOException e) {
            result.put(ROTATE, "0");
        }
        return result;
    }

    /**
     * Метод возвращает полную Exif информацию по картинке.
     * Если картинка в формате jpg/jpeg/jfif и содержит в себе exif информацию orientation тогда метод извлечет данные,
     * переведет значение в угол поворота и покладет в Map с ключем "rotate". Если информация orientation отсутствует тогда
     * "rotate" = "0"
     * @param imageMetadata com.drew.metadata.Metadata
     * @return Map<String, String>
     * @throws MetadataException
     */
    private Map<String, String> getExif(com.drew.metadata.Metadata imageMetadata) throws MetadataException {
        Map<String, String> exifInfo = new HashMap<>();
        final Iterable<Directory> directories = imageMetadata.getDirectories();
        if (directories != null) {
            for (Directory directory: directories) {
                if("Exif IFD0".equals(directory.getName())) {
                    exifInfo.put(ROTATE, Integer.toString(getDegree(directory.getInt(ExifIFD0Directory.TAG_ORIENTATION))));
                }
                Collection<Tag> tags = directory.getTags();
                for (Tag tag : tags) {
                    exifInfo.put(skipNonPrintable(tag.getTagName()), skipNonPrintable(tag.getDescription()));
                }
            }
            if(exifInfo.get(ROTATE) == null) {
                exifInfo.put(ROTATE, "0");
            }
        } else {
            exifInfo.put(ROTATE, "0");
        }
        return exifInfo;
    }

    /** Заменяет непечатаемые символы unicode */
    private String skipNonPrintable(String value) {
        if (value != null) {
            return value.replaceAll("\\P{Print}", "");
        }
        return null;
    }

    /**
     * Приводит значение из поля orientation в градусы. Значение orientation достаем из exif информации в картинке
     * (если оно задано). Эти значения соответствуют соответствующим градусам поворота 3-180 6-90 8-270 по всем другим
     * кодам rotate: 0. Зеркальные отображения не учитываем.
     * @param orientation int value
     * @return degree
     */
    private int getDegree(int orientation) {
        switch (orientation) {
            case 3: // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
                return 180;
            case 6: // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
                return 90;
            case 8:  // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
                return 270;
            default:
                return 0; // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
        }
    }

    @GetMapping(value = ROOT + ID)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\", \"api_nsds_download\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    @ApiOperation(value = "Метод для получения контента файла")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable("fileId") @NotBlank final String fileId) {
        final File content = fileService.get(fileId);
        return streamContent(content);
    }

    @PostMapping(value = ROOT + "/getExifBy" + ID, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\", \"api_nsds_download\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    @ApiOperation(value = "Метод для получения exif информации по айди файлу")
    public ResponseEntity<String> getExifByFileId(@PathVariable("fileId") @NotBlank final String fileId) {
        MetadataEntity metadataEntity = metadataRepository.findById(UUID.fromString(fileId)).orElseThrow(() -> new NotFoundException("File not found"));
        String exif = metadataEntity.getExif();
        if (StringUtils.isNotBlank(exif)) {
            return ResponseEntity.ok(exif);
        }
        return ResponseEntity.ok(updateInfoAndExifByFileId(fileId));
    }

    @PutMapping(value = ROOT + "/updateExifAndInfo")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    @ApiOperation(value = "Заполнение exif, info информацией по файлу",
            notes = "numberUpdatedRecords - количество записей которые нужно обновить информацией")
    public void updateExifAndInfo(@RequestParam("numberUpdatedRecords") @Valid @Min(0) final Integer numberUpdatedRecords) {
        LOGGER.info("Start of the procedure for updating information from exif");
        int numberOfProcessedRecords = Math.min(numberUpdatedRecords, filesMassUpdate);
        Collection<MetadataEntity> metadataEntities = findMetadataEntities(numberOfProcessedRecords);
        LOGGER.info("Received records from the database: " + metadataEntities.size());

        while (!metadataEntities.isEmpty()) {
            for (MetadataEntity metadataEntity : metadataEntities) {
                updateFieldsInMetadata(metadataEntity);
            }
            LOGGER.info("Processed records from the database: " + numberOfProcessedRecords);
            if (numberOfProcessedRecords < numberUpdatedRecords) {
                // остаток - разница между сколько нужно обработать и сколько на текущий момент обработано
                int remainder = numberUpdatedRecords - numberOfProcessedRecords;
                int numberUpdatefiles = Math.min(remainder, filesMassUpdate);
                numberOfProcessedRecords += numberUpdatefiles;
                metadataEntities = metadataRepository.findMetadataEntityByExifIsNull(numberUpdatefiles);
            } else {
                LOGGER.info("Completed the procedure for updating information from exif. Processed records: " + numberOfProcessedRecords);
                return;
            }
        }
        LOGGER.info("Completed the procedure for updating information from exif");
    }

    private void updateFieldsInMetadata(MetadataEntity metadataEntity) {
        String metadataId = metadataEntity.getId().toString();
        try {
            File file = fileService.get(metadataId);
            // если поле info - пустое или не содержит значение rotate
            if (metadataEntity.getInfo() == null || !metadataEntity.getInfo().contains("rotate")) {
                // обновим поля info и exif в metadata
                updateFieldsMetadata(metadataId, file);
            } else {
                // если поле info - содержит значение rotate, обновим поле exif в metadata
                fileService.updateExif(metadataId, JSONObject.toJSONString(getImageExif(file)));
            }
        } catch (NotFoundException e) {
            LOGGER.error("Failed to update field exif in metadata for file id: " + metadataId, e);
        }
    }

    /**
     * Возвращает коллекцию MetadataEntity у которых exif не заполнен информацией
     * @param numberRecords Integer
     * @return Collection<MetadataEntity>
     */
    private Collection<MetadataEntity> findMetadataEntities(Integer numberRecords) {
        return metadataRepository.findMetadataEntityByExifIsNull(numberRecords);
    }

    @PutMapping(value = ROOT + "/zip")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_settler\", \"nsds_settler_manager\", \"nsds_admin\", \"nsds_viewer\", \"api_nsds_download\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"profitsoft\") || @springSecurityKeycloak.getAuthorizedParty().equalsIgnoreCase(\"esb\")")
    @ApiOperation(value = "Метод для получения контента группы файлов запакованых в формате zip")
    public ResponseEntity<StreamingResponseBody> download(@RequestBody @NotEmpty final HashSet<UUID> fileIds) {
        final Collection<File> files = fileService.getContentOfGroupOfFiles(fileIds);
        return ResponseEntity
                .status(OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.TRANSFER_ENCODING, "binary")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"compressed.zip\"")
                .body(outputStream -> zipFiles(files, outputStream));
    }

    private void validate(final MetadataDTO metadataDTO) {
        validateExtension(metadataDTO.getName());
        final Set<ConstraintViolation<MetadataDTO>> constraintViolations = validator.validate(metadataDTO);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    private void validate(final TempFileMetadataDTO tempFileMetadataDTO) {
        validateExtension(tempFileMetadataDTO.getName());
        final Set<ConstraintViolation<TempFileMetadataDTO>> constraintViolations = validator.validate(tempFileMetadataDTO);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    private void validateExtension(final String fileName) {
        final String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        if (restrictedExtensions.contains(extension)) {
            throw new BusinessException("file extension not supported");
        }
    }

    private String collectInputStreamAsString(final InputStream inputStream, final Charset charset) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private Source resolveSource() {
        final String authorizedParty = authorizedPartyProvider.getAuthorizedParty();
        if ("weblogin".equalsIgnoreCase(authorizedParty)) {
            return Source.WEB_INTERFACE;
        }
        if ("chatbot".equalsIgnoreCase(authorizedParty)) {
            return Source.CHAT_BOT;
        }
        if ("profitsoft".equalsIgnoreCase(authorizedParty)) {
            return Source.PROFITSOFT;
        }
        if ("esb".equalsIgnoreCase(authorizedParty)) {
            return Source.ESB;
        }
        if ("HIS1c".equalsIgnoreCase(authorizedParty)) {
            return Source._1C;
        }
        if ("citrus".equalsIgnoreCase(authorizedParty)) {
            return Source.CITRUS;
        }
        throw new UnprocessableEntityException("unknown source: " + authorizedParty);
    }

}