package com.universalna.nsds.component;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockAuthorizedPartyProvider implements AuthorizedPartyProvider {

    @Override
    public String getAuthorizedParty() {
        return "weblogin";
    }
}
