package com.universalna.nsds;

import com.universalna.nsds.config.ApplicationConfigurationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@EnableScheduling
@EnableConfigurationProperties(ApplicationConfigurationProperties.class)
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class Application {

    private static final Logger LOG = LogManager.getLogger(Application.class);

    public static void main(String[] args) throws UnknownHostException {
        final ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        final Environment env = context.getEnvironment();
        final String protocol;
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        } else {
            protocol = "http";
        }
        LOG.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());
    }
}
