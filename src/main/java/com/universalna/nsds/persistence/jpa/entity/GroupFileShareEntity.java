package com.universalna.nsds.persistence.jpa.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "SHARED_FILE_GROUP")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
public class GroupFileShareEntity {

    @Id
    @GeneratedValue
    @Type(type = "pg-uuid")
    @Column(name = "ID")
    private UUID id;

    @Type(type = "jsonb")
    @Column(name = "METADATA_IDS")
    private Set<UUID> metadataIds;

    @Type(type = "pg-uuid")
    @Column(name = "KEY")
    private UUID key;

    @Type(type = "jsonb")
    @Column(name = "INFO")
    private GroupFileShareEntityInfo info;

}
