package com.universalna.nsds.service.postgres;

import com.universalna.nsds.component.PrincipalProvider;
import com.universalna.nsds.component.SecurityUtil;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.component.content.FileKeeper;
import com.universalna.nsds.config.ApplicationConfigurationProperties;
import com.universalna.nsds.controller.SSEController;
import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.exception.IoExceptionHandler;
import com.universalna.nsds.exception.NotFoundException;
import com.universalna.nsds.exception.UnprocessableEntityException;
import com.universalna.nsds.model.*;
import com.universalna.nsds.persistence.jpa.entity.*;
import com.universalna.nsds.service.*;
import com.universalna.nsds.service.search.profitsoft.ClaimInfoDto;
import com.universalna.nsds.service.search.profitsoft.ProfitsoftSearchService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.universalna.nsds.config.MetricsConfig.UPLOADED_FILES_TOTAL;
import static com.universalna.nsds.model.Relation.INSURANCE_CASE;
import static com.universalna.nsds.model.Relation.SETTLEMENT_NOTIFICATION;
import static com.universalna.nsds.model.Source.CHAT_BOT;
import static com.universalna.nsds.model.Status.*;
import static java.util.stream.Collectors.toList;

@Service
public class PostgresMetaFileService implements FileService, IoExceptionHandler {

    @Autowired
    private FileKeeper fileKeeper;

    @Autowired
    private MetadataPersistenceService metadataPersistenceService;

    @Autowired
    private FileSharePersistenceService fileSharePersistenceService;

    @Autowired
    private GroupFileSharePersistenceService groupFileSharePersistenceService;

    @Autowired
    private Mapper mapper;

    @Autowired
    private PrincipalProvider principalProvider;

    @Autowired
    private ProfitsoftSearchService profitsoftSearchService;

    @Autowired
    private UUIDGenerator uuidGenerator;

    @Autowired
    private SSEController sseController;

    @Autowired
    private ApplicationConfigurationProperties applicationConfigurationProperties;

    @Autowired
    private SecurityUtil securityUtil;

    @Value("${application.fileshare.ttl}")
    private int sharedFileGroupTtl;

    @Override
    public String create(final InputStream content, final Metadata metadata) {
        final MetadataEntity savedMetadata = uploadFile(content, metadata, metadata.getSource() == CHAT_BOT ? INACTIVE : ACTIVE);
        copyToInsuranceCaseIfExists(savedMetadata);
        UPLOADED_FILES_TOTAL.increment();
        notifyCurrentUsers(savedMetadata);
        return savedMetadata.getId().toString();
    }

    @Override
    public String createTemporaryFile(final InputStream content, final Metadata metadata) {
        return uploadFile(content, metadata, TEMPORARY).getId().toString();
    }

    private MetadataEntity uploadFile(final InputStream content, final Metadata metadata, final Status status) {
        final UUID id = uuidGenerator.generate();
        final String fileStorageFileId = fileKeeper.uploadFile(content);
        final MetadataEntity metadataToCreate = mapper.toEntity(metadata,
                id,
                0L,
                principalProvider.getPrincipal(),
                OffsetDateTime.now(),
                status,
                fileStorageFileId
        );
        final Long contentLength = fileKeeper.getContentLengthFromBlob(fileStorageFileId);
        metadataToCreate.setSize(contentLength);
        return metadataPersistenceService.save(metadataToCreate);
    }

    @Override
    public String createSharedUri(final String metadataId) {
        final UUID id = UUID.fromString(metadataId);
        if (metadataPersistenceService.metadataExistsByIdAndStatus(id, ACTIVE)) {
            final FileShareEntity existingEntity = fileSharePersistenceService.findById(id)
                    .orElseGet(() -> fileSharePersistenceService.save(FileShareEntity.builder()
                                                                                      .metadataId(id)
                                                                                      .key(uuidGenerator.generate())
                                                                                      .info(new Info())
                                                                                      .build()));
            return existingEntity.getKey().toString();
        }
        throw new NotFoundException("File not found");
    }

