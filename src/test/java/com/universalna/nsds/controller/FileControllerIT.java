package com.universalna.nsds.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.controller.dto.MetadataDTO;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.persistence.jpa.FileShareRepository;
import com.universalna.nsds.persistence.jpa.GroupFileShareRepository;
import com.universalna.nsds.persistence.jpa.entity.*;
import io.restassured.http.ContentType;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.universalna.nsds.MetadataTestConstants.FILE_ID_STRING;
import static com.universalna.nsds.MetadataTestConstants.FILE_NAME;
import static com.universalna.nsds.TestConstants.*;
import static com.universalna.nsds.model.Relation.CONTRACT;
import static com.universalna.nsds.model.Relation.INSURANCE_CASE;
import static com.universalna.nsds.model.Status.ACTIVE;
import static com.universalna.nsds.model.Status.INACTIVE;
import static io.restassured.RestAssured.given;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

class FileControllerIT extends AbstractIT implements MetadataTestValuesPreparable {

    private static final String ROOT = "/files";
    private static final String ID = "/{fileId}";
    private static final String META = "/meta";

    @Autowired
    private FileShareRepository fileShareRepository;

    @Autowired
    private GroupFileShareRepository groupFileShareRepository;

    @BeforeEach
    void tearDown() {
        metadataAuditRepository.deleteAllInBatch();
        metadataRepository.deleteAllInBatch();
        groupFileShareRepository.deleteAll();
        repositories.forEach(CrudRepository::deleteAll);
    }

