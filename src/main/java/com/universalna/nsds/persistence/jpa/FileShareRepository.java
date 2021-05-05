package com.universalna.nsds.persistence.jpa;

import com.universalna.nsds.persistence.jpa.entity.FileShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileShareRepository extends JpaRepository<FileShareEntity, UUID> {

    Optional<FileShareEntity> findByKey(UUID key);
}
