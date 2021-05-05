package com.universalna.nsds.service;

import com.universalna.nsds.model.FileTagWIthOrder;

import java.util.Set;

public interface DictionaryService {

    Set<FileTagWIthOrder> getDefaultFileTags();
}
