package com.universalna.nsds.service.search.profitsoft;

import com.universalna.nsds.config.ApplicationConfigurationProperties;
import com.universalna.nsds.exception.IoExceptionHandler;
import com.universalna.nsds.model.FileTagWIthOrder;
import com.universalna.nsds.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BoSearcherDictionaryService implements DictionaryService, IoExceptionHandler {

    @Autowired
    private ApplicationConfigurationProperties applicationConfigurationProperties;

    @Override
    public Set<FileTagWIthOrder> getDefaultFileTags() {
        return applicationConfigurationProperties.getTag();
    }
}
