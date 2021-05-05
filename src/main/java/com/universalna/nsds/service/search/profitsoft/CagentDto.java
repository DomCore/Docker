package com.universalna.nsds.service.search.profitsoft;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * DTO-представление кратких данных контрагента
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class CagentDto extends IdentifiedName {

  public static final String EMAIL = "email";

  private String email;

  public CagentDto(Long id, String name, String email) {
    super(id, name);
    this.email = email;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> result = super.toMap();
    result.put(EMAIL, email);
    return result;
  }

  public static CagentDto getFromMap(Object cagentDtoMap) {
    Map<String, Object> map = (Map<String, Object>) cagentDtoMap;
    if (map != null) {
      Number id = (Number) map.get(IdentifiedName.ID);
      String name = (String) map.get(IdentifiedName.NAME);
      if (id != null || StringUtils.isNotBlank(name)) {
        return new CagentDto(id.longValue(), name, (String) map.get(EMAIL));
      }
    }
    return null;
  }

  public static void updateCagentDto(CagentDto dto, Map<Long, CagentDto> cagentDtos) {
    if (dto != null && dto.getId() != null) {
      CagentDto actualCagentDto = cagentDtos.get(dto.getId());
      if (actualCagentDto != null) {
        dto.setName(actualCagentDto.getName());
        dto.setEmail(actualCagentDto.getEmail());
      }
    }
  }

}
