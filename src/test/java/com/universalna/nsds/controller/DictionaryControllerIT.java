package com.universalna.nsds.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.universalna.nsds.controller.dto.FileTagDTO;
import com.universalna.nsds.model.InsuranceTypeDictionary;
import com.universalna.nsds.model.SettlementConfigurationDictionary;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static com.universalna.nsds.TestConstants.AUTHORIZATION_HEADER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DictionaryControllerIT extends AbstractIT {

    private static final String ROOT = "/dictionary";

    @Test
    @Disabled
    void shouldReturnInsuranceTypes() throws Exception {
        final String responseJson =
        given()
                .port(port)
        .when()
                .header(AUTHORIZATION_HEADER)
                .get(ROOT + "/insurance/types")
        .then()
                .statusCode(200)
        .extract()
                .response().asString();

        final Collection<InsuranceTypeDictionary> expectedValue = Arrays.asList(
                InsuranceTypeDictionary.builder()
                                                    .id(991L)
//                                                    .code("osago")
//                                                    .nameShort("ОЦВВЛТЗ")
                        .build(),
                InsuranceTypeDictionary.builder()
                                                    .id(3889L)
//                                                    .code("dms")
//                                                    .nameShort("МЕДИЧНЕ")
                        .build(),
                InsuranceTypeDictionary.builder()
                                                    .id(3891L)
//                                                    .code("mpp")
//                                                    .nameShort("МЕДВИТРАТИ")
                        .build()
        );

        final Collection<InsuranceTypeDictionary> actualValue = objectMapper.readValue(responseJson, new TypeReference<>() {});

        assertThat(actualValue, is(expectedValue));
    }

    @Test
    @Disabled
    void shouldReturnSettlementConfigurations() throws Exception {
        final String responseJson =
                given()
                        .port(port)
                .when()
                        .header(AUTHORIZATION_HEADER)
                        .get(ROOT + "/settlement/configurations")
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        final Collection<SettlementConfigurationDictionary> expectedValue = Arrays.asList(
                SettlementConfigurationDictionary.builder()
                        .id(3L)
                        .name("КАСКО")
                        .build(),
                SettlementConfigurationDictionary.builder()
                        .id(7L)
                        .name("МАЙНО (FLEXA)")
                        .build()
        );

        final Collection<SettlementConfigurationDictionary> actualValue = objectMapper.readValue(responseJson, new TypeReference<>() {});

        assertThat(actualValue, is(expectedValue));
    }

    @Test
    void shouldReturnDefaultFileTags() throws Exception {
        final String responseJson =
                given()
                        .port(port)
                .when()
                        .header(AUTHORIZATION_HEADER)
                        .get(ROOT + "/file/tags")
                .then()
                        .statusCode(200)
                .extract()
                        .response().asString();

        final Collection<FileTagDTO> expectedValue = Arrays.asList(
                FileTagDTO.builder()
                        .tag("Cкан-копія коду ІНН")
                        .build(),
                FileTagDTO.builder()
                        .tag("Картка Кредобонус")
                        .build()
        );

        final Collection<FileTagDTO> actualValue = objectMapper.readValue(responseJson, new TypeReference<>() {});

        assertThat(actualValue, is(expectedValue));
    }
}