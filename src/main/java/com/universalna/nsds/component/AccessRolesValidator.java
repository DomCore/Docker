package com.universalna.nsds.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccessRolesValidator {

//    TODO: made for integration tests, find better solution
    @Autowired
    private SecurityUtil securityUtil;

    public boolean hasAccess(final String... requiredRoles) {
        return securityUtil.hasAccess(requiredRoles);
    }

}
