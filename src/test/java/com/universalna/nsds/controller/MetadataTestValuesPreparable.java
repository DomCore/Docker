package com.universalna.nsds.controller;

import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.controller.dto.MetadataDTO;
import com.universalna.nsds.model.FileTag;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.MetadataAudit;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.persistence.jpa.entity.FileTagEntity;
import com.universalna.nsds.persistence.jpa.entity.MetadataAuditEntity;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;

import java.util.Set;
import java.util.UUID;

import static com.universalna.nsds.MetadataTestConstants.*;
import static com.universalna.nsds.TestConstants.DEFAULT_DATE;
import static com.universalna.nsds.TestConstants.INTEGRATION_TEST_PRINCIPAL;
import static com.universalna.nsds.model.Origin.ATTACHED;
import static com.universalna.nsds.model.Source.WEB_INTERFACE;
import static com.universalna.nsds.model.Status.ACTIVE;
import static com.universalna.nsds.model.Status.INACTIVE;
import static org.hibernate.envers.RevisionType.ADD;

public interface MetadataTestValuesPreparable {

    default MetadataDTO.Builder prepareMetadataDto() {
        return MetadataDTO.builder()
                .name(FILE_NAME)
                .relation(Relation.INSURANCE_CASE)
                .relationId(RELATION_ID)
                .description(DESCRIPTION)
                .origin(ATTACHED)
                .tags(Set.of(new FileTagDTO("tag1"), new FileTagDTO("tag2")));
    }

    default Metadata.Builder prepareMetadata() {
        return Metadata.builder()
                .id(UUID.fromString(FILE_ID_STRING))
                .name(FILE_NAME)
                .size(FILE_SIZE)
                .relation(Relation.INSURANCE_CASE)
                .relationId(RELATION_ID)
                .description(DESCRIPTION)
                .uploaderId(INTEGRATION_TEST_PRINCIPAL)
                .timestamp(DEFAULT_DATE)
                .source(WEB_INTERFACE)
                .origin(ATTACHED)
                .status(ACTIVE)
                .tags(Set.of(new FileTag("tag1"), new FileTag("tag2")))
                .lastModifiedBy(INTEGRATION_TEST_PRINCIPAL)
                .lastModifiedDate(DEFAULT_DATE);
    }

    default MetadataEntity.MetadataEntityBuilder prepareMetadataEntity() {
        return MetadataEntity.metadataEntityBuilder()
                .id(UUID.fromString(FILE_ID_STRING))
                .name(FILE_NAME)
                .size(FILE_SIZE)
                .relation(Relation.INSURANCE_CASE)
                .relationId(RELATION_ID)
                .description(DESCRIPTION)
                .uploaderId(INTEGRATION_TEST_PRINCIPAL)
                .timestamp(DEFAULT_DATE)
                .source(WEB_INTERFACE)
                .origin(ATTACHED)
                .status(ACTIVE)
                .fileStorageFileId(FILE_ID_STRING)
                .tags(Set.of(new FileTagEntity("tag1"), new FileTagEntity("tag2")))
                .createdBy(INTEGRATION_TEST_PRINCIPAL)
                .createdDate(DEFAULT_DATE)
                .lastModifiedBy(INTEGRATION_TEST_PRINCIPAL)
                .lastModifiedDate(DEFAULT_DATE);
    }

    default MetadataAuditEntity.MetadataAuditEntityBuilder prepareMetadataAuditEntity() {
        return MetadataAuditEntity.MetadataAuditEntityBuilder()
                .id(UUID.fromString(FILE_ID_STRING))
                .name(FILE_NAME)
                .size(FILE_SIZE)
                .relation(Relation.INSURANCE_CASE)
                .relationId(RELATION_ID)
                .description(DESCRIPTION)
                .uploaderId(INTEGRATION_TEST_PRINCIPAL)
                .timestamp(DEFAULT_DATE)
                .source(WEB_INTERFACE)
                .origin(ATTACHED)
                .status(ACTIVE)
                .fileStorageFileId(FILE_ID_STRING)
                .tags(Set.of(new FileTagEntity("tag1"), new FileTagEntity("tag2")))
                .createdBy(INTEGRATION_TEST_PRINCIPAL)
                .createdDate(DEFAULT_DATE)
                .lastModifiedBy(INTEGRATION_TEST_PRINCIPAL)
                .lastModifiedDate(DEFAULT_DATE)
                .revision(null)
                .revisionEnd(null)
                .revisionEndTimestamp(null)
                .actionType(null);
    }


    default MetadataAudit.MetadataAuditBuilder prepareMetadataAuditDto() {
        return MetadataAudit.MetadataAuditDTOBuilder()
                .revision(null)
                .revisionEnd(null)
                .revisionEndTimestamp(DEFAULT_DATE)
                .actionType(ADD)
                .createdBy(INTEGRATION_TEST_PRINCIPAL)
                .createdDate(DEFAULT_DATE)
                .lastModifiedBy(INTEGRATION_TEST_PRINCIPAL)
                .lastModifiedDate(DEFAULT_DATE)
                .id(UUID.fromString(FILE_ID_STRING))
                .name(FILE_NAME)
                .size(FILE_SIZE)
                .relation(Relation.INSURANCE_CASE)
                .relationId(RELATION_ID)
                .description(DESCRIPTION)
                .uploaderId(INTEGRATION_TEST_PRINCIPAL)
                .timestamp(DEFAULT_DATE)
                .source(WEB_INTERFACE)
                .origin(ATTACHED)
                .status(INACTIVE);
    }
}
