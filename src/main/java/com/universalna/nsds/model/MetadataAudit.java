package com.universalna.nsds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.hibernate.envers.RevisionType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetadataAudit extends Metadata implements Comparable<MetadataAudit> {

    private Long revision;

    private Long revisionEnd;

    private OffsetDateTime revisionEndTimestamp;

    private RevisionType actionType;

    private String createdBy;

    private OffsetDateTime createdDate;

    @lombok.Builder(builderMethodName = "MetadataAuditDTOBuilder")
    public MetadataAudit(final UUID id, final String name, final Long size, final Relation relation, final String relationId, final String documentType, final String documentId, final String description, final String uploaderId, final OffsetDateTime timestamp, final Source source, final Origin origin, final Status status, final Set<FileTag> tags, final String info, final String exif, final String lastModifiedBy, final OffsetDateTime lastModifiedDate, final Long revision, final Long revisionEnd, final OffsetDateTime revisionEndTimestamp, final RevisionType actionType, final String createdBy, final OffsetDateTime createdDate) {
        super(id, name, size, relation, relationId, documentType, documentId, description, uploaderId, timestamp, source, origin, status, tags, info, lastModifiedBy, lastModifiedDate, exif);
        this.revision = revision;
        this.revisionEnd = revisionEnd;
        this.revisionEndTimestamp = revisionEndTimestamp;
        this.actionType = actionType;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
    }

    @Override
    public int compareTo(@NonNull final MetadataAudit that) {
        return (int) (this.revision - that.revision);
    }
}
