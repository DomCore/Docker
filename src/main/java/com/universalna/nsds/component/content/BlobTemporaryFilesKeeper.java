package com.universalna.nsds.component.content;

import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.service.MetadataPersistenceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.universalna.nsds.model.Status.TEMPORARY;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.HOURS;

@Component
@Transactional
public class BlobTemporaryFilesKeeper {

    private static final Logger LOGGER = LogManager.getLogger(FileKeeper.class);

    private static final long ONCE_PER_HOUR = 1000 * 60 * 60;

    @Autowired
    private BlobClient blobClient;

    @Autowired
    private MetadataPersistenceService metadataPersistenceService;

    @Scheduled(fixedDelay = ONCE_PER_HOUR)
    public void cleanBlobOfExpiredTemporaryFiles() {
        try {
            final MetadataEntity entity = MetadataEntity.metadataEntityBuilder().status(TEMPORARY).build();
            final Example<MetadataEntity> example = Example.of(entity);
            metadataPersistenceService.findAll(example).stream()
                    .filter(e -> now().isAfter(e.getTimestamp().plus(24, HOURS)))
                    .forEach(e -> {
                        blobClient.deleteContent(e.getFileStorageFileId());
                        metadataPersistenceService.deleteById(e.getId());
                    });
        } catch (Exception e) {
            LOGGER.error("Exception caught in scheduled task cleanBlobOfExpiredTemporaryFiles", e);
        }
    }
}
