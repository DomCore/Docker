package com.universalna.nsds.model;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

@Data
@Builder(builderClassName = "Builder")
public class File {

	private final InputStream content;

	private final String originalName;

}
