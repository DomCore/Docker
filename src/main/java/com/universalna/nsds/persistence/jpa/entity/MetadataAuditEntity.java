package com.universalna.nsds.persistence.jpa.entity;

import com.universalna.nsds.model.Origin;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.model.Source;
import com.universalna.nsds.model.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.RevisionType;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;


//TODO: redesign this class for ENVERS compatibility with any entity
@Entity
@IdClass(MetadataAuditPK.class)
@Table(name = "METADATA_AUDIT")
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetadataAuditEntity extends AbstractMetadataEntity {

//    TODO: try to merge this ID with MetadataAuditPK
    @Id
    private Long revision;

    @Column(name = "AUDIT_REVISION_END")
    private Long revisionEnd;

    @Column(name = "AUDIT_REVISION_END_TS")
    private OffsetDateTime revisionEndTimestamp;

    @Column(name = "ACTION_TYPE")
    private RevisionType actionType;

    @lombok.Builder(builderMethodName = "MetadataAuditEntityBuilder")
    public MetadataAuditEntity(final String createdBy, final OffsetDateTime createdDate, final String lastModifiedBy, final OffsetDateTime lastModifiedDate, final UUID id, final String name, final Long size, final Relation relation, final String relationId, final String documentType, final String documentId, final String description, final String uploaderId, final OffsetDateTime timestamp, final Source source, final Origin origin, final Status status, final String fileStorageFileId, final Set<FileTagEntity> tags, final String info, final String exif, final Long revision, final Long revisionEnd, final OffsetDateTime revisionEndTimestamp, final RevisionType actionType) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, id, name, size, relation, relationId, documentType, documentId, description, uploaderId, timestamp, source, origin, status, fileStorageFileId, tags, info, exif);
        this.revision = revision;
        this.revisionEnd = revisionEnd;
        this.revisionEndTimestamp = revisionEndTimestamp;
        this.actionType = actionType;
    }
}
