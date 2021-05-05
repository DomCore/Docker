package com.universalna.nsds.controller.dto;

import com.universalna.nsds.model.Origin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class TempFileMetadataDTO {

    @NotBlank
    @Size(max = 230)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull
    private Origin origin;

    @Size(max = 230)
    private String documentType;

    @Size(max = 230)
    private String documentId;

    private Set<FileTagDTO> tags;

}
