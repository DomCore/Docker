package com.universalna.nsds.service.search.profitsoft;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ContractInfoQueryDto extends ContractSuggestQueryDto {

  /**
   * Идентификатор Конфигурации продукта
   */
  private Long productVersionId;

  /**
   * Идентификатор Тарифного плана
   */
  private Long tarifPlanId;

  /**
   * Идентификатор Серии бланка
   */
  private Long blankSerieId;

  /**
   * Идентификатор Серии бланка сертификата/стикера/карты партнера
   */
  private Long certificateBlankSerieId;

  /**
   * Номер сертификата/стикера/карты партнера или его фрагмент
   */
  private String certificateNumber;

  /**
   * ФИО/Название организации - клиента
   */
  private String clientName;

  private String clientPhone;

  private String insObjectSummary;

  private String insPersonName;

  private List<Long> divisionIds;

  private Long acquisitorId;

  /**
   * Будут отбираться договора, в которых либо аквизитор, либо менеджер с указанным ID
   */
  private Long acquisitorOrManagerId;

  private Long managerId;

  private Long accepterId;

  private Long partnerDivisionId;

  private List<Long> saleChannelIds;

  private List<String> statuses;

  private String activationStatus;

  private List<String> signs;

  private String originalContractBlankSerie;

  private String originalContractNumber;

  private Boolean contractCommissionChargeIncludingFlag;

  private RangeDto<LocalDate> signDate;
  private RangeDto<LocalDate> contractBeginDate;
  private RangeDto<LocalDate> contractEndDate;
  private RangeDto<LocalDate> activateDate;
  private RangeDto<LocalDate> createDate;
  private RangeDto<Double> totalPremie;
  private Boolean multiplePremieRecords;

  /**
   * Дата очередного платежа
   */
  private RangeDto<LocalDate> regularPremieRecordDate;

  /**
   * Сумма очередного платежа
   */
  private RangeDto<Double> regularPremieRecordValue;


  private Long importProtocolId;

  private ContractSecuritySpec securitySpec;

  private int from;
  private int size;

  private String sortField;
  private boolean asc = true;

}
