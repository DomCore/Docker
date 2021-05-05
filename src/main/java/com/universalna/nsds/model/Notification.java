package com.universalna.nsds.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class Notification {

    private final OffsetDateTime id = OffsetDateTime.now();

    private String insuranceCaseId;

    private String noticeId;

    private UUID fileId;

    private String lastModifiedBy;

    private OffsetDateTime lastModifiedDate;

}
