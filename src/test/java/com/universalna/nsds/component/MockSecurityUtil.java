package com.universalna.nsds.component;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockSecurityUtil implements SecurityUtil {

    public boolean hasAccess(final String... requiredRoles) {
        return true;
    }

}
