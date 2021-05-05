package com.universalna.nsds.persistence.jpa;

import com.universalna.nsds.persistence.jpa.entity.GroupFileShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupFileShareRepository extends JpaRepository<GroupFileShareEntity, UUID> {

    Optional<GroupFileShareEntity> findByKey(UUID key);
}
