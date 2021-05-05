package com.universalna.nsds.controller;

import com.universalna.nsds.model.FileTagWIthOrder;
import com.universalna.nsds.service.DictionaryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static com.universalna.nsds.controller.DictionaryController.ROOT;

@RestController(ROOT)
public class DictionaryController {

    static final String ROOT = "/dictionary";

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping(ROOT + "/file/tags")
    @ApiOperation(value = "Метод для получения списка стандартных тегов для файлов")
    public ResponseEntity<Set<FileTagWIthOrder>> getDefaultFileTags() {
        return ResponseEntity.ok(dictionaryService.getDefaultFileTags());
    }
}
