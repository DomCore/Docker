package com.universalna.nsds.controller;

import com.universalna.nsds.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import static com.universalna.nsds.controller.UserController.ROOT;

@RestController(ROOT)
@Api(value = "Контроллер для работы с настройками сервиса пользователем")
public class UserController {

    static final String ROOT = "/user";

    @Autowired
    private UserService userService;

    @PutMapping(ROOT)
    @ApiOperation(value = "Метод для сохранения настроек пользователя")
    public ResponseEntity<Serializable> saveUserProfile(HttpServletRequest request) throws IOException {
        final String requestJson = collectInputStreamAsString(request.getInputStream());
        return ResponseEntity.ok(userService.saveUserProfile(requestJson));
    }

    @GetMapping(ROOT)
    @ApiOperation(value = "Метод для получения настроек пользователя")
    public ResponseEntity<Serializable> getUserData() {
        return ResponseEntity.ok(userService.getUserData());
    }

    private String collectInputStreamAsString(final InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
