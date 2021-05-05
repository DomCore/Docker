package com.universalna.nsds.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.universalna.nsds.component.JDKUUIDGenerator;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.model.FileTag;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.model.ModelWithLastModified;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.service.FileService;
import com.universalna.nsds.service.postgres.TransactionalReadOnlyMetadataService;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.universalna.nsds.MetadataTestConstants.BLANK_STRING;
import static com.universalna.nsds.MetadataTestConstants.FILE_ID;
import static com.universalna.nsds.MetadataTestConstants.FILE_ID_STRING;
import static com.universalna.nsds.MetadataTestConstants.RELATION_ID;
import static com.universalna.nsds.model.Relation.INSURANCE_CASE;
import static com.universalna.nsds.model.Status.ACTIVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = MetadataController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class MetadataControllerTest extends AbstractControllerTest {

    private static final String ROOT = "/files";

    private static final String META = "/meta";

    private static final String ID = "/{fileId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Mapper mapper;

    @MockBean
    protected FileService fileService;

    @MockBean
    protected TransactionalReadOnlyMetadataService transactionalReadOnlyMetadataService;

    @TestConfiguration
    static class AdditionalConfig {
        @Bean
        public UUIDGenerator uuidGenerator() {
            return new JDKUUIDGenerator();
        }
    }

    @Nested
    class ChangeFileStatus {

        @Test
        void changeFileStatus() throws Exception {
            final String fileId = FILE_ID_STRING;
            final ModelWithLastModified<String> expectedValue = ModelWithLastModified.<String>builder().body(fileId).build();

            when(fileService.changeFileStatus(fileId, ACTIVE)).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(put(ROOT + ID, fileId)
                    .param("status", String.valueOf(ACTIVE)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            final ModelWithLastModified<String> actualValue = objectMapper.readValue(responseJson, ModelWithLastModified.class);

            assertThat(actualValue, is(expectedValue));
            verify(fileService).changeFileStatus(fileId, ACTIVE);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThenReturn400() throws Exception {
            mockMvc.perform(put(ROOT + ID, BLANK_STRING).param("status", String.valueOf(ACTIVE)))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class GetRelatedMetadata {
        @Test
        void getRelatedMetadata() throws Exception {
            final Relation relation = Relation.INSURANCE_CASE;
            final String relationId = RELATION_ID;
            final Collection<Metadata> expectedValue = Arrays.asList(Metadata.builder().build(), Metadata.builder().build());

            when(fileService.getRelatedMetadata(relation, relationId)).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(get(ROOT + "/{relation}" + "/{relationId}", relation, relationId))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            final Collection<Metadata> actualValue = objectMapper.readValue(responseJson, new TypeReference<Collection<Metadata>>() {});

            assertThat(actualValue, is(expectedValue));
            verify(fileService).getRelatedMetadata(relation, relationId);
            verifyNoMoreInteractions();
        }

        @Test
        void ifRelationIdIsBlankThen400() throws Exception {
            mockMvc.perform(get(ROOT + "/{relation}" + "/{relationId}", Relation.INSURANCE_CASE, BLANK_STRING))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class GetInactiveRelatedFileIds {
        @Test
        void getInactiveRelatedFileIds() throws Exception {
            final Relation relation = INSURANCE_CASE;
            final String relationId = RELATION_ID;
            final Collection<Metadata> expectedValue = Arrays.asList(Metadata.builder().build(), Metadata.builder().build());

            when(fileService.getInactiveFilesMetadata(relation, relationId)).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(get(ROOT + "/{relation}" + "/{relationId}" + "/inactive", relation, relationId))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            final Collection<Metadata> actualValue = objectMapper.readValue(responseJson, new TypeReference<Collection<Metadata>>() {});

            assertThat(actualValue, is(expectedValue));
            verify(fileService).getInactiveFilesMetadata(relation, relationId);
            verifyNoMoreInteractions();
        }

        @Test
        void ifRelationIdIsBlankThen400() throws Exception {
            mockMvc.perform(get(ROOT + "/{relation}" + "/{relationId}" + "/inactive", INSURANCE_CASE, BLANK_STRING))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class CreateSharedUri {
        @Test
        void createSharedUri() throws Exception {
            final String expectedValue = "testUri";
            final String fileId = FILE_ID_STRING;

            when(fileService.createSharedUri(fileId)).thenReturn(expectedValue);

            final String response = mockMvc.perform(get(ROOT + ID + "/publish", fileId))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            assertThat(response, is(expectedValue));
            verify(fileService).createSharedUri(fileId);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            mockMvc.perform(get(ROOT + ID + "/publish", BLANK_STRING))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class CreateSharedUriToGroupOfFiles {

        @Test
        void createSharedUriToGroupOfFiles() throws Exception {
            final Collection<UUID> fileIds = Arrays.asList(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), UUID.fromString("550e8400-e29b-41d4-a716-446655440002"));
            final String expectedValue = "testString";

            when(fileService.createSharedUriToGroupOfFiles(fileIds)).thenReturn(expectedValue);

            final String response = mockMvc.perform(post(ROOT + "/group/publish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fileIds)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse()
                    .getContentAsString();

            assertThat(response, is(expectedValue));
            verify(fileService).createSharedUriToGroupOfFiles(fileIds);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdsIsNullThen400() throws Exception {

            mockMvc.perform(post(ROOT + "/group/publish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content((byte[]) null))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdsEmptyThen400() throws Exception {
            final Collection<UUID> fileIds = Collections.emptyList();

            mockMvc.perform(post(ROOT + "/group/publish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(fileIds)))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class GetMetadata {
        @Test
        void getMetadata() throws Exception {
            final String fileId = FILE_ID_STRING;
            final Metadata expectedValue = Metadata.builder().build();

            when(fileService.getMetadata(fileId)).thenReturn(expectedValue);

            String responseMetadata = mockMvc.perform(get(ROOT + ID + META, fileId))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            final Metadata actualValue = objectMapper.readValue(responseMetadata, new TypeReference<Metadata>() {});

            assertThat(actualValue, is(expectedValue));
            verify(fileService).getMetadata(fileId);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            mockMvc.perform(get(ROOT + ID + META, BLANK_STRING))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class Delete {

        @Test
        void deleteFileById() throws Exception {
            final String fileId = FILE_ID_STRING;

            mockMvc.perform(delete(ROOT + ID, fileId))
                    .andExpect(status().isOk());

            verify(fileService).delete(fileId);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            mockMvc.perform(delete(ROOT + ID, BLANK_STRING))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class UpdateName {

        @Test
        void updateName() throws Exception {
            final String fileId = FILE_ID_STRING;
            final String name = "testName";
            final ModelWithLastModified<String> expectedValue = ModelWithLastModified.<String>builder().body(name).build();

            when(fileService.updateName(fileId, name)).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(put(ROOT + ID + META + "/name", fileId)
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
                    .content(name))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            final ModelWithLastModified<String> actualValue = objectMapper.readValue(responseJson, ModelWithLastModified.class);

            assertThat(actualValue, is(expectedValue));
            verify(fileService).updateName(fileId, name);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {

            final String name = "testName";

            mockMvc.perform(put(ROOT + ID + META + "/name", BLANK_STRING)
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
                    .content(name))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }

        @Test
        void ifNameIsBlankThen400() throws Exception {
            final String fileId = FILE_ID_STRING;
            final String name = " ";

            mockMvc.perform(put(ROOT + ID + META + "/name", fileId)
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
                    .content(name))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();

        }
    }

    @Nested
    class UpdateDescription {

        @Test
        void updateDescription() throws Exception {
            final String fileId = FILE_ID_STRING;
            final String description = "testName";
            final ModelWithLastModified<String> expectedValue = ModelWithLastModified.<String>builder().body("description").build();

            when(fileService.updateDescription(fileId, description)).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(put(ROOT + ID + META + "/description", fileId)
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
                    .content(description))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            final ModelWithLastModified<String> actualValue = objectMapper.readValue(responseJson, ModelWithLastModified.class);

            assertThat(actualValue, is(expectedValue));
            verify(fileService).updateDescription(fileId, description);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            final String fileId = " ";
            final String description = "testName";

            mockMvc.perform(put(ROOT + ID + META + "/description", fileId)
                    .contentType(MediaType.TEXT_PLAIN_VALUE)
                    .content(description))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class UpdateTags {

        @Test
        void updateTags() throws Exception {
            final String fileId = FILE_ID_STRING;
            final FileTagDTO tagDTO1 = new FileTagDTO("tag1");
            final FileTagDTO tagDTO2 = new FileTagDTO("tag2");
            final Collection<FileTagDTO> tagDTOs = List.of(tagDTO1, tagDTO2);
            final FileTag tag1 = new FileTag("tag1");
            final FileTag tag2 = new FileTag("tag2");
            final Set<FileTag> tags = Set.of(tag1,tag2);

            when(mapper.toModel(tagDTO1)).thenReturn(tag1);
            when(mapper.toModel(tagDTO2)).thenReturn(tag2);

            mockMvc.perform(put(ROOT + ID + META + "/tags", fileId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagDTOs)))
                    .andExpect(status().isOk());

            verify(fileService).updateTags(fileId, tags);
            tagDTOs.forEach(m -> verify(mapper).toModel(m));
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class UpdateInfo {

        @Test
        void updateInfo() throws Exception {
            final String fileId = FILE_ID_STRING;
            final JSONObject infoJson = new JSONObject();
            final ModelWithLastModified<String> expectedValue = ModelWithLastModified.<String>builder().body("expectedValue").build();

            when(fileService.updateInfo(fileId, infoJson.toJSONString())).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(put(ROOT + ID + META + "/info", fileId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(infoJson.toJSONString()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            final ModelWithLastModified<String> actualValue = objectMapper.readValue(responseJson, ModelWithLastModified.class);

            assertThat(actualValue, is(expectedValue));
            verify(fileService).updateInfo(fileId, infoJson.toJSONString());
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            final String description = "testName";

            mockMvc.perform(put(ROOT + ID + META + "/info", BLANK_STRING)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(description))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class CopyMetadataToRelation {

        @Test
        void copyMetadataToRelation() throws Exception {
            final UUID id = FILE_ID;
            final Relation copyToRelation = INSURANCE_CASE;
            final String copyToRelationId = "testCopyToRelationId";
            final UUID expectedValue = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
            final Set<FileTagDTO> tagsDTO = Set.of(new FileTagDTO("tag1"), new FileTagDTO("tag2"));

            when(fileService.copyMetadataAsNew(id, copyToRelation, copyToRelationId, tagsDTO)).thenReturn(expectedValue);

            final String responseJson = mockMvc.perform(post(ROOT + ID + "/copy/{copyToRelation}/{copyToRelationId}", id, copyToRelation, copyToRelationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagsDTO)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse()
                    .getContentAsString();

            final UUID actualValue = objectMapper.readValue(responseJson, new TypeReference<UUID>() {});

            assertThat(actualValue, is(expectedValue));
            verify(fileService).copyMetadataAsNew(id, copyToRelation, copyToRelationId, tagsDTO);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            final Set<FileTagDTO> tagsDTO = Set.of(new FileTagDTO("tag1"), new FileTagDTO("tag2"));

            mockMvc.perform(post(ROOT + ID + "/copy/{copyToRelation}/{copyToRelationId}", FILE_ID_STRING, INSURANCE_CASE, BLANK_STRING)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagsDTO)))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class CreateUrlToEditableFile {

        @Test
        void createUrlToEditableFile() throws Exception {
            final UUID id = FILE_ID;
            final String expectedValue = "urlToEditableFile";

            when(fileService.createUrlToEditableFile(id)).thenReturn(expectedValue);

            final String actualValue = mockMvc.perform(get(ROOT + ID + "/edit", id)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse()
                    .getContentAsString();

            assertThat(actualValue, is(expectedValue));
            verify(fileService).createUrlToEditableFile(id);
            verifyNoMoreInteractions();
        }
    }

    @Nested
    class GetAllTagsFromAllEntities {

        @Test
        void getAllTagsFromAllEntities_OrderedAlphabetically() throws Exception {
            final Collection<String> expectedValue = List.of("tag1", "tag2", "tag3");

            when(fileService.getAllTagsFromAllEntities()).thenReturn(Set.of("tag3", "tag1", "tag2"));

            final String jsonResponse = mockMvc.perform(get(ROOT + "/tags"))
                    .andExpect(status().isOk())
                    .andReturn().getResponse()
                    .getContentAsString();

            final List<String> actualValue = objectMapper.readValue(jsonResponse, new TypeReference<List<String>>() {});

            assertThat(actualValue, is(expectedValue));
            verify(fileService).getAllTagsFromAllEntities();
            verifyNoMoreInteractions();
        }
    }

    @Nested
    @Disabled("TODO")
    class GetAllTagsFromAllEntitiesGroupedWithRelations {

    }

    @Nested
    @Disabled("TODO")
    class GetTagsByEntity {

    }

    @Nested
    @Disabled("TODO")
    class Archive {

    }

    @Nested
    @Disabled("TODO")
    class RevertFromArchive {

    }

    @Nested
    @Disabled("TODO")
    class SetTempFilesToPersistent {

    }

    private void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(mapper, fileService, transactionalReadOnlyMetadataService);
    }

}