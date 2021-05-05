package com.universalna.nsds.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "Контроллер для проверки работоспособности приложения")
public class HealthController {

    @GetMapping(value = "/health")
    @ApiOperation(value = "Метод для проверки запущно ли приложение")
    public ResponseEntity<Void> check() {
        return ResponseEntity.ok().build();
    }
}
