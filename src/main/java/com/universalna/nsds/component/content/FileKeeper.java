package com.universalna.nsds.component.content;

import com.microsoft.graph.http.GraphServiceException;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.exception.IoExceptionHandler;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.persistence.jpa.entity.OneDriveDocumentEntity;
import com.universalna.nsds.service.MetadataPersistenceService;
import com.universalna.nsds.service.OneDriveDocumentPersistenceService;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static com.universalna.nsds.Profiles.STREAMING;

@Component
@Profile(STREAMING)
public class FileKeeper implements IoExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger(FileKeeper.class);

    @Autowired
    private BlobClient azureBlobClient;

    @Autowired
    private DriveClient driveClient;

    @Autowired
    private OneDriveDocumentPersistenceService oneDriveDocumentPersistenceService;

    @Autowired
    private MetadataPersistenceService metadataPersistenceService;

    @Autowired
    private UUIDGenerator uuidGenerator;

    public String uploadFile(final InputStream content) {
        final String fileStorageFileId = uuidGenerator.generate().toString();
        azureBlobClient.putContent(fileStorageFileId, content);
        return fileStorageFileId;
    }

    public InputStream getContent(final MetadataEntity metadataEntity) {
        return oneDriveDocumentPersistenceService.findById(metadataEntity.getId())
                .map(OneDriveDocumentEntity::getOneDriveId)
                .map(id -> driveClient.download(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseGet(() -> azureBlobClient.getContent(metadataEntity.getFileStorageFileId()));
    }

    public Long getContentLengthFromBlob(final String fileStorageFileId) {
        return azureBlobClient.getContentLength(fileStorageFileId);
    }

    public String createUrlToEditableFile(final MetadataEntity metadataEntity) {
        return oneDriveDocumentPersistenceService.findById(metadataEntity.getId())
                .map(OneDriveDocumentEntity::getOneDriveId)
                .map(driveClient::createEditableUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseGet(() -> {
                    final OneDriveDocumentEntity newOneDriveDocumentEntity = uploadToOneDrive(metadataEntity.getId(), metadataEntity.getFileStorageFileId(), metadataEntity.getName());
                    oneDriveDocumentPersistenceService.save(newOneDriveDocumentEntity);
                    return driveClient.createEditableUrl(newOneDriveDocumentEntity.getOneDriveId()).orElseThrow();
                });
    }

    private OneDriveDocumentEntity uploadToOneDrive(final UUID metadataId, final String fileStorageFileId, final String name) {
        final String oneDriveFileId = driveClient.uploadToDrive(azureBlobClient.getContent(fileStorageFileId), name);
        return OneDriveDocumentEntity.builder().metadataId(metadataId).oneDriveId(oneDriveFileId).build();
    }

    @Scheduled(fixedDelay = 600000 //once per 10 min
//            cron = "0 0 0 * * *", zone = "GMT+2:00"
    )
    public void saveToBlobAndCleanDrive() {
        try {
            oneDriveDocumentPersistenceService.findAll().forEach(this::saveToBlobAndCleanDrive);
        } catch (Exception e) {
            LOGGER.error("Exception caught in scheduled task saveToBlobAndCleanDrive", e);
        }
    }

    public void saveToBlobAndCleanDrive(final MetadataEntity metadataEntity) {
        oneDriveDocumentPersistenceService.findById(metadataEntity.getId()).ifPresent(this::saveToBlobAndCleanDrive);
    }

    private void saveToBlobAndCleanDrive(final OneDriveDocumentEntity oneDriveDocumentEntity) {
        try {
            updateInBlob(oneDriveDocumentEntity);
            clean(oneDriveDocumentEntity);
        } catch (GraphServiceException e) {
            LOGGER.error("GraphServiceException caught while cleaning drive file, driveId: {}, metadataId: {}, throwable: {}", oneDriveDocumentEntity.getOneDriveId(), oneDriveDocumentEntity.getMetadataId(), e);
        }
    }

    private void updateInBlob(OneDriveDocumentEntity d) {
        metadataPersistenceService.findById(d.getMetadataId())
                .ifPresent(metadataEntity -> driveClient.download(d.getOneDriveId())
                        .ifPresentOrElse(driveInputStream -> {
                            final byte[] driveVersion = tryIoOperation(()-> IOUtils.toByteArray(driveInputStream));
                            final InputStream blobInputStream = azureBlobClient.getContent(metadataEntity.getFileStorageFileId());
                            if (tryIoOperation(() -> !IOUtils.contentEquals(blobInputStream, new ByteArrayInputStream(driveVersion)))) {
                                final String azureBlobFileId = uuidGenerator.generate().toString();
                                azureBlobClient.putContent(azureBlobFileId, new ByteArrayInputStream(driveVersion));
                                metadataEntity.setFileStorageFileId(azureBlobFileId);
                                metadataEntity.setSize((long) driveVersion.length);
                                metadataPersistenceService.save(metadataEntity);
                            }
                        }, () -> {}));
    }

    private void clean(OneDriveDocumentEntity d) {
        try {
            driveClient.delete(d.getOneDriveId());
            oneDriveDocumentPersistenceService.deleteById(d.getMetadataId());
        } catch (final GraphServiceException e) {
            // do nothing, retry in next cycle
        }
    }
}

