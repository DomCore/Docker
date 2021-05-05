package com.universalna.nsds.persistence.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
public class MetadataAuditPK implements Serializable {

    @Id
    @Column(name = "ID")
    private UUID id;

    @Id
    @Column(name = "AUDIT_REVISION")
    private Long revision;
}
