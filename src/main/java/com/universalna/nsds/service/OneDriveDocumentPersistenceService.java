package com.universalna.nsds.service;

import com.universalna.nsds.persistence.jpa.OneDriveDocumentRepository;
import com.universalna.nsds.persistence.jpa.entity.OneDriveDocumentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OneDriveDocumentPersistenceService {

    @Autowired
    private OneDriveDocumentRepository oneDriveDocumentRepository;

    public Optional<OneDriveDocumentEntity> findById(final UUID id) {
        return oneDriveDocumentRepository.findById(id);
    }

    public OneDriveDocumentEntity save(final OneDriveDocumentEntity OneDriveDocumentEntity) {
        return oneDriveDocumentRepository.save(OneDriveDocumentEntity);
    }

    public Collection<OneDriveDocumentEntity> findAll() {
        return oneDriveDocumentRepository.findAll();
    }

    public void deleteById(final UUID id) {
        oneDriveDocumentRepository.deleteById(id);
    }
}
