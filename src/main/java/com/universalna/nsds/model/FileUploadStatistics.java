package com.universalna.nsds.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class FileUploadStatistics {

    private OffsetDateTime from;

    private OffsetDateTime to;

    private Long metadataCounter;

    private Long filesCounter;

    private Map<String, Long> relations;

    private Collection<Metadata> metadata;

}