    @Test
    void shouldUploadFile() {
        final MetadataDTO metadataDTO = prepareMetadataDto().build();

        given()
                .port(port)
                .multiPart("metadata", metadataDTO)
                .multiPart("file", FILE_NAME, FILE_CONTENT.get())
        .when()
                .header(AUTHORIZATION_HEADER)
                .post(ROOT)
        .then()
                .statusCode(200)
                .body(instanceOf(String.class));

        final List<MetadataEntity> expectedMetadataTableState = Stream.of(prepareMetadataEntity()
                                                                                                .uploaderId(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                                .createdBy(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                                .lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                                .build())
                                                                         .peek(this::setRandomParameters).collect(toList());
        final List<MetadataAuditEntity> expectedMetadataAuditTableState = Stream.of(prepareMetadataAuditEntity()
                                                                                                               .uploaderId(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                                               .createdBy(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                                               .lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                                               .actionType(RevisionType.ADD).build())
                                                                         .peek(this::setRandomParameters).collect(toList());

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll().stream().peek(this::setRandomParameters).collect(toList());
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                                                                                                                 .peek(this::setRandomParameters)
                                                                                                                 .peek(e -> e.setRevision(null))
                                                                                                                 .collect(toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, is(expectedMetadataAuditTableState));
    }

    @Test
    void shouldGetInactiveRelatedFilesMetadata() throws Exception {
        metadataRepository.saveAll(Arrays.asList(
                prepareMetadataEntity().build(),
                prepareMetadataEntity().id(UUID.fromString("a608153b-a9c7-4253-a150-d8358f8d0716"))
                                       .status(INACTIVE)
                                       .build())
        );

        final String response =
                given()
                        .port(port)
                .when()
                        .header(AUTHORIZATION_HEADER)
                        .get(ROOT + "/{relation}" + "/{relationId}" + "/inactive", INSURANCE_CASE, "mockedRelationId")
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        Collection<Metadata> actualValue = objectMapper.readValue(response, new TypeReference<>() {});
        final Collection<Metadata> expectedValue = Arrays.asList(
                prepareMetadata().status(INACTIVE).id(UUID.fromString("a608153b-a9c7-4253-a150-d8358f8d0716")).build()
        );

        assertThat(actualValue, is(expectedValue));
    }


    @Test
    @Disabled
    void shouldDownloadFile() {
        downloadContent("/files/{id}", FILE_ID_STRING, FILE_CONTENT.get());
    }

    @Test
    void shouldGetMetadata() throws Exception{
        metadataRepository.save(prepareMetadataEntity().build());

        final String responseJson =
        given()
                .port(port)
        .when()
                .header(AUTHORIZATION_HEADER)
                .get(ROOT + ID + META, FILE_ID_STRING)
        .then()
                .statusCode(200)
        .extract()
                .response().asString();

        final Metadata expectedValue = prepareMetadata().build();
        final Metadata actualValue = objectMapper.readValue(responseJson, Metadata.class);

        assertThat(actualValue, is(expectedValue));
    }

    //    TODO: fix slow execution
    private void downloadContent(final String path, final String fileId, final byte[] expectedValue) {
        metadataRepository.save(prepareMetadataEntity().build());

        final String actualValue =
                given()
                        .port(port)
                .when()
                        .header(AUTHORIZATION_HEADER)
                        .get(path, fileId)
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        assertThat(actualValue, is(expectedValue));
    }

    @Test
    void shouldDeleteFile() {
        metadataRepository.save(prepareMetadataEntity().build());

        given()
                .port(port)
        .when()
                .header(AUTHORIZATION_HEADER)
                .delete(ROOT + ID, FILE_ID_STRING)
        .then()
                .statusCode(200);

        final List<MetadataEntity> expectedMetadataTableState = Collections.emptyList();

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll();
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                                                                                                                  .peek(this::setRandomParameters)
                                                                                                                  .peek(e -> {
                                                                                                                      e.setRevision(null);
                                                                                                                      e.setRevisionEnd(null);
                                                                                                                      e.setRevisionEndTimestamp(null);
                                                                                                                  })
                                                                                                                  .collect(toList());

        final Collection<MetadataAuditEntity> expectedAuditTable = List.of(prepareMetadataAuditEntity().actionType(RevisionType.ADD).build(),
                prepareMetadataAuditEntity().actionType(RevisionType.DEL).build())
                .stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(Collectors.toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, containsInAnyOrder(expectedAuditTable.toArray()));
    }

    @Test
    void shouldChangeFileStatus() {
        metadataRepository.save(prepareMetadataEntity().status(INACTIVE).build());

        given()
                .port(port)
        .when()
                .header(AUTHORIZATION_HEADER)
                .param("status", ACTIVE)
                .put(ROOT + ID, FILE_ID_STRING)
        .then()
                .statusCode(200);

        final List<MetadataEntity> expectedMetadataTableState = Arrays.asList(prepareMetadataEntity().lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL).build());

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll();
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(toList());

        final Collection<MetadataAuditEntity> expectedAuditTable = List.of(prepareMetadataAuditEntity().status(INACTIVE).actionType(RevisionType.ADD).build(),
                prepareMetadataAuditEntity().lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL).status(ACTIVE).actionType(RevisionType.MOD).build())
                .stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(Collectors.toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, containsInAnyOrder(expectedAuditTable.toArray()));
    }

    @Test
    void shouldUpdateFileName() {
        metadataRepository.save(prepareMetadataEntity().build());

        final String updatedName = "updatedName";
        given()
                .port(port)
                .body(updatedName)
        .when()
                .header(AUTHORIZATION_HEADER)
                .put(ROOT + ID + META + "/name", FILE_ID_STRING)
        .then()
                .statusCode(200);

        final String originalFileExtension = ".jpg";
        final List<MetadataEntity> expectedMetadataTableState = Collections.singletonList(prepareMetadataEntity().lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL).name(updatedName + originalFileExtension).build());

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll();
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(toList());

        final Collection<MetadataAuditEntity> expectedAuditTable = List.of(prepareMetadataAuditEntity().actionType(RevisionType.ADD).build(),
                prepareMetadataAuditEntity().name(updatedName + originalFileExtension).actionType(RevisionType.MOD).build())
                .stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(Collectors.toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, containsInAnyOrder(expectedAuditTable.toArray()));
    }

    @Test
    void shouldChangeFileDescription() {
        metadataRepository.save(prepareMetadataEntity().build());

        final String updatedDescription = "testUpdatedDescription";
        given()
                .port(port)
                .body(updatedDescription)
        .when()
                .header(AUTHORIZATION_HEADER)
                .put(ROOT + ID + META + "/description", FILE_ID_STRING)
        .then()
                .statusCode(200);

        final List<MetadataEntity> expectedMetadataTableState = Collections.singletonList(prepareMetadataEntity().lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL).description(updatedDescription).build());


        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll();
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(toList());

        final Collection<MetadataAuditEntity> expectedAuditTable = List.of(prepareMetadataAuditEntity().actionType(RevisionType.ADD).build(),
                prepareMetadataAuditEntity().description(updatedDescription).actionType(RevisionType.MOD).build())
                .stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(Collectors.toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, containsInAnyOrder(expectedAuditTable.toArray()));
    }

    @Test
    void shouldCreateSharedUri() {
        metadataRepository.save(prepareMetadataEntity().build());

        given()
                .port(port)
        .when()
                .header(AUTHORIZATION_HEADER)
                .get(ROOT + ID + "/publish", FILE_ID_STRING)
        .then()
                .statusCode(200)
                .body(instanceOf(String.class));

        final List<FileShareEntity> expectedDatabaseState = Arrays.asList(FileShareEntity.builder().metadataId(UUID.fromString(FILE_ID_STRING)).info(new Info()).build());
        final List<FileShareEntity> actualDatabaseState = fileShareRepository.findAll().stream().peek(e -> {
                                                                                                  e.setId(null);
                                                                                                  e.setKey(null); })
                                                                                                .collect(toList());

        assertThat(actualDatabaseState, is(expectedDatabaseState));
    }



