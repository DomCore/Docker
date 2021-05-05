package com.universalna.nsds.persistence.jpa;

import com.universalna.nsds.persistence.jpa.entity.OneDriveDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OneDriveDocumentRepository extends JpaRepository<OneDriveDocumentEntity, UUID> {
}
