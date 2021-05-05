package com.universalna.nsds;

import io.restassured.http.Header;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Supplier;

public final class TestConstants {

    public static final OffsetDateTime DEFAULT_DATE = OffsetDateTime.of(1970, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC);
    public static final String INTEGRATION_TEST_PRINCIPAL = "IntegrationTestPrincipal";
    public static final String JWT = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJUYjZEZW5kVmZyd1JEZ3dsQ25lX1Bva1kxb1hoR0N1VkIzZEhjeUtWYTR3In0.eyJqdGkiOiJkZWRmNzA3Mi1iZGI2LTQwNjUtYWFhMC0wM2VlYWQxYmM0MTAiLCJleHAiOjE4NTg4NDQ3NzAsIm5iZiI6MCwiaWF0IjoxNTQzNDg0NzcwLCJpc3MiOiJodHRwczovLzEwNC40MC4xODIuMTk0Ojg0NDMvYXV0aC9yZWFsbXMvRklSU1QuUkVBTE0iLCJhdWQiOlsidGVzdGNsaWVudCIsImFjY291bnQiXSwic3ViIjoiNWMxOTM3OWYtYzFjOC00OWRjLTk1NmYtMGEwMzIzZGRjN2EyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoidGVzdGNsaWVudCIsImF1dGhfdGltZSI6MTU0MzQ4NDc3MCwic2Vzc2lvbl9zdGF0ZSI6IjQyYWE1NDRkLWY1MjktNDc4Yi05ZTJkLWE4OGFmMjZiNDE3YSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7InRlc3RjbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iLCJ1bml2ZXJzYWxuYS1yb2xlIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJ1c2VyMWdvIiwiaWQiOiI1OTQyMjBmMS0zNGQxLTQ4NWMtOTQ0ZS01YjRkMTMzNTJkMTYiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMWdvIiwiZ2l2ZW5fbmFtZSI6InVzZXIxZ28ifQ.SXF2zkrVdUl-EK5aLQiqz22V1pAlwiMR-XeRREW7-MV6nKTbankt2-NHk9P_OrnYJ3f-D_5Mc-oUlN9IQkcYERUjKOl2ufi-e6CAmFrrumzgHMAbyFMBv7YYNFJM3zQpzakgrYU6S2eKrhbWf8ShJgB23ozGi4w4ujS4cZN6mK2wx2VUt4iytGH65kRhrC4pCwQKENVVJ7CnrJl34SshvKdpaSYClYG4nUdd6dfFESITjnsTXq7mai4ZgwW9e9aqRk8aNSWOnJIYDSxerU2Jn0LBMTUQwlmX8cSS2fh835m9K9yJHxl4U1M2aHgKQEpHxOEeGwCP-jt8OnriVROYCQ";
    public static final Header AUTHORIZATION_HEADER = new Header("Authorization","Bearer " + JWT);
    public static final String CURRENT_SECURITY_TOKEN_PRINCIPAL = "user1go";
    private static final byte[] FILE_BYTES = new Supplier() {
        @Override
        public byte[] get() {
            try {
                return IOUtils.toByteArray(getClass().getResourceAsStream("/files/image.JPG"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }.get();

    public static final Supplier<byte[]> FILE_CONTENT = FILE_BYTES::clone;

    private TestConstants() {
    }
}