    @Override
    public String createSharedUriToGroupOfFiles(final Collection<UUID> fileIds) {
        final Set<UUID> existingFiles = fileIds.parallelStream().filter(id -> metadataPersistenceService.metadataExistsById(id)).collect(Collectors.toSet());
        final OffsetDateTime now = OffsetDateTime.now();
        final GroupFileShareEntityInfo info = GroupFileShareEntityInfo.builder()
                .createdBy(principalProvider.getPrincipal())
                .createdAt(now)
                .expireAt(now.plus(sharedFileGroupTtl, ChronoUnit.MONTHS))
                .build();

        final GroupFileShareEntity groupFileShareEntity = GroupFileShareEntity.builder()
                .id(uuidGenerator.generate())
                .metadataIds(existingFiles)
                .key(uuidGenerator.generate())
                .info(info)
                .build();

        return groupFileSharePersistenceService.save(groupFileShareEntity).getKey().toString();
    }

    @Override
    public Collection<File> getContentOfGroupOfFiles(final UUID key, final Set<UUID> requestedFileIds) {
        final Collection<UUID> allFileIdsOfGroup = getGroupOfFiles(key);
        final Set<UUID> filesToReturn = allFileIdsOfGroup.stream().filter(requestedFileIds::contains).collect(Collectors.toSet());
        return getFilesInParallel(filesToReturn);
    }

    @Override
    public Collection<File> getContentOfGroupOfFiles(final Set<UUID> fileIds) {
        return getFilesInParallel(fileIds);
    }

    @Override
    public Collection<UUID> getGroupOfFiles(final UUID key) {
        final GroupFileShareEntity groupFileShareEntity = groupFileSharePersistenceService.groupFileShareFindByKey(key)
                .filter(e -> OffsetDateTime.now().isBefore(e.getInfo().getExpireAt()))
                .orElseThrow(() -> new NotFoundException("Record not found"));
        return groupFileShareEntity.getMetadataIds();
    }

    @Override
    public File getContentByKeyAndFileId(final UUID key, final String fileId) {
        return getGroupOfFiles(key).stream().anyMatch(e -> e.equals(UUID.fromString(fileId))) ? get(fileId) : null;
    }

    @Override
    public File get(final String id) {
        final MetadataEntity metadata = findOneById(id);
        return File.builder()
                .content(fileKeeper.getContent(metadata))
                .originalName(metadata.getName())
                .build();
    }

    @Override
    public File getByShareKey(final String shareKey) {
        final UUID key = UUID.fromString(shareKey);
        final FileShareEntity existingFileShareEntity = fileSharePersistenceService.findByKey(key)
                .orElseThrow(() -> new NotFoundException("File not found"));
        final MetadataEntity metadata = findOneById(existingFileShareEntity.getMetadataId());
        return File.builder()
                .content(fileKeeper.getContent(metadata))
                .originalName(metadata.getName())
                .build();
    }

    @Override
    public Collection<Metadata> getInactiveFilesMetadata(final Relation relation, final String relationId) {
        return metadataPersistenceService.findAllByRelationAndRelationId(relation, relationId).stream()
                .filter(e -> e.getStatus() == INACTIVE)
                .map(mapper::toModel)
                .collect(toList());
    }

    @Override
    public Collection<Metadata> getRelatedMetadata(final Relation relation, final String relationId) {
        final Collection<MetadataEntity> entities = metadataPersistenceService.findAllByRelationAndRelationId(relation, relationId).stream().filter(this::byStatusNotTemporary).collect(toList());
        return filterAndMap(entities);
    }

