package com.universalna.nsds.service.search.profitsoft;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Data
public class ContractInfoDto {

  private long id;

  private long ccId;

  private String number;

  private String clientName;

  private String status;

  private LocalDate signDate;

  private IdentifiedName product;
  private IdentifiedName tarifPlan;

  private CagentDto acquisitor;
  private CagentDto responsible;
  private CagentDto manager;

  private LocalDate contractBeginDate;
  private LocalDate contractEndDate;

  private LocalDate contractRealEndDate;

  private String clientPhone;
  private String clientAddress;

  private String productVersionType;
  private String reInsuranceType;

}
