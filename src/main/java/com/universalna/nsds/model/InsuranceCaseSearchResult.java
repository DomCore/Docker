package com.universalna.nsds.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder(builderClassName = "Builder")
public class InsuranceCaseSearchResult {

    private String caseNumber;

    private String notificationNumber;

    private LocalDateTime incidentTime;

    private String insuranceType;

    private String contractNumber;

    private String clientName;

    private String status;

}
