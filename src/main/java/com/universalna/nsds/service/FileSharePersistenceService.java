package com.universalna.nsds.service;

import com.universalna.nsds.persistence.jpa.FileShareRepository;
import com.universalna.nsds.persistence.jpa.entity.FileShareEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FileSharePersistenceService {

    @Autowired
    private FileShareRepository fileShareRepository;

    public FileShareEntity save(final FileShareEntity fileShareEntity) {
        return fileShareRepository.save(fileShareEntity);
    }

    public Optional<FileShareEntity> findById(final UUID id) {
        return fileShareRepository.findById(id);
    }

    public Optional<FileShareEntity> findByKey(final UUID id) {
        return fileShareRepository.findByKey(id);
    }
}
