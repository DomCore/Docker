package com.universalna.nsds.config;

import com.universalna.nsds.model.FileTagWIthOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
@ConfigurationProperties("application")
public class ApplicationConfigurationProperties {

    @Valid
    @NotNull
    private Extension extension;

    @NotNull
    private Set<String> editable;

    @NotNull
    private Set<FileTagWIthOrder> tag;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Extension {

        @NotNull
        private Set<@Valid Restricted> restricted;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Restricted {
            @NotBlank
            private String extension;
            private String fileType;
            private String warning;

        }
    }
}
