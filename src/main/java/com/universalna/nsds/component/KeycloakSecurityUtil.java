package com.universalna.nsds.component;

import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class KeycloakSecurityUtil implements SecurityUtil {

    private static final String RESOURCE_NAME = "nsds";

    @Override
    public boolean hasAccess(final String... requiredRoles) {
        return getAuthentication()
                .map(authentication -> (SimpleKeycloakAccount) authentication.getDetails())
                .map(SimpleKeycloakAccount::getKeycloakSecurityContext)
                .map(RefreshableKeycloakSecurityContext::getToken)
                .map(AccessToken::getResourceAccess)
                .map(r -> r.get(RESOURCE_NAME))
                .filter(a -> Arrays.stream(requiredRoles).anyMatch(a::isUserInRole))
                .isPresent();
    }

    private Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication);
    }
}
