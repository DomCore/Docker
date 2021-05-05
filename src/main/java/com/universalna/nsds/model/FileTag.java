package com.universalna.nsds.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class FileTag implements Comparable<FileTag> {

    private String tag;

    @Override
    public int compareTo(final FileTag that) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.tag, that.tag);
    }
}
