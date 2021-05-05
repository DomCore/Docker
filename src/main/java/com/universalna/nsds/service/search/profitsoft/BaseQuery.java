package com.universalna.nsds.service.search.profitsoft;

import lombok.Data;

@Data
public class BaseQuery {

  public static final int LIST_STANDARD_SIZE = 20;

  private int from;
  private int size;

  private String sortField;
  private boolean asc = true;

}
