package com.universalna.nsds.service.postgres;

import com.universalna.nsds.component.PrincipalProvider;
import com.universalna.nsds.persistence.jpa.UserRepository;
import com.universalna.nsds.persistence.jpa.entity.UserProfileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PostgresUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrincipalProvider principalProvider;

    @InjectMocks
    private PostgresUserService postgresUserService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void saveUserProfile() {
        final String principal = "testPrincipal";
        final String expectedValue = "someRandomStringParameter";

        final UserProfileEntity expectedEntity = UserProfileEntity.builder()
                .userId(principal)
                .profileData(expectedValue)
                .build();

        when(userRepository.save(expectedEntity)).thenReturn(expectedEntity);
        when(principalProvider.getPrincipal()).thenReturn(principal);

        final Serializable actualValue = postgresUserService.saveUserProfile(expectedValue);

        assertThat(actualValue, is(expectedValue));
        verify(userRepository).save(expectedEntity);
        verify(principalProvider).getPrincipal();
        verifyNoMoreInteractions(userRepository, principalProvider);
    }

    @Test
    void getUserData() {
        final String principal = "testPrincipal";
        final String expectedValue = "someRandomStringParameter";

        final UserProfileEntity existingEntity = UserProfileEntity.builder()
                .userId(principal)
                .profileData(expectedValue)
                .build();

        when(userRepository.findById(principal)).thenReturn(Optional.of(existingEntity));
        when(principalProvider.getPrincipal()).thenReturn(principal);

        final Serializable actualValue = postgresUserService.getUserData();

        assertThat(actualValue, is(expectedValue));
        verify(userRepository).findById(principal);
        verify(principalProvider).getPrincipal();
        verifyNoMoreInteractions(userRepository, principalProvider);
    }

    @Test
    void getUserData_ifProfileNotExistShouldReturnEmptyJson() {
        final String principal = "testPrincipal";

        when(userRepository.findById(principal)).thenReturn(Optional.empty());
        when(principalProvider.getPrincipal()).thenReturn(principal);

        final Serializable expectedValue = "{}";
        final Serializable actualValue = postgresUserService.getUserData();

        assertThat(actualValue, is(expectedValue));
        verify(userRepository).findById(principal);
        verify(principalProvider).getPrincipal();
        verifyNoMoreInteractions(userRepository, principalProvider);
    }
}