package com.universalna.nsds.persistence.jpa;

import com.universalna.nsds.persistence.jpa.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserProfileEntity, String> {
}
