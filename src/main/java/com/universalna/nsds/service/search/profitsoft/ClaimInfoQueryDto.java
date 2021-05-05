package com.universalna.nsds.service.search.profitsoft;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ClaimInfoQueryDto extends BaseQuery {

  /**
   * Номер договора. Целиком, либо начало фрагмента номера.
   * Может указывать несколько фрагментов через пробел.
   */
  private String contractNumber;

  /**
   * Номер Сообщнния об убытке (по полному совпадению)
   */
  private String noticeNumber;

  /**
   * Диапазон дат, в котором создано Сообщение об убытке.
   */
  private RangeDto<LocalDateTime> noticeCreateDate;

  /**
   * Номер Страхового дела (по полному совпадению)
   */
  private String settlementCaseNumber;

  /**
   * Список статусов страховых дел.
   */
  private List<String> statuses;

  /**
   * Диапазон дат, в котором произошло происшествие.
   */
  private RangeDto<LocalDateTime> claimDate;

  /**
   * Диапазон дат, в котором создано страховое дело.
   */
  private RangeDto<LocalDateTime> settlementCaseCreateDate;

  /**
   * Идентификатор карточки контрагента - урегулировщика, на котором дело
   */
  private Long responsibleId;

  /**
   * Идентификаторы карточек подразделений, за которыми закреплены СД, а если нет СД, то СУ.
   */
  private List<Long> divisionIds;

  private List<Long> settlementConfigurationIds;

  private List<Long> insRuleIds;

  private String clientName;

  private String injuredName;

  /**
   * ИНН/ЕГРПОУ Страхователя либо Потерпевшего
   */
  private String inn;

  private String insObject;

  private String injuredObject;

  /**
   * Позволяет управлять некоторым атрибутами, которые будут приходить или не приходить в ответе.
   * Список потерпевших по умолчанию выключен.
   */
  private ClaimSelectSpec select = new ClaimSelectSpec(false);

}
