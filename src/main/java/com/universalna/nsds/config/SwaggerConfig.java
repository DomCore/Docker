package com.universalna.nsds.config;

import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.List;

import static com.universalna.nsds.Profiles.DEPLOYMENT;
import static com.universalna.nsds.Profiles.PRODUCTION;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    @Profile("!" + DEPLOYMENT + " & " + "!" + PRODUCTION)
    public Docket apiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(new ApiKey("JWT", HttpHeaders.AUTHORIZATION, In.HEADER.name())))
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    @Profile(value = DEPLOYMENT)
    public Docket deploymentApiDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Collections.singletonList(new ApiKey("JWT", HttpHeaders.AUTHORIZATION, In.HEADER.name())))
                .pathMapping("/api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    @Profile(PRODUCTION)
    public Docket deploymentApiDocketForProduction() {
        return new Docket(DocumentationType.SWAGGER_2).enable(false);
    }

    @Profile(PRODUCTION)
    @RestController
    public class DisableSwaggerUiController {

        @RequestMapping(value = "swagger-ui.html", method = RequestMethod.GET)
        public ResponseEntity<Void> getSwaggerInProduction() {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.any())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        return Collections.singletonList(new SecurityReference("JWT", new AuthorizationScope[]{new AuthorizationScope("global", "accessEverything")}));
    }

}
