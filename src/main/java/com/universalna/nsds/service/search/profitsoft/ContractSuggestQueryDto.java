package com.universalna.nsds.service.search.profitsoft;

import lombok.Data;

import java.util.List;

@Data
public class ContractSuggestQueryDto {

  /**
   * Номер договора или его фрагмент
   */
  private String number;

  /**
   * Идентификатор продукта
   */
  private Long productId;

  /**
   * Идентификатор вида (правила) страхования
   */
  private Long insRuleId;

  private Long cagentId;

  private String cagentName;

  private boolean activeOnly;

  private List<String> productVersionTypes;

}
