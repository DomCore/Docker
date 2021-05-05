package com.universalna.nsds.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.universalna.nsds.component.JDKUUIDGenerator;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.model.FileTagWIthOrder;
import com.universalna.nsds.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = DictionaryController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
public class DictionaryControllerTest extends AbstractControllerTest {

    private static final String ROOT = "/dictionary";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DictionaryService dictionaryService;

    @TestConfiguration
    static class AdditionalConfig {
        @Bean
        public UUIDGenerator uuidGenerator() {
            return new JDKUUIDGenerator();
        }
    }

    @Test
    void getDefaultFileTags() throws Exception {
        final FileTagWIthOrder tag = new FileTagWIthOrder("tag", 0);
        final FileTagWIthOrder tag2 = new FileTagWIthOrder("otherTag", 1);
        final Set<FileTagWIthOrder> givenValue = Stream.of(tag, tag, tag2 ).collect(Collectors.toSet());
        final Collection<FileTagWIthOrder> expectedValue = List.of(tag, tag2);

        when(dictionaryService.getDefaultFileTags()).thenReturn(givenValue);

        final String responseJson = mockMvc.perform(get(ROOT + "/file/tags"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final Collection<FileTagWIthOrder> actualValue = objectMapper.readValue(responseJson, new TypeReference<>() {});

        assertThat(actualValue, containsInAnyOrder(expectedValue.toArray()));
        verify(dictionaryService).getDefaultFileTags();
        verifyNoMoreInteractions(dictionaryService);
    }
}
