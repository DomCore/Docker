package com.universalna.nsds.controller.dto;

import com.universalna.nsds.model.Origin;
import com.universalna.nsds.model.Relation;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MetadataWithUrlDTO {

    @NotBlank
    @Size(max = 230)
    @ApiModelProperty(value = "Имя файла", required = true, example = "image.jpg")
    private String name;

    @NotNull
    @ApiModelProperty(value = "Тип связи", required = true)
    private Relation relation;

    @NotBlank
    @ApiModelProperty(value = "ID обьекта связи", required = true)
    private String relationId;

    private String documentType;

    private String documentId;

    @Size(max = 5000)
    @ApiModelProperty(value = "Описание файла")
    private String description;

    @NotNull
    @ApiModelProperty(value = "Происхождение файла", required = true)
    private Origin origin;

    @ApiModelProperty(value = "Список тегов к файлу")
    private Set<FileTagDTO> tags;

    @NotBlank
    @ApiModelProperty(value = "Прямая ссылка на файл для закачки сервисом", required = true)
    private String url;

}
