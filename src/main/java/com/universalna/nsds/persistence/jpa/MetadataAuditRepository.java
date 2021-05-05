package com.universalna.nsds.persistence.jpa;

import com.universalna.nsds.model.Relation;
import com.universalna.nsds.persistence.jpa.entity.MetadataAuditEntity;
import com.universalna.nsds.persistence.jpa.entity.MetadataAuditPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MetadataAuditRepository extends JpaRepository<MetadataAuditEntity, MetadataAuditPK> {

    Collection<MetadataAuditEntity> findAllById(UUID id);

    Optional<MetadataAuditEntity> findByIdAndRevision(UUID id, Long revision);

    Collection<MetadataAuditEntity> findAllByRelationAndRelationId(@Param("relation") Relation relation,
                                                                   @Param("relationId") String relationId);
}
