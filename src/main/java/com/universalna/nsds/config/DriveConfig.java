package com.universalna.nsds.config;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.universalna.nsds.component.TokenKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DriveConfig {

    @Bean
    public IAuthenticationProvider authenticationProvider(final TokenKeeper tokenKeeper) {
        return request -> {
            final String jwt = tokenKeeper.getToken();
            request.addHeader("Authorization", "Bearer " + jwt);
        };
    }

    @Bean
    public IGraphServiceClient graphServiceClient(final IAuthenticationProvider authenticationProvider) {
        return GraphServiceClient
                .builder()
                .authenticationProvider(authenticationProvider)
                .buildClient();
    }
}
