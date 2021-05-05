package com.universalna.nsds.controller;

import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.controller.dto.MetadataDTO;
import com.universalna.nsds.controller.dto.MetadataWithUrlDTO;
import com.universalna.nsds.controller.dto.TempFileMetadataDTO;
import com.universalna.nsds.model.FileTag;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.Source;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@org.mapstruct.Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, implementationName = "controllerMapperImpl")
interface Mapper {

    @Mappings(value = {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "size", ignore = true),
            @Mapping(target = "source", source = "source"),
            @Mapping(target = "uploaderId", ignore = true),
            @Mapping(target = "timestamp", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "info", ignore = true),
            @Mapping(target = "lastModifiedBy", ignore = true),
            @Mapping(target = "lastModifiedDate", ignore = true),
            @Mapping(target = "exif", ignore = true)
    })
    Metadata toModel(MetadataDTO metadataDTO, Source source);

    @Mappings(value = {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "size", ignore = true),
            @Mapping(target = "source", source = "source"),
            @Mapping(target = "relation", constant = "TEMPORARY"),
            @Mapping(target = "relationId", constant = "TEMPORARY"),
            @Mapping(target = "uploaderId", ignore = true),
            @Mapping(target = "timestamp", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "info", ignore = true),
            @Mapping(target = "lastModifiedBy", ignore = true),
            @Mapping(target = "lastModifiedDate", ignore = true),
            @Mapping(target = "exif", ignore = true)
    })
    Metadata toModel(TempFileMetadataDTO tempFileMetadataDTO, Source source);

    @Mappings(value = {
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "size", ignore = true),
            @Mapping(target = "source", source = "source"),
            @Mapping(target = "uploaderId", ignore = true),
            @Mapping(target = "timestamp", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "info", ignore = true),
            @Mapping(target = "lastModifiedBy", ignore = true),
            @Mapping(target = "lastModifiedDate", ignore = true),
            @Mapping(target = "exif", ignore = true)
    })
    Metadata toModel(MetadataWithUrlDTO metadataDTO, Source source);

    FileTag toModel(FileTagDTO fileTagDTO);
}
