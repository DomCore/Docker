package com.universalna.nsds.persistence.jpa.entity;

import com.universalna.nsds.model.Origin;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.model.Source;
import com.universalna.nsds.model.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "METADATA")
@Audited(auditParents = {AbstractMetadataEntity.class, AbstractAuditableEntity.class})
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetadataEntity extends AbstractMetadataEntity {

    @lombok.Builder(builderMethodName = "metadataEntityBuilder")
    public MetadataEntity(final String createdBy, final OffsetDateTime createdDate, final String lastModifiedBy, final OffsetDateTime lastModifiedDate, final UUID id, final String name, final Long size, final Relation relation, final String relationId, final String documentType, final String documentId, final String description, final String uploaderId, final OffsetDateTime timestamp, final Source source, final Origin origin, final Status status, final String fileStorageFileId, final Set<FileTagEntity> tags, final String info, final String exif) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, id, name, size, relation, relationId, documentType, documentId, description, uploaderId, timestamp, source, origin, status, fileStorageFileId, tags, info, exif);
    }
}
