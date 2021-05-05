package com.universalna.nsds.service;

import com.universalna.nsds.model.MetadataAudit;
import com.universalna.nsds.model.Relation;
import org.hibernate.envers.RevisionType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MetadataAuditService {

    List<MetadataAudit> getMetadataVersionsSorted(UUID id);

    void revert(UUID id, Long revisionNumber);

    List<MetadataAudit> getMetadataVersionsByRelation(Relation relation, String relationId, LocalDate from, LocalDate to, RevisionType action);
}
