package com.universalna.nsds.service.search.profitsoft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Спецификация полей, которые могут приходить либо не приходить в ответе на запрос списка claims.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSelectSpec {

  public static final ClaimSelectSpec ALL_ATTRIBUTES = new ClaimSelectSpec(true);

  /**
   * Задает, будет ли приходить в ответе список потерпевших (injureds).
   */
  private boolean injureds;

}
