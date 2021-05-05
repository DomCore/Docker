package com.universalna.nsds.config;

import com.universalna.nsds.component.PrincipalProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorIdentifierProvider", dateTimeProviderRef = "dateTimeProvider")
public class AuditConfig {

    @Autowired
    private PrincipalProvider principalProvider;

    @Bean
    public AuditorAware<String> auditorIdentifierProvider() {
        return () -> Optional.of(principalProvider.getPrincipal());
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
