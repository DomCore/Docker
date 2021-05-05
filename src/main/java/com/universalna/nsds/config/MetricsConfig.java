package com.universalna.nsds.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    public static final Counter UPLOADED_FILES_TOTAL =  Metrics.counter("UPLOADED_FILES_TOTAL");

}
