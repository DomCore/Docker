package com.universalna.nsds.service.search.profitsoft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class IdentifiedName {

  public static final String ID = "id";
  public static final String NAME = "name";

  private Long id;

  private String name;

  /**
   * Конвертирует объект в Map с ключами id и name.
   */
  public Map<String, Object> toMap() {
    return toIdNameMap(this.getId(), this.getName());
  }

  public static Map<String, Object> toIdNameMap(Long id, String name) {
    Map<String, Object> result = new HashMap<>();
    result.put(IdentifiedName.ID, id);
    result.put(IdentifiedName.NAME, name);
    return result;
  }

  public static String getNameOrNull(IdentifiedName in) {
    return in != null ? in.getName() : null;
  }

  /**
   * Обновляет in.name из аналогичного объекта (с таким же id) в namesMap, если он там есть.
   */
  public static void updateName(IdentifiedName in, Map<Long, IdentifiedName> namesMap) {
    if (in != null && in.getId() != null) {
      String name = getNameOrNull(namesMap.get(in.getId()));
      if (name != null) {
        in.setName(name);
      }
    }
  }

  public static IdentifiedName getFromMap(Object idNameMap) {
    Map<String, Object> map = (Map<String, Object>) idNameMap;
    if (map != null) {
      Number id = (Number) map.get(IdentifiedName.ID);
      String name = (String) map.get(IdentifiedName.NAME);
      if (id != null || StringUtils.isNotBlank(name)) {
        return new IdentifiedName(id.longValue(), name);
      }
    }
    return null;
  }

}
