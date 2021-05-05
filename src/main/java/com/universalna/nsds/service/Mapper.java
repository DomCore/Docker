package com.universalna.nsds.service;

import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.MetadataAudit;
import com.universalna.nsds.model.Status;
import com.universalna.nsds.persistence.jpa.entity.MetadataAuditEntity;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT;

@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, nullValueMappingStrategy = RETURN_DEFAULT, implementationName = "persistenceMapperImpl")
public interface Mapper {

    Metadata toModel(MetadataEntity metadata);

    MetadataAudit toModel(MetadataAuditEntity metadata);

    @Mappings(value = {
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "size", source = "size"),
            @Mapping(target = "uploaderId", source = "uploaderId"),
            @Mapping(target = "timestamp", source = "timestamp"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "fileStorageFileId", source = "fileStorageFileId"),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "lastModifiedBy", ignore = true),
            @Mapping(target = "lastModifiedDate", ignore = true)
    })
    MetadataEntity toEntity(Metadata metadata,
                            UUID id,
                            Long size,
                            String uploaderId,
                            OffsetDateTime timestamp,
                            Status status,
                            String fileStorageFileId);

    MetadataEntity toEntity(MetadataAuditEntity auditEntity);

}
