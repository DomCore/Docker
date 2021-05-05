package com.universalna.nsds.service.search.profitsoft;

import lombok.Data;

import java.util.List;

@Data
public class ContractSecuritySpec {

  private boolean all = false;

  private List<Long> divisionIds;

  private List<Long> adminDivisionIds;

  private List<Long> acquisitorIds;

  private List<Long> managerIds;

  private List<Long> accepterIds;

  private List<Long> partnerDivisionIds;

}
