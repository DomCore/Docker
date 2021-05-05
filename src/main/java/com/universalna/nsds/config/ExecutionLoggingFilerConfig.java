package com.universalna.nsds.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.OffsetDateTime;

@Configuration
public class ExecutionLoggingFilerConfig {

    @Component
    public class ExecutionLoggingFilter implements Filter {

        private final Logger LOGGER = LoggerFactory.getLogger(ExecutionLoggingFilter.class);

        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            try {
                final String uri = ((HttpServletRequest) req).getServletPath();
                if (!uri.contains("/upload")) {
                    final OffsetDateTime start = OffsetDateTime.now();
                    chain.doFilter(req, res);
                    final OffsetDateTime end = OffsetDateTime.now();
                    if (start.plusSeconds(5).isBefore(end)) {
                        LOGGER.info("Request execution took more that 5 seconds, uri: {} , start {} , end {}", uri, start, end);
                    }
                } else {
                    chain.doFilter(req, res);
                }
            } catch (Exception e) {
                LOGGER.info("Exception caught in ExecutionLoggingFilter", e);
                throw e;
            }
        }
    }
}
