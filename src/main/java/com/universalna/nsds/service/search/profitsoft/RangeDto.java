package com.universalna.nsds.service.search.profitsoft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RangeDto<T> {

  private T from;
  private T till;

}
