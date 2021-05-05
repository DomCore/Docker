package com.universalna.nsds.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universalna.nsds.Application;
import com.universalna.nsds.component.MockSecurityUtil;
import com.universalna.nsds.component.MockTokenKeeper;
import com.universalna.nsds.component.content.MockBlobClient;
import com.universalna.nsds.config.MockAuditConfig;
import com.universalna.nsds.config.MockAzureConfig;
import com.universalna.nsds.persistence.jpa.MetadataAuditRepository;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.util.Collection;
import java.util.TimeZone;

import static com.universalna.nsds.TestConstants.INTEGRATION_TEST_PRINCIPAL;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class,
                                                                                       MockAuditConfig.class,
                                                                                       MockBlobClient.class,
                                                                                       MockAzureConfig.class,
                                                                                       MockTokenKeeper.class,
                                                                                       MockSecurityUtil.class
                                                                                       })
abstract class AbstractIT {

    private static final DockerComposeContainer environment = new DockerComposeContainer(new java.io.File("src/test/resources/docker-compose.yml"))
            .withLocalCompose(true)
            .waitingFor("nsds-rabbitmq-it", new WaitAllStrategy());

    @LocalServerPort
    int port;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected Collection<CrudRepository> repositories;

    @Autowired
    protected MetadataRepository metadataRepository;

    @Autowired
    protected MetadataAuditRepository metadataAuditRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(INTEGRATION_TEST_PRINCIPAL, null));
    }

    //    TODO: remove this workaround for DBUnit
    //    TODO: remove environment.start() from static block and use JUnit, container should launch nce per all tests launch, not per class
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        environment.start();
    }

}
