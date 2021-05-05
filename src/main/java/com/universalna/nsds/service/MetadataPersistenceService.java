package com.universalna.nsds.service;

import com.universalna.nsds.model.Relation;
import com.universalna.nsds.model.Status;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MetadataPersistenceService {

    @Autowired
    private MetadataRepository metadataRepository;

    @Transactional
    public MetadataEntity save(final MetadataEntity metadataEntity) {
        return metadataRepository.save(metadataEntity);
    }

    @Transactional
    public Collection<MetadataEntity> saveAll(final Collection<MetadataEntity> metadataEntity) {
        return metadataRepository.saveAll(metadataEntity);
    }

    @Transactional(readOnly = true)
    public Optional<MetadataEntity> findById(final UUID id) {
        return metadataRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MetadataEntity> findAll() {
        return metadataRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Collection<MetadataEntity> findAll(final Example<MetadataEntity> example) {
        return metadataRepository.findAll(example);
    }

    @Transactional(readOnly = true)
    public List<MetadataEntity> findAllById(final Collection<UUID> ids) {
        return metadataRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Collection<MetadataEntity> findAllByRelationAndRelationId(final Relation relation, final String relationId) {
        return metadataRepository.findAllByRelationAndRelationId(relation, relationId);
    }

    @Transactional
    public void deleteById(final UUID id) {
        metadataRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean metadataExistsByIdAndStatus(final UUID id, final Status status) {
        return metadataRepository.existsByIdAndStatus(id, status);
    }

    @Transactional(readOnly = true)
    public boolean metadataExistsById(final UUID id) {
        return metadataRepository.existsById(id);
    }

}
