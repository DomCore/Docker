package com.universalna.nsds.service;

import java.io.Serializable;

public interface UserService {

    Serializable saveUserProfile(String profileDataJson);

    Serializable getUserData();
}
