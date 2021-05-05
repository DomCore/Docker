package com.universalna.nsds.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

class SpringSecurityPrincipalProviderTest {

    @InjectMocks
    private PrincipalProvider springSecurityPrincipalProvider;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Disabled("implement this test")
    void getPrincipal() {
        springSecurityPrincipalProvider.getPrincipal();
    }
}