package com.universalna.nsds.service.search.profitsoft;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

/**
 * Данные потерпевшего в {@link ClaimInfoDto}
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InjuredDto {

  /**
   * Идентификатор Потерпевшего в СД.
   */
  private Long id;

  /**
   * ФИО/Названия организации Потерпевшего.
   */
  private String name;

  /**
   * ИНН/ЕГРПОУ Потерпевшего
   */
  private String inn;

  /**
   * Статус Потерпевшего в СД.
   */
  private String status;

  /**
   * Дата страхового события
   */
  private String claimDate;

  /**
   * Идентификатор карточки контрагента - Потерпевшего.
   */
  private Long cagentId;

  /**
   * Вид страхования, к которому относится Потерпевший.
   */
  private IdentifiedName insRule;

  /**
   * Риск (интерфейсный), к которому относится Потерпевший.
   */
  private IdentifiedName insRisk;

  /**
   * Конфигурация урегулирования, к которому относится Потерпевший.
   */
  private IdentifiedName settlementConfiguration;

  /**
   * Идентификатор договора, к которому относится Потерпевший.
   */
  private Long contractId;

  /**
   * Номер договора, к которому относится Потерпевший.
   */
  private String contractNumber;

  /**
   * ФИО страхователя из договора, к которому относится Потерпевший.
   */
  private String clientName;

  /**
   * ИНН/ЕГРПОУ страхователя из договора, к которому относится Потерпевший.
   */
  private String clientInn;

  /**
   * Название (caption) застрахованного объекта
   */
  private ObjectDto insObject;

  /**
   * Название (caption) поврежденного объекта (если не совпадает с застрахованным)
   */
  private ObjectDto injuredObject;

  @JsonAnySetter
  private Map<String, Object> specificFields;

  @JsonAnyGetter
  public Map<String, Object> getSpecificFields() {
    return specificFields;
  }
}
