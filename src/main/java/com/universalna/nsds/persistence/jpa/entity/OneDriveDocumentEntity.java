package com.universalna.nsds.persistence.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "DRIVE_DOCUMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class OneDriveDocumentEntity {

    @Id
    @Column(name = "METADATA_ID")
    private UUID metadataId;

    @Column(name = "DRIVE_ID")
    private String oneDriveId;

}