    @Test
    void shouldUpdateTagsToMetadata() {
        metadataRepository.save(prepareMetadataEntity().build());

        final String tagName = "newTag";
        final FileTagDTO tagDTO = new FileTagDTO(tagName);

        given()
                .port(port)
        .when()
                .header(AUTHORIZATION_HEADER)
                .contentType(ContentType.JSON)
                .body(tagDTO)
                .put(ROOT + ID + META + "/tags", FILE_ID_STRING)
        .then()
                .statusCode(200);

        final List<MetadataEntity> expectedMetadataTableState = Collections.singletonList(prepareMetadataEntity().lastModifiedBy(CURRENT_SECURITY_TOKEN_PRINCIPAL).tags(Collections.singleton(new FileTagEntity(tagName))).build());

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll();
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(toList());

        final Collection<MetadataAuditEntity> expectedAuditTable = List.of(prepareMetadataAuditEntity().actionType(RevisionType.ADD).build(),
                prepareMetadataAuditEntity().tags(Collections.singleton(new FileTagEntity(tagName))).actionType(RevisionType.MOD).build())
                .stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(Collectors.toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, containsInAnyOrder(expectedAuditTable.toArray()));
    }

    @Test
    void shouldCopyFileToAnotherRelation() {
        metadataRepository.save(prepareMetadataEntity().build());
        final Relation relationToClone = CONTRACT;
        final String relationIdToClone = "2";
        final String tag = "thisTagShouldReplaceAllOthers";
        final Set<FileTagDTO> tags = Collections.singleton(new FileTagDTO(tag));

        given()
                .port(port)
                .when()
                .header(AUTHORIZATION_HEADER)
                .contentType(ContentType.JSON)
                .body(tags)
                .post(ROOT + ID + "/copy/{copyToRelation}/{copyToRelationId}", FILE_ID_STRING, relationToClone, relationIdToClone)
                .then()
                .statusCode(200);

        final List<MetadataEntity> expectedMetadataTableState = Stream.of(prepareMetadataEntity().build(),
                                                                              prepareMetadataEntity().relation(relationToClone).relationId(relationIdToClone)
                                                                                      .tags(Collections.singleton(new FileTagEntity(tag)))
                                                                                      .createdBy(CURRENT_SECURITY_TOKEN_PRINCIPAL)
                                                                                      .build())
                .peek(this::setRandomParameters)
                .collect(toList());

        final List<MetadataEntity> actualMetadataTableState = metadataRepository.findAll().stream().peek(this::setRandomParameters).collect(toList());
        final List<MetadataAuditEntity> actualMetadataAuditTableState = metadataAuditRepository.findAll().stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setId(null);
                    e.setTimestamp(null);
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(toList());

        final Collection<MetadataAuditEntity> expectedAuditTable = List.of(prepareMetadataAuditEntity().actionType(RevisionType.ADD).build(),
                prepareMetadataAuditEntity().relation(relationToClone).relationId(relationIdToClone).tags(Collections.singleton(new FileTagEntity(tag))).createdBy(CURRENT_SECURITY_TOKEN_PRINCIPAL).actionType(RevisionType.ADD).build())
                .stream()
                .peek(this::setRandomParameters)
                .peek(e -> {
                    e.setRevision(null);
                    e.setRevisionEnd(null);
                    e.setRevisionEndTimestamp(null);
                })
                .collect(Collectors.toList());

        assertThat(actualMetadataTableState, is(expectedMetadataTableState));
        assertThat(actualMetadataAuditTableState, containsInAnyOrder(expectedAuditTable.toArray()));
    }

    @Test
    void shouldCreateGroupOfSharedFiles() {
        final UUID existingMetadataId = metadataRepository.save(prepareMetadataEntity().build()).getId();
        final UUID notExistingMetadataId = UUID.randomUUID();

        given()
                .port(port)
                .when()
                .header(AUTHORIZATION_HEADER)
                .contentType(ContentType.JSON)
                .body(Arrays.asList(existingMetadataId, notExistingMetadataId))
                .post(ROOT + "/group/publish")
                .then()
                .statusCode(200)
                .body(instanceOf(String.class));

        final List<GroupFileShareEntity> expectedDatabaseState = Arrays.asList(GroupFileShareEntity.builder()
                .id(null)
                .metadataIds(Collections.singleton(UUID.fromString(FILE_ID_STRING)))
                .info(null)
                .build()
        );

        final List<GroupFileShareEntity> actualDatabaseState = groupFileShareRepository.findAll().stream()
                .peek(e -> {
                    e.setId(null);
                    e.setKey(null);
                })
                .collect(toList());

        final GroupFileShareEntityInfo info = actualDatabaseState.get(0).getInfo();
        actualDatabaseState.forEach(e -> e.setInfo(null));
        assertThat(actualDatabaseState, is(expectedDatabaseState));
        assertThat(info.getCreatedAt().plus(6, MONTHS), is(info.getExpireAt()));
    }

    private void setRandomParameters(AbstractMetadataEntity metadataEntity) {
        metadataEntity.setId(null);
        metadataEntity.setTimestamp(null);
        metadataEntity.setFileStorageFileId(null);
        metadataEntity.setLastModifiedDate(null);
        metadataEntity.setLastModifiedBy(null);
    }
}