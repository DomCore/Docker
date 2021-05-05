package com.universalna.nsds.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "REVINFO")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RevinfoEntity {

    @Id
    @Column(name = "REV")
    private Long rev;

    @Column(name = "REVTSTMP")
    private Long revttstmp;

}
