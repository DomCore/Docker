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
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
})
@Table(name = "USER_PROFILE")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
public class UserProfileEntity {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    @Type(type = "jsonb")
    @Column(name = "DATA")
    private String profileData;

}
