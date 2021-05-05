package com.universalna.nsds.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.universalna.nsds.persistence.jpa.UserRepository;
import com.universalna.nsds.persistence.jpa.entity.UserProfileEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.universalna.nsds.TestConstants.AUTHORIZATION_HEADER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UserProfileControllerIT extends AbstractIT {

    private static final String ROOT = "/user";

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUserProfile() {

        final String expectedValue = "{\"mockString\": \"mockValue\", \"mockNumber\": 1}";

        final String actualValue =
                given()
                        .port(port)
                .when()
                        .header(AUTHORIZATION_HEADER)
                        .body(expectedValue)
                        .put(ROOT)
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        assertThat(actualValue, is(expectedValue));
    }

    @Test
    void shouldGetUserProfile() throws Exception {
        final String initialJson = "{\"mockString\": \"mockValue\", \"mockNumber\": 1}";
        userRepository.save(new UserProfileEntity("user1go", initialJson));

        final String responseJson =
                given()
                        .port(port)
                .when()
                        .header(AUTHORIZATION_HEADER)
                        .body(initialJson)
                        .get(ROOT)
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        final JsonNode expectedValue = objectMapper.readTree(initialJson);
        final JsonNode actualValue = objectMapper.readTree(responseJson);
        assertThat(actualValue, is(expectedValue));
    }
}
