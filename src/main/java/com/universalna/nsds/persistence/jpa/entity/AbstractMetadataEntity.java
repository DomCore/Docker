package com.universalna.nsds.persistence.jpa.entity;

import com.universalna.nsds.model.Origin;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.model.Source;
import com.universalna.nsds.model.Status;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static javax.persistence.EnumType.STRING;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
})
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractMetadataEntity extends AbstractAuditableEntity {

    @Id
    @Type(type = "pg-uuid")
    @Column(name = "ID")
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SIZE")
    private Long size;

    @Column(name = "RELATION")
    @Enumerated(STRING)
    private Relation relation;

    @Column(name = "RELATION_ID")
    private String relationId;

    @Column(name = "DOCUMENT_TYPE")
    private String documentType;

    @Column(name = "DOCUMENT_ID")
    private String documentId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "UPLOADER_ID")
    private String uploaderId;

    @Column(name = "TIMESTAMP")
    private OffsetDateTime timestamp;

    @Column(name = "SOURCE")
    @Enumerated(STRING)
    private Source source;

    @Column(name = "ORIGIN")
    @Enumerated(STRING)
    private Origin origin;

    @Column(name = "STATUS")
    @Enumerated(STRING)
    private Status status;

    @Column(name = "FILE_STORAGE_FILE_ID")
    private String fileStorageFileId;

    @Column(name = "TAGS")
    @Type(type = "jsonb")
    private Set<FileTagEntity> tags = new HashSet<>();

    @Column(name = "INFO")
    @Type(type = "jsonb")
    private String info;

    @Column(name = "EXIF")
    @Type(type = "jsonb")
    private String exif;

    public AbstractMetadataEntity(final String createdBy, final OffsetDateTime createdDate, final String lastModifiedBy, final OffsetDateTime lastModifiedDate, final UUID id, final String name, final Long size, final Relation relation, final String relationId, final String documentType, final String documentId, final String description, final String uploaderId, final OffsetDateTime timestamp, final Source source, final Origin origin, final Status status, final String fileStorageFileId, final Set<FileTagEntity> tags, final String info, final String exif) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
        this.id = id;
        this.name = name;
        this.size = size;
        this.relation = relation;
        this.relationId = relationId;
        this.documentType = documentType;
        this.documentId = documentId;
        this.description = description;
        this.uploaderId = uploaderId;
        this.timestamp = timestamp;
        this.source = source;
        this.origin = origin;
        this.status = status;
        this.fileStorageFileId = fileStorageFileId;
        this.tags = tags;
        this.info = info;
        this.exif = exif;
    }
}
