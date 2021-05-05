package com.universalna.nsds.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.universalna.nsds.persistence.jpa.FileShareRepository;
import com.universalna.nsds.persistence.jpa.GroupFileShareRepository;
import com.universalna.nsds.persistence.jpa.entity.FileShareEntity;
import com.universalna.nsds.persistence.jpa.entity.GroupFileShareEntity;
import com.universalna.nsds.persistence.jpa.entity.GroupFileShareEntityInfo;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static com.universalna.nsds.MetadataTestConstants.FILE_ID_STRING;
import static com.universalna.nsds.MetadataTestConstants.FILE_NAME;
import static com.universalna.nsds.TestConstants.FILE_CONTENT;
import static io.restassured.RestAssured.given;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PublicAccessControllerIT extends AbstractIT implements MetadataTestValuesPreparable {

    private static final String ROOT = "/public";

    private static final String FILES = "/files";
    private static final String GROUP = "/group";

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
    void shouldGetGroupOfSharedFiles() throws Exception {
        final UUID key = UUID.randomUUID();
        final Set<UUID> expectedValue = Set.of(UUID.randomUUID(), UUID.randomUUID());
        final GroupFileShareEntityInfo info = new GroupFileShareEntityInfo(null, null, OffsetDateTime.now().plus(1, DAYS));
        final GroupFileShareEntity groupFileShareEntity = GroupFileShareEntity.builder()
                .id(UUID.randomUUID())
                .metadataIds(expectedValue)
                .key(key)
                .info(info)
                .build();

        groupFileShareRepository.save(groupFileShareEntity);

        final String responseJson =
                given()
                        .port(port)
                .when()
                        .param("key", key)
                        .get(ROOT + FILES + GROUP)
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        final Set<UUID> actualValue = objectMapper.readValue(responseJson, new TypeReference<>() {});

        assertThat(actualValue, is(expectedValue));
    }

    @Test
    @Disabled("fix bytes size")
    void shouldDownloadSharedFilesAsZip() {
        final Set<UUID> MetadataIds = Set.of(metadataRepository.save(prepareMetadataEntity().build()).getId(),
                                                metadataRepository.save(prepareMetadataEntity().id(UUID.randomUUID()).build()).getId());

        final UUID key = UUID.randomUUID();
        final GroupFileShareEntityInfo info = new GroupFileShareEntityInfo(null, null, OffsetDateTime.now().plus(1, DAYS));
        final GroupFileShareEntity groupFileShareEntity = GroupFileShareEntity.builder()
                                                                                        .id(UUID.randomUUID())
                                                                                        .metadataIds(MetadataIds)
                                                                                        .key(key)
                                                                                        .info(info)
                                                                                        .build();

        groupFileShareRepository.save(groupFileShareEntity);

        final byte[] expectedValue = zipFiles(FILE_CONTENT.get(), FILE_CONTENT.get());

        final byte[] actualValue =
                given()
                        .port(port)
                .when()
                        .param("key", key)
                        .get(ROOT + FILES + GROUP + "/zip")
                .then()
                        .statusCode(200)
                .extract()
                        .response().asByteArray();

        System.out.println(new String(actualValue));

        assertThat(actualValue, is(expectedValue));
    }

    private byte[] zipFiles(byte[]... contents) {
        ByteArrayOutputStream archiveStream = new ByteArrayOutputStream();
        try {
            ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
            for (byte[] content : contents) {
                ZipArchiveEntry entry = new ZipArchiveEntry(FILE_NAME);
                archive.putArchiveEntry(entry);
                BufferedInputStream input = new BufferedInputStream(new ByteArrayInputStream(content));
                IOUtils.copy(input, archive);
                input.close();
                archive.closeArchiveEntry();
            }
            archive.finish();
            archiveStream.close();
        } catch (IOException | ArchiveException e) {
            throw new RuntimeException(e);
        }
        return archiveStream.toByteArray();
    }

    @Test
    @Disabled
    void shouldDownloadFileBySharedUri() {
        final UUID key = UUID.fromString("04c39d72-09fa-4e0f-8a79-ef65f7aeaef1");
        metadataRepository.save(prepareMetadataEntity().build());
        fileShareRepository.save(FileShareEntity.builder().id(key).metadataId(UUID.fromString(FILE_ID_STRING)).key(key).build());

        final byte[] expectedValue = FILE_CONTENT.get();

        final byte[] actualValue =
                given()
                        .port(port)
                .when()
                        .param("key", key)
                        .get(ROOT + FILES)
                .then()
                        .statusCode(200)
                .extract()
                        .response().asByteArray();

        assertThat(actualValue, is(expectedValue));
    }
}
