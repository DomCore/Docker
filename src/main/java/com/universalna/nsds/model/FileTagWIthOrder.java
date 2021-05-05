package com.universalna.nsds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileTagWIthOrder extends FileTag {

    private Integer order;

    public FileTagWIthOrder(final String tag, final Integer order) {
        super(tag);
        this.order = order;
    }
}
