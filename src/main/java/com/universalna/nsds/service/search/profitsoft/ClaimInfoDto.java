package com.universalna.nsds.service.search.profitsoft;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Краткие данные по Требованию на возмещение убытка: содержит информацию по СД и СУ.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClaimInfoDto {

  /**
   * Идентификатор Страхового дела.
   * Может быть null, если есть только СУ, но нет СД.
   */
  private Long settlementCaseId;

  /**
   * Номер Страхового дела
   */
  private String settlementCaseNumber;

  /**
   * Статус СД
   */
  private String status;

  /**
   * Дата и время создания СД
   */
  private String settlementCaseCreateDate;

  /**
   * Индентификатор Сообщения об убытке (СУ).
   * Если страхового дела нет, то является идентификатором Требования на возмещение убытка.
   * Может быть null (если СД создано без СУ), а также может быть несколько Требований на возмещение с одним noticeId,
   * но в этом случае у каждого из них будет свой уникальный settlementCaseId. Если settlementCaseId - null, то noticeId - уникальное.
   */
  private Long noticeId;

  /**
   * Номер СУ
   */
  private String noticeNumber;

  /**
   * Статус СУ. Полный перечень возможных статусов см. в проекте BACK-OFFICE, класс
   * ua.com.profitsoft.bo.settlement.privatbank.dict.NoticeStatus
   */
  private String noticeStatus;

  /**
   * Дата и время принятия СУ.
   * В формате yyyy-MM-ddTHH:mm:ssZ
   */
  private String noticeReceiveDate;

  /**
   * Дата и время создания СУ.
   * В формате yyyy-MM-ddTHH:mm:ssZ
   */
  private String noticeCreateDate;

  /**
   * Характер происшествия, указанный с СУ
   */
  private String noticeDescription;

  /**
   * Указывает на то, что СУ имеет признаки попытки мошенничества
   */
  private Boolean swindle;

  /**
   * Дата и время происшествия (берется из СД, но если нет СД, то из СУ)
   */
  private String claimDate;

  /**
   * Пользователь, которому на данный момент назначено СД.
   * ИД карточки контрагента и ФИО краткое.
   */
  private CagentDto responsible;

  /**
   * Подразделение, к которму относится СД либо СУ (если на базе СУ без СД)
   */
  private IdentifiedName division;

  /**
   * Вид страхования, к которму относится СД либо СУ (если на базе СУ без СД)
   * Если в СД несколько потерпевших, и у них разные Виды страхования, то поле будет пустое.
   */
  private IdentifiedName insRule;

  /**
   * Риск (интерфейсный), к которму относится СД либо СУ (если на базе СУ без СД)
   * Если в СД несколько потерпевших, и у них разные риски, то поле будет пустое.
   */
  private IdentifiedName insRisk;

  /**
   * Конфигурация урегулирования, к которму относится СД либо СУ (если на базе СУ без СД).
   * Если в СД несколько потерпевших, и у них разные Конфигурации орегулирования, то поле будет пустое.
   */
  private IdentifiedName settlementConfiguration;

  /**
   * Идентификатор договора, к которму относится СД либо СУ (если на базе СУ без СД).
   * Если в СД несколько потерпевших, и у них разные договора, то поле будет пустое.
   */
  private Long contractId;

  /**
   * Номер договора, к которму относится СД либо СУ (если на базе СУ без СД).
   * Если в СД несколько потерпевших, и у них разные договора, то поле будет пустое.
   */
  private String contractNumber;

  /**
   * ФИО страхователя из договора, к которму относится СД либо СУ (если на базе СУ без СД).
   * Если в СД несколько потерпевших, и у них разные договора, то поле будет пустое.
   */
  private String clientName;

  /**
   * ИНН/ЕГРПОУ страхователя из договора, к которму относится СД либо СУ (если на базе СУ без СД).
   * Если в СД несколько потерпевших, и у них разные договора, то поле будет пустое.
   */
  private String clientInn;

  /**
   * Данные застрахованного объекта
   */
  private ObjectDto insObject;

  /**
   * Данные поврежденного объекта (если не совпадает с застрахованным)
   */
  private ObjectDto injuredObject;

  private List<InjuredDto> injureds;

  private List<IdentifiedName> beneficiaries;

  private LinkToCaseDto link_to_case;

  private Map<String, Object> specificFields;

  @JsonAnyGetter
  public Map<String, Object> getSpecificFields() {
    return specificFields;
  }
}
