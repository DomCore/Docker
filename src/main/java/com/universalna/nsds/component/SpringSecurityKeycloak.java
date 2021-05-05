package com.universalna.nsds.component;

import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.representations.JsonWebToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityKeycloak implements PrincipalProvider, AuthorizedPartyProvider {

    private static final String NOT_PRESENT = "not present";

    @Override
    public String getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .map(String::valueOf)
                .orElse(NOT_PRESENT);
    }

    @Override
    public String getAuthorizedParty() {
        return getAuthentication()
                .map(authentication -> (SimpleKeycloakAccount) authentication.getDetails())
                .map(SimpleKeycloakAccount::getKeycloakSecurityContext)
                .map(RefreshableKeycloakSecurityContext::getToken)
                .map(JsonWebToken::getIssuedFor)
                .orElse(NOT_PRESENT);
    }

    private Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication);
    }
}
