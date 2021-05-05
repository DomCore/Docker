package com.universalna.nsds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/*")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .exposedHeaders( "Access-Control-Allow-Credentials",
                                         "Access-Control-Allow-Origin",
                                         "Access-Control-Allow-Headers",
                                         "Access-Control-Allow-Methods"
                                         )
                .allowCredentials(true);
            }
        };
    }

    @Component
    public class SimpleCORSFilter implements Filter {

        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setHeader("Access-Control-Allow-Headers", "access-control-allow-origin,Origin,X-Requested-With,Content-Type,Accept");
            response.setHeader("Access-Control-Allow-Methods", "GET,HEAD,POST,PUT,PATCH,DELETE");
            response.setHeader("Access-Control-Allow-Origin", "*");
            chain.doFilter(req, res);
        }
        public void init(FilterConfig filterConfig) {}
        public void destroy() {}

    }
}
