package com.universalna.nsds.component;

public interface SecurityUtil {

    boolean hasAccess(final String... requiredRoles);

}
