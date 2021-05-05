package com.universalna.nsds.service.search.profitsoft;

import lombok.Data;

/**
 * Данные Застрахованного или Поврежденного объекта
 */
@Data
public class ObjectDto {

  private Long id;

  private String code;

  private String caption;

  private String vin;

}
