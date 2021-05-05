package com.universalna.nsds.controller;

import com.universalna.nsds.component.JDKUUIDGenerator;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.service.UserService;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
class UserControllerTest extends AbstractControllerTest {

    private static final String ROOT = "/user";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @TestConfiguration
    static class AdditionalConfig {
        @Bean
        public UUIDGenerator uuidGenerator() {
            return new JDKUUIDGenerator();
        }
    }

    @Test
    void saveUserProfile() throws Exception {
        final String expectedValue = "someRandomStringParameter";

        when(userService.saveUserProfile(expectedValue)).thenReturn(expectedValue);

        final String actualValue = mockMvc.perform(put(ROOT).content(expectedValue))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(actualValue, is(expectedValue));
        verify(userService).saveUserProfile(expectedValue);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void getUserData() throws Exception {
        final String expectedValue = "someRandomStringParameter";

        when(userService.getUserData()).thenReturn(expectedValue);

        final String actualValue = mockMvc.perform(get(ROOT))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(actualValue, is(expectedValue));
        verify(userService).getUserData();
        verifyNoMoreInteractions(userService);
    }
}