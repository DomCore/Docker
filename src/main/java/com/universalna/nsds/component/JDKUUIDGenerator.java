package com.universalna.nsds.component;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JDKUUIDGenerator implements UUIDGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