    @Override
    public Metadata getMetadata(final String id) {
        final MetadataEntity entity = metadataPersistenceService.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("File not found"));
        return mapper.toModel(entity);
    }

    @Override
    public Metadata getMetadataWithPreview(final String id) {
        final MetadataEntity entity = metadataPersistenceService.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("File not found"));

        return mapper.toModel(entity);
    }

    @Override
    public ModelWithLastModified<String> delete(final String id) {
        boolean hasAccess = securityUtil.hasAccess("nsds_admin");
        return metadataPersistenceService.findById(UUID.fromString(id))
                .map(metadataEntity -> {
                    if (metadataEntity.getStatus() == ARCHIVED && !hasAccess) {
                        throw new UnprocessableEntityException("Can't delete archived files");
                    } else {
                        metadataPersistenceService.deleteById(metadataEntity.getId());
                        metadataEntity.setLastModifiedDate(null);
                        notifyCurrentUsers(metadataEntity);
                        return ModelWithLastModified.<String>builder().body(id).lastModifiedBy(metadataEntity.getLastModifiedBy()).lastModifiedDate(metadataEntity.getLastModifiedDate()).build();
                    }
                })
                .orElseThrow(() -> new NotFoundException("File not found"));
    }

    @Override
    public ModelWithLastModified<String> changeFileStatus(final String id, final Status status) {
        final MetadataEntity metadataEntity = metadataPersistenceService.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("File not found"));
        if (metadataEntity.getStatus() == ARCHIVED) {
            throw new UnprocessableEntityException("Can't change status of archived files");
        }
        metadataEntity.setStatus(status);
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(metadataEntity);
        notifyCurrentUsers(savedMetadataEntity);
        return ModelWithLastModified.<String>builder().body(id).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
    }

    @Override
    public ModelWithLastModified<String> updateName(final String id, final String name) {
        final MetadataEntity metadataEntity = findOneById(id);
        boolean hasAccess = securityUtil.hasAccess("nsds_settler_manager", "nsds_admin");
        if (metadataEntity.getStatus() == ARCHIVED && !hasAccess) {
            throw new AccessDeniedException("Can't change name of archived files");
        }
        final String originalExtension = FilenameUtils.getExtension(metadataEntity.getName());
        final String newNameWithExtension = name + "." + originalExtension;
        metadataEntity.setName(newNameWithExtension);
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(metadataEntity);
        notifyCurrentUsers(savedMetadataEntity);
        return ModelWithLastModified.<String>builder().body(id).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
    }

    @Override
    public ModelWithLastModified<String> updateDescription(final String id, final String description) {
        final MetadataEntity metadataEntity = findOneById(id);
        boolean hasAccess = securityUtil.hasAccess("nsds_settler_manager", "nsds_admin");
        if (metadataEntity.getStatus() == ARCHIVED && !hasAccess) {
            throw new AccessDeniedException("Can't change description of archived files");
        }
        metadataEntity.setDescription(description);
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(metadataEntity);
        notifyCurrentUsers(savedMetadataEntity);
        return ModelWithLastModified.<String>builder().body(id).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
    }

    @Override
    public ModelWithLastModified<Collection<FileTag>> updateTags(final String id, final Set<FileTag> tags) {
        final MetadataEntity metadataEntity = findOneById(id);
        final Set<FileTagEntity> tagsToUpdate = tags.stream().map(t -> new FileTagEntity(t.getTag())).collect(Collectors.toSet());
        if (metadataEntity.getStatus() == ARCHIVED) {
            if (!tagsToUpdate.containsAll(metadataEntity.getTags())) {
                throw new UnprocessableEntityException("Can't delete tags from archived files");
            }
        }
        metadataEntity.setTags(tagsToUpdate);
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(metadataEntity);
        final Set<FileTagEntity> savedTags = tagsToUpdate;
        notifyCurrentUsers(savedMetadataEntity);
        final Collection<FileTag> savedTagsModel = savedTags.stream().map(e -> new FileTag(e.getTag())).collect(toList());
        return ModelWithLastModified.<Collection<FileTag>>builder().body(savedTagsModel).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
    }

    @Override
    public UUID copyMetadata(final UUID id, final Relation copyToRelation, final String copyToRelationId) {
        final MetadataEntity existingMetadataEntity = metadataPersistenceService.findById(id).filter(e -> TEMPORARY != e.getStatus()).orElseThrow(() -> new NotFoundException("File not found"));
        final MetadataEntity cloneMetadataEntity = copyMetadataEntity(existingMetadataEntity, copyToRelation, copyToRelationId);
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(cloneMetadataEntity);
        notifyCurrentUsers(savedMetadataEntity);
        return savedMetadataEntity.getId();
    }

    @Override
    public UUID copyMetadataAsNew(final UUID id, final Relation copyToRelation, final String copyToRelationId, final Set<FileTagDTO> replacementTags) {
        final MetadataEntity existingMetadataEntity = metadataPersistenceService.findById(id).filter(e -> TEMPORARY != e.getStatus()).orElseThrow(() -> new NotFoundException("File not found"));
        final MetadataEntity cloneMetadataEntity = copyMetadataEntity(existingMetadataEntity, copyToRelation, copyToRelationId);

        final OffsetDateTime now = OffsetDateTime.now();
        cloneMetadataEntity.setTags(replacementTags.stream().map(t -> new FileTagEntity(t.getTag())).collect(Collectors.toSet()));
        cloneMetadataEntity.setCreatedDate(now);
        cloneMetadataEntity.setTimestamp(existingMetadataEntity.getTimestamp());
        cloneMetadataEntity.setLastModifiedBy(null);
        cloneMetadataEntity.setLastModifiedDate(null);
        if (cloneMetadataEntity.getStatus() == ARCHIVED) {
            cloneMetadataEntity.setStatus(ACTIVE);
        }
        if (copyToRelation == SETTLEMENT_NOTIFICATION) {
            copyToInsuranceCaseIfExists(cloneMetadataEntity);
        }
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(cloneMetadataEntity);
        notifyCurrentUsers(savedMetadataEntity);
        return savedMetadataEntity.getId();

    }

    @Override
    public ModelWithLastModified<String> updateInfo(final String id, final String infoJson) {
        final MetadataEntity existingMetadataEntity = metadataPersistenceService.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("File not found"));
        existingMetadataEntity.setInfo(infoJson);
        final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(existingMetadataEntity);
        notifyCurrentUsers(savedMetadataEntity);
        return ModelWithLastModified.<String>builder().body(savedMetadataEntity.getInfo()).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
    }

    /**
     * Обновляет поля Info и Exif в таблице Metadata
     */
    @Override
    public void updateInfoAndExif(final String id, final String infoJson, final String exifJson) {
        final MetadataEntity existingMetadataEntity = metadataPersistenceService.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("File not found"));
        existingMetadataEntity.setInfo(infoJson);
        existingMetadataEntity.setExif(exifJson);
        metadataPersistenceService.save(existingMetadataEntity);
    }

    /**
     * Обновить поле Exif в таблице Metadata
     */
    @Override
    public void updateExif(final String id, final String exifJson) {
        final MetadataEntity existingMetadataEntity = metadataPersistenceService.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("File not found"));
        existingMetadataEntity.setExif(exifJson);
        metadataPersistenceService.save(existingMetadataEntity);
    }

    @Override
    public String createUrlToEditableFile(final UUID id) {
        final MetadataEntity metadata = findOneById(id);
        if (metadata.getStatus() == ARCHIVED) {
            throw new UnprocessableEntityException("Archived files is not editable");
        }
        final boolean editableDocument = applicationConfigurationProperties.getEditable().contains(FilenameUtils.getExtension(metadata.getName()).toLowerCase());
        if (editableDocument) {
            return fileKeeper.createUrlToEditableFile(metadata);
        }
        throw new UnprocessableEntityException("file not editable");
    }

    @Override
    public Set<String> getAllTagsFromAllEntities() {
        return metadataPersistenceService.findAll().stream().flatMap(e -> e.getTags().stream()).map(FileTagEntity::getTag).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Map<String, Set<String>> getAllTagsFromAllEntitiesGroupedWithRelations() {
        final List<MetadataEntity> all = metadataPersistenceService.findAll();
        Map<String, Set<String>> tagsWithRelations = new HashMap<>();
        final Set<String> allUniqueTags = all.stream().flatMap(e -> e.getTags().stream().map(FileTagEntity::getTag)).collect(Collectors.toSet());
        allUniqueTags.forEach(t -> tagsWithRelations.put(t, new HashSet<String>()));
        all.forEach(m -> m.getTags().stream().map(FileTagEntity::getTag).forEach(t -> {
            final String entityNumberInProfitsoft = resolveNumberById(m);
            tagsWithRelations.get(t).add(m.getRelation() + ":" + entityNumberInProfitsoft);
        }));
        return tagsWithRelations;
    }

    private String resolveNumberById(final MetadataEntity metadataEntity) {
        if (metadataEntity.getRelation() == INSURANCE_CASE) {
            return profitsoftSearchService.getBySettlementCaseId(Long.valueOf(metadataEntity.getRelationId())).getSettlementCaseNumber();
        }
        if (metadataEntity.getRelation() == SETTLEMENT_NOTIFICATION) {
            return profitsoftSearchService.getByNoticeId(Long.valueOf(metadataEntity.getRelationId())).getNoticeNumber();
        } else {
            return metadataEntity.getRelation() + " " + "ID:" + metadataEntity.getRelationId();
        }
    }

    @Override
    public Set<String> getAllTagsByRelation(final Relation relation, final String relationId) {
        return metadataPersistenceService.findAllByRelationAndRelationId(relation, relationId).stream().flatMap(m -> m.getTags().stream().map(FileTagEntity::getTag)).collect(Collectors.toSet());
    }

    //    TODO: убрать metadataPersistenceService.findById(m.getId()) из-за изменяемости данных после fileKeeper.saveToBlobAndCleanDrive(m);
    @Override
    public Collection<ModelWithLastModified<String>> archive(final Relation relation, final String relationId) {
        final Collection<MetadataEntity> allByRelationAndRelationId = metadataPersistenceService.findAllByRelationAndRelationId(relation, relationId);
        final boolean inactiveFilesPresent = allByRelationAndRelationId.stream().anyMatch(m -> INACTIVE == m.getStatus());
        if (inactiveFilesPresent) {
            throw new UnprocessableEntityException("Cannot be archived, inactive files present");
        } else {
            return allByRelationAndRelationId.stream().map(m -> {
                fileKeeper.saveToBlobAndCleanDrive(m);
                final MetadataEntity metadataEntity = metadataPersistenceService.findById(m.getId()).orElseThrow(() -> new NotFoundException("File not found"));
                metadataEntity.setStatus(ARCHIVED);
                final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(metadataEntity);
                notifyCurrentUsers(savedMetadataEntity);
                return ModelWithLastModified.<String>builder().body(savedMetadataEntity.getInfo()).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
            }).collect(toList());
        }
    }

    @Override
    public Collection<ModelWithLastModified<String>> revertFromArchive(final Relation relation, final String relationId) {
        final List<MetadataEntity> archivedEntries = metadataPersistenceService.findAllByRelationAndRelationId(relation, relationId).stream()
                .filter(m -> m.getStatus() == ARCHIVED)
                .collect(toList());
        archivedEntries.forEach(m -> m.setStatus(ACTIVE));
        final Collection<MetadataEntity> savedMetadataEntities = metadataPersistenceService.saveAll(archivedEntries);
        return savedMetadataEntities.stream().map(savedMetadataEntity -> {
            notifyCurrentUsers(savedMetadataEntity);
            return ModelWithLastModified.<String>builder().body(savedMetadataEntity.getInfo()).lastModifiedBy(savedMetadataEntity.getLastModifiedBy()).lastModifiedDate(savedMetadataEntity.getLastModifiedDate()).build();
        })
                .collect(toList());
    }

    @Override
    public void setTempFilesToPersistent(final Relation relation, final String relationId, final Set<UUID> fileIds) {
        final List<MetadataEntity> pendingEntities = metadataPersistenceService.findAllById(fileIds);
        pendingEntities.forEach(e -> {
            e.setRelation(relation);
            e.setRelationId(relationId);
            e.setStatus(ACTIVE);
        });
        final Collection<MetadataEntity> savedEntities = metadataPersistenceService.saveAll(pendingEntities);
        savedEntities.forEach(this::notifyCurrentUsers);
        UPLOADED_FILES_TOTAL.increment(pendingEntities.size());
    }

    private void copyToInsuranceCaseIfExists(final MetadataEntity metadataEntity) {
        if (metadataEntity.getRelation() == SETTLEMENT_NOTIFICATION) {
            final ClaimInfoDto searchResult = profitsoftSearchService.getByNoticeId(Long.valueOf(metadataEntity.getRelationId()));
            final Long settlementCaseId = searchResult.getSettlementCaseId();
            if (settlementCaseId != null) {
                final MetadataEntity entityToSave = copyMetadataEntity(metadataEntity, INSURANCE_CASE, String.valueOf(settlementCaseId));
                final MetadataEntity savedMetadataEntity = metadataPersistenceService.save(entityToSave);
                notifyCurrentUsers(savedMetadataEntity);
            }
        }
    }

    private MetadataEntity copyMetadataEntity(final MetadataEntity source, final Relation copyToRelation, final String copyToRelationId) {
        return MetadataEntity.metadataEntityBuilder()
                .id(uuidGenerator.generate())
                .name(source.getName())
                .size(source.getSize())
                .relation(copyToRelation)
                .relationId(copyToRelationId)
                .description(source.getDescription())
                .uploaderId(source.getUploaderId())
                .timestamp(source.getTimestamp())
                .source(source.getSource())
                .origin(source.getOrigin())
                .status(source.getStatus())
                .fileStorageFileId(source.getFileStorageFileId())
                .tags(source.getTags())
                .info(source.getInfo())
                .build();
    }

    private Collection<File> getFilesInParallel(final Collection<UUID> ids) {
        return metadataPersistenceService.findAllById(ids)
                .parallelStream()
                .map(metadata -> File.builder()
                        .content(fileKeeper.getContent(metadata))
                        .originalName(metadata.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private MetadataEntity findOneById(final String id) {
        return findOneById(UUID.fromString(id));
    }

    private MetadataEntity findOneById(final UUID id) {
        return metadataPersistenceService.findById(id).orElseThrow(() -> new NotFoundException("File not found"));
    }

    private Collection<Metadata> filterAndMap(final Collection<MetadataEntity> collection) {
        return collection.stream()
//                .filter(this::byStatusActive)
                .map(mapper::toModel)
                .collect(toList());
    }

    private boolean byStatusNotTemporary(final MetadataEntity entity) {
        return entity.getStatus() != TEMPORARY;
    }

    private void notifyCurrentUsers(final MetadataEntity metadataEntity) {
        sseController.send(Notification.builder()
                .insuranceCaseId(metadataEntity.getRelation() == INSURANCE_CASE ? metadataEntity.getRelationId() : null)
                .noticeId(metadataEntity.getRelation() == SETTLEMENT_NOTIFICATION ? metadataEntity.getRelationId() : null)
                .fileId(metadataEntity.getId())
                .lastModifiedBy(metadataEntity.getLastModifiedBy())
                .lastModifiedDate(metadataEntity.getLastModifiedDate())
                .build());
    }
}
