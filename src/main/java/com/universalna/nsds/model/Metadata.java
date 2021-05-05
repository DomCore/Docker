package com.universalna.nsds.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class Metadata {

    private UUID id;

    private String name;

    private Long size;

    private Relation relation;

    private String relationId;

    private String documentType;

    private String documentId;

    private String description;

    private String uploaderId;

    private OffsetDateTime timestamp;

    private Source source;

    private Origin origin;

    private Status status;

    private Set<FileTag> tags;

    @JsonRawValue
    private String info;

    private String lastModifiedBy;

    private OffsetDateTime lastModifiedDate;

    /** EXIF информация картинки */
    @JsonRawValue
    private String exif;

}
