package com.universalna.nsds.service.postgres;

import com.universalna.nsds.component.PrincipalProvider;
import com.universalna.nsds.persistence.jpa.UserRepository;
import com.universalna.nsds.persistence.jpa.entity.UserProfileEntity;
import com.universalna.nsds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Service
@Transactional
public class PostgresUserService implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PrincipalProvider principalProvider;

    @Override
    public Serializable saveUserProfile(final String profileDataJson) {
        return userRepository.save(UserProfileEntity.builder()
                                                             .userId(principalProvider.getPrincipal())
                                                             .profileData(profileDataJson)
                                                     .build())
                .getProfileData();
    }

    @Override
    public Serializable getUserData() {
        return findById().getProfileData();
    }

    private UserProfileEntity findById() {
       return userRepository.findById(principalProvider.getPrincipal()).orElse(UserProfileEntity.builder().profileData("{}").build());
    }
}
