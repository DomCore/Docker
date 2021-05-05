package com.universalna.nsds.persistence.jpa;

import com.universalna.nsds.model.Relation;
import com.universalna.nsds.model.Status;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

public interface MetadataRepository extends JpaRepository<MetadataEntity, UUID> {

    Collection<MetadataEntity> findAllByRelationAndRelationId(@Param("relation") Relation relation,
                                                              @Param("relationId") String relationId);

    boolean existsByIdAndStatus(UUID id, Status status);

    @Query(value = "SELECT id,name,size,relation,relation_id,document_type,document_id,description,uploader_id,timestamp,source,origin,status,file_storage_file_id,tags,created_date,created_by,last_modified_date,last_modified_by,info" +
            " FROM metadata" +
            " WHERE metadata.timestamp > :after AND metadata.timestamp < :before", nativeQuery = true)
    Collection<MetadataEntity> findAllByTimestampAfterAndTimestampBefore(@Param("after") OffsetDateTime after,@Param("before") OffsetDateTime before);

    /** Возвращает коллекцию MetadataEntity у которых exif не заполнен информацией, name равен одному из расширений (.jpg, .jpeg, .png),
     * limit задается пользователем */
    @Query(value = "SELECT * FROM metadata where metadata.exif is null and metadata.name similar to '%(.jpg|.jpeg|.png)' ORDER BY timestamp asc limit :limit", nativeQuery = true)
    Collection<MetadataEntity> findMetadataEntityByExifIsNull(@Param("limit") Integer limit);

}
