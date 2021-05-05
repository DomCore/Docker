package com.universalna.nsds.service.postgres;

import com.universalna.nsds.exception.NotFoundException;
import com.universalna.nsds.model.Metadata;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.service.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Deprecated
public class TransactionalReadOnlyMetadataService {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private Mapper mapper;

    @Transactional(readOnly = true)
    public Metadata getMetadata(final String fileId) {
        final MetadataEntity metadataEntity = metadataRepository.findById(UUID.fromString(fileId))
                .orElseThrow(() -> new NotFoundException("File not found"));
        return mapper.toModel(metadataEntity);
    }
}
