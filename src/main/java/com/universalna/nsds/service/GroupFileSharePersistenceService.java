package com.universalna.nsds.service;

import com.universalna.nsds.persistence.jpa.GroupFileShareRepository;
import com.universalna.nsds.persistence.jpa.entity.GroupFileShareEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GroupFileSharePersistenceService {

    @Autowired
    private GroupFileShareRepository groupFileShareRepository;

    public GroupFileShareEntity save(final GroupFileShareEntity groupFileShareEntity) {
        return groupFileShareRepository.save(groupFileShareEntity);
    }

    public Optional<GroupFileShareEntity> groupFileShareFindByKey(final UUID key) {
        return groupFileShareRepository.findByKey(key);
    }
}
