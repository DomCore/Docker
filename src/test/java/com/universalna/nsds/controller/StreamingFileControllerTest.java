package com.universalna.nsds.controller;

import com.universalna.nsds.component.AuthorizedPartyProvider;
import com.universalna.nsds.component.JDKUUIDGenerator;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.component.content.ContentDownloader;
import com.universalna.nsds.controller.dto.MetadataDTO;
import com.universalna.nsds.model.File;
import com.universalna.nsds.service.FileService;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.util.HashMap;

import static com.universalna.nsds.MetadataTestConstants.FILE_NAME;
import static com.universalna.nsds.MetadataTestConstants.FILE_SIZE;
import static com.universalna.nsds.TestConstants.FILE_CONTENT;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource("/config/application.yml")
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = StreamingFileController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Disabled
class StreamingFileControllerTest extends AbstractControllerTest implements MetadataTestValuesPreparable {

    private static final String ROOT = "/files";
    private static final String ID = "/{fileId}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    protected FileService fileService;

    @MockBean
    private Mapper metadataMapper;

    @MockBean
    private Validator validator;

    @MockBean
    private ContentDownloader contentDownloader;

    @MockBean
    private AuthorizedPartyProvider authorizedPartyProvider;

    @TestConfiguration
    static class AdditionalConfig {

        @Bean
        public UUIDGenerator uuidGenerator() {
            return new JDKUUIDGenerator();
        }
    }

    @Nested
    @Disabled
    class Upload {

//        https://stackoverflow.com/questions/22642012/how-to-use-springs-mockmultiparthttpservletrequest-getting-no-multipart-bound
        @Test
        void upload() throws Exception {
            final MetadataDTO metadataDTO = prepareMetadataDto().build();

//            final ByteArrayInputStream inputStream = new ByteArrayInputStream(FILE_CONTENT.get());
            MockMultipartFile meta = new MockMultipartFile("metadata",  "metadata", MediaType.APPLICATION_JSON_VALUE,objectMapper.writeValueAsBytes(metadataDTO));

            HashMap<String, String> contentTypeParams = new HashMap<>();
            contentTypeParams.put("boundary", "265001916915724");

            MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

            final String boundary = "q1w2e3r4t5y6u7i8o9";
            final String contentType = "multipart/form-data; boundary=" + boundary;
            MockMultipartFile file = new MockMultipartFile("file", "file",contentType, FILE_CONTENT.get());
//            final byte[] metadataWithBoundary = createFileContent(objectMapper.writeValueAsBytes(metadataDTO), "q1w2e3r4t5y6u7i8o1", "multipart/form-data; boundary=" + "q1w2e3r4t5y6u7i8o1", "metadata");
            final byte[] fileWithBoundary = createFileContent(FILE_CONTENT.get(), "q1w2e3r4t5y6u7i8o9", contentType, FILE_NAME);

            mockMvc.perform(multipart(ROOT)
                    .file(file)
                    .content(objectMapper.writeValueAsBytes(metadataDTO))
                    .contentType(contentType))
                    .andExpect(status().isOk());


//            final Part meta = new MockPart("metadata", objectMapper.writeValueAsBytes(metadataDTO));
//            final Part file = new MockPart("file", "file", contentWithBoundary);
//            mockMvc.perform(post(ROOT)
//                            .contentType(contentType)
////                    .param("metadata", objectMapper.writeValueAsString(metadataDTO))
//                    .content(metadataWithBoundary)
//                    .content(fileWithBoundary)
////                    .part(meta, file)
////                    .part()
////                    .
////                            .file(meta)
////                            .file(file)
////                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//            )

//            .param("metadata", objectMapper.writeValueAsString(metadataDTO)))
//                    .andExpect(status().isOk());
//            .doPost(mockRequest, response);
//            verify(metadataMapper).toModel(metadataDTO, FILE_NAME);
//            verify(fileService).create(inputStream, prepareMetadata().build());
        }

        private byte[] createFileContent(byte[] data, String boundary, String contentType, String fileName){
            String start = "--" + boundary + "\r\n Content-Disposition: form-data; name=\"file\"; filename=\""+fileName+"\"\r\n"
                    + "Content-type: "+contentType+"\r\n\r\n";

            String end = "\r\n--" + boundary + "--"; // correction suggested @butfly
            return ArrayUtils.addAll(start.getBytes(),ArrayUtils.addAll(data,end.getBytes()));
        }
    }

    @Nested
    class Download{

        @Test
        @Disabled
        void download() throws Exception {
            final String fileId = "1";
            final String filename = "testFileName";
            final File expectedValue = File.builder()
                    .originalName(filename)
                    .content(new ByteArrayInputStream(FILE_CONTENT.get()))
                    .build();

            when(fileService.get(fileId)).thenReturn(expectedValue);

            MvcResult mvcResult = mockMvc.perform(get(ROOT + ID, fileId)).andReturn();

            byte[] responseBytes = mockMvc
                    .perform(asyncDispatch(mvcResult))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, FILE_SIZE.toString()))
                    .andExpect(header().string(HttpHeaders.TRANSFER_ENCODING,"binary"))
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename +"\""))
                    .andReturn()
                    .getResponse().getContentAsByteArray();

            assertArrayEquals(FILE_CONTENT.get(), responseBytes);
            verify(fileService).get(fileId);
            verifyNoMoreInteractions();
        }

        @Test
        void ifFileIdIsBlankThen400() throws Exception {
            final String fileId = " ";

            mockMvc.perform(get(ROOT + ID, fileId))
                    .andExpect(status().isBadRequest());

            verifyZeroInteractions(fileService);
            verifyNoMoreInteractions();
        }
    }

    private void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(fileService, metadataMapper, validator, contentDownloader, authorizedPartyProvider);
    }

}