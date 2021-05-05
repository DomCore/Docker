package com.universalna.nsds.persistence.jpa.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class GroupFileShareEntityInfo {

    private String createdBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime expireAt;

}
