package com.universalna.nsds.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.universalna.nsds.model.MetadataAudit;
import com.universalna.nsds.persistence.RevinfoEntity;
import com.universalna.nsds.persistence.RevinfoRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataAuditEntity;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.universalna.nsds.MetadataTestConstants.FILE_ID_STRING;
import static com.universalna.nsds.TestConstants.DEFAULT_DATE;
import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hibernate.envers.RevisionType.ADD;
import static org.hibernate.envers.RevisionType.DEL;
import static org.hibernate.envers.RevisionType.MOD;

@Disabled
class FileAuditControllerIT extends AbstractIT implements MetadataTestValuesPreparable {

    private static final String ROOT = "/audit/files";
    private static final String ID = "/{fileId}";

    @Autowired
    private RevinfoRepository revinfoRepository;

    @BeforeEach
    void tearDown() {
        metadataAuditRepository.deleteAllInBatch();
        metadataRepository.deleteAllInBatch();
        repositories.forEach(CrudRepository::deleteAll);
    }

    @Test
    void shouldReturnMetadataAuditRecords() throws Exception {
        final Collection<MetadataAudit> expectedValue = Arrays.asList(prepareMetadataAuditDto().revision(-4L)
                                                                                                  .revisionEnd(-3L)
                                                                                                  .revisionEndTimestamp(DEFAULT_DATE)
                                                                                                  .actionType(ADD).build(),
                                                                         prepareMetadataAuditDto().description("firstUpdate")
                                                                                                  .revision(-3L)
                                                                                                  .revisionEnd(-2L)
                                                                                                  .revisionEndTimestamp(DEFAULT_DATE)
                                                                                                  .actionType(MOD).build(),
                                                                         prepareMetadataAuditDto().description("secondUpdate")
                                                                                                  .revision(-2L)
                                                                                                  .revisionEnd(-1L)
                                                                                                  .revisionEndTimestamp(DEFAULT_DATE)
                                                                                                  .actionType(MOD).build(),
                                                                         prepareMetadataAuditDto().description("delete")
                                                                                                  .revision(-1L)
                                                                                                  .revisionEnd(null)
                                                                                                  .revisionEndTimestamp(null)
                                                                                                  .actionType(DEL).build());
        final String response =
        given()
                .port(port)
        .when()
                .post(ROOT + ID, FILE_ID_STRING)
        .then()
                .statusCode(200)
        .extract()
                .response().asString();

        final List<MetadataAudit> actualValue = objectMapper.readValue(response, new TypeReference<List<MetadataAudit>>() {});

        assertThat(actualValue, is(expectedValue));
    }

    @Test
    void shouldRevertMetadataFromAudit() {
        Stream.of(new RevinfoEntity(-1L, 1L),
                  new RevinfoEntity(-3L, 2L),
                  new RevinfoEntity(-2L, 3L),
                  new RevinfoEntity(-1L, 4L)
        ).forEach(revinfoRepository::save);

        final List<MetadataAuditEntity> initialMetadataAuditTableState = Arrays.asList(prepareMetadataAuditEntity()
                                                                                                                  .revision(-4L)
                                                                                                                  .revisionEnd(-3L)
                                                                                                                  .revisionEndTimestamp(DEFAULT_DATE)
                                                                                                                  .actionType(ADD).build(),
                                                                                       prepareMetadataAuditEntity()
                                                                                                                  .revision(-3L)
                                                                                                                  .revisionEnd(-3L)
                                                                                                                  .revisionEndTimestamp(DEFAULT_DATE)
                                                                                                                  .actionType(MOD)
                                                                                                                  .description("firstUpdate").build(),
                                                                                       prepareMetadataAuditEntity()
                                                                                                                  .revision(-2L)
                                                                                                                  .revisionEnd(-1L)
                                                                                                                  .revisionEndTimestamp(DEFAULT_DATE)
                                                                                                                  .actionType(MOD)
                                                                                                                  .description("secondUpdate").build(),
                                                                                       prepareMetadataAuditEntity()
                                                                                                                  .revision(-1L)
                                                                                                                  .revisionEnd(null)
                                                                                                                  .revisionEndTimestamp(null)
                                                                                                                  .actionType(DEL)
                                                                                                                  .description("delete").build()
        );

        initialMetadataAuditTableState.forEach(metadataAuditRepository::save);

        final Long revisionNumber = -2L;
                given()
                        .port(port)
                .when()
                        .post(ROOT + ID + "/{revision}", FILE_ID_STRING, revisionNumber)
                .then()
                        .statusCode(200);

        final List<MetadataEntity> expectedMetadataTableState =
                List.of(prepareMetadataEntity().build());
        final List<MetadataAuditEntity> expectedMetadataAuditTableState = Stream.concat(Stream.of(prepareMetadataAuditEntity().description("secondUpdate").actionType(RevisionType.ADD).build()),
                                                                                        initialMetadataAuditTableState.stream()).sorted()
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(toList());

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll();
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .peek(e -> e.setRevision(null))
                .collect(toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, is(expectedMetadataAuditTableState));
    }


}