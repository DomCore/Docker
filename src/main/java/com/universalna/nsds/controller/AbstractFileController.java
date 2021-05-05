package com.universalna.nsds.controller;

import com.universalna.nsds.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import static com.universalna.nsds.controller.AbstractFileController.ROOT;
import static com.universalna.nsds.controller.PathConstants.FILES;

@RestController(ROOT)
class AbstractFileController {

    static final String ROOT = FILES;

    @Autowired
    protected FileService fileService;

}
