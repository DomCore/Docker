package com.universalna.nsds.service;

import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.model.*;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FileService {

    String create(InputStream content, Metadata metadata);

    String createTemporaryFile(InputStream content, Metadata metadata);

    String createSharedUri(String id);

    File get(String id);

    File getByShareKey(String shareKey);

    Collection<Metadata> getRelatedMetadata(Relation relation, String relationId);

    Metadata getMetadata(String id);

    Metadata getMetadataWithPreview(final String id);

    ModelWithLastModified<String> delete(String id);

    ModelWithLastModified<String> changeFileStatus(String id, Status status);

    ModelWithLastModified<String> updateName(String id, String name);

    ModelWithLastModified<String> updateDescription(String id, String description);

    ModelWithLastModified<Collection<FileTag>> updateTags(String id, Set<FileTag> tags);

    Collection<Metadata> getInactiveFilesMetadata(Relation relation, String relationId);

    UUID copyMetadata(UUID id, Relation copyToRelation, String copyToRelationId);

    UUID copyMetadataAsNew(final UUID id, final Relation copyToRelation, final String copyToRelationId, final Set<FileTagDTO> replacementTags);

    String createSharedUriToGroupOfFiles(Collection<UUID> fileIds);

    Collection<UUID> getGroupOfFiles(UUID key);

    File getContentByKeyAndFileId(final UUID key, final String fileId);

    Collection<File> getContentOfGroupOfFiles(UUID key, Set<UUID> fileIds);

    Collection<File> getContentOfGroupOfFiles(Set<UUID> fileIds);

    ModelWithLastModified<String> updateInfo(String id, String infoJson);

    void updateInfoAndExif(String id, String infoJson,  String exifJson);

    void updateExif(String id, String exifJson);

    String createUrlToEditableFile(UUID id);

    Set<String> getAllTagsFromAllEntities();

    Map<String, Set<String>> getAllTagsFromAllEntitiesGroupedWithRelations();

    Set<String> getAllTagsByRelation(Relation relation, String relationId);

    Collection<ModelWithLastModified<String>> archive(Relation relation, String relationId);

    Collection<ModelWithLastModified<String>> revertFromArchive(Relation relation, String relationId);

    void setTempFilesToPersistent(final Relation relation, final String relationId, Set<UUID> fileIds);

}
