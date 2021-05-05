package com.universalna.nsds.component;

import org.springframework.stereotype.Component;

@Component
public class MockTokenKeeper implements TokenKeeper {

    @Override
    public String getToken() {
        return "mockTestToken";
    }
}
