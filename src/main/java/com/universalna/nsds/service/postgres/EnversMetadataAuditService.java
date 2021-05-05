package com.universalna.nsds.service.postgres;

import com.universalna.nsds.exception.NotFoundException;
import com.universalna.nsds.model.MetadataAudit;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.persistence.jpa.MetadataAuditRepository;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataAuditEntity;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.service.Mapper;
import com.universalna.nsds.service.MetadataAuditService;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnversMetadataAuditService implements MetadataAuditService {

    @Autowired
    private MetadataAuditRepository auditRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private Mapper auditMapper;

    @Override
    public List<MetadataAudit> getMetadataVersionsSorted(final UUID id) {
        return auditRepository.findAllById(id).stream()
                .map(auditMapper::toModel)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    @Override
    public List<MetadataAudit> getMetadataVersionsByRelation(final Relation relation, final String relationId, final LocalDate from, final LocalDate to, final RevisionType action) {
        final OffsetDateTime fromOffset = from == null ? null : OffsetDateTime.of(from, LocalTime.MIN, ZoneOffset.from(OffsetDateTime.now()));
        final OffsetDateTime toOffset = to ==     null ? null : OffsetDateTime.of(to,   LocalTime.MAX, ZoneOffset.from(OffsetDateTime.now()));
        return auditRepository.findAllByRelationAndRelationId(relation, relationId)
                .stream()
                .filter(m -> fromOffset == null || (m.getRevisionEndTimestamp() == null ? OffsetDateTime.now().isAfter(fromOffset) : m.getRevisionEndTimestamp().isAfter(fromOffset)))
                .filter(m -> toOffset   == null || (m.getRevisionEndTimestamp() == null ? OffsetDateTime.now().isBefore(toOffset)  : m.getRevisionEndTimestamp().isBefore(toOffset)))
                .filter(m -> action == null || m.getActionType() == action)
                .map(auditMapper::toModel)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    @Override
    public void revert(final UUID id, final Long revisionNumber) {
        final MetadataAuditEntity revision = auditRepository.findByIdAndRevision(id, revisionNumber)
                .orElseThrow(() -> new NotFoundException("revision not found"));
        final MetadataEntity metadataEntity = auditMapper.toEntity(revision);
        metadataRepository.save(metadataEntity);
    }
}
