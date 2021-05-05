package com.universalna.nsds.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractControllerTest {

    @Autowired
    protected ObjectMapper objectMapper;

}
