package com.universalna.nsds.config;

import com.universalna.nsds.component.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

import static com.universalna.nsds.TestConstants.DEFAULT_DATE;

/**
 * @author Igor Maksymov created on 10.05.2017.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorIdentifierProvider", dateTimeProviderRef = "dateTimeProvider")
public class MockAuditConfig {

    @Autowired
    private PrincipalProvider principalProvider;

    @Bean
    public AuditorAware<String> auditorIdentifierProvider() {
        return () -> Optional.of(principalProvider.getPrincipal());
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(DEFAULT_DATE);
    }
}

