package com.universalna.nsds.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universalna.nsds.exception.IoExceptionHandler;
import com.universalna.nsds.persistence.redis.Token;
import com.universalna.nsds.persistence.redis.TokenRepository;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MINUTES;

@Component
@Profile("!IT")
public class AzureOauth2TokenKeeper implements TokenKeeper, IoExceptionHandler {

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenRepository tokenRepository;

    @Value("${azure.oauth2.clientId}")
    private String clientId;

    @Value("${azure.oauth2.clientSecret}")
    private String clientSecret;

    @Value("${azure.oauth2.scope}")
    private String scope;

    @Value("${azure.oauth2.username}")
    private String username;

    @Value("${azure.oauth2.password}")
    private String password;

    public String getToken() {
        final Token existingToken = tokenRepository.findById("token").orElseGet(this::login);
        final Token token = loginOrRefresh(existingToken);

        return token.getAccessToken();
    }

    private Token loginOrRefresh(final Token token) {
        final boolean accessTokenAlive = validateAccessToken(token);
        if (accessTokenAlive) {
            return token;
        } else {
            return refresh(token.getRefreshToken());
        }
    }

    private boolean validateAccessToken(final Token token) {
        if (token == null || StringUtils.isBlank(token.getAccessToken())) {
            return false;
        }
        final String[] split_string = token.getAccessToken().split("\\.");
        final String base64EncodedBody = split_string[1];
        final Base64 base64Url = new Base64(true);
        final String body = new String(base64Url.decode(base64EncodedBody));
        final String expirationDateInSeconds = tryIoOperation(() -> objectMapper.readTree(body).get("exp")).asText();
        final OffsetDateTime expirationDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(expirationDateInSeconds)), UTC);
        return OffsetDateTime.now(UTC).isBefore(expirationDate.minus(2, MINUTES));
    }

    private Token login() {
        final RequestBody requestBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("scope", scope)
                .add("grant_type", "password")
                .add("username", username)
                .add("password", password)
                .build();

        final Request request = new Request.Builder()
                .url("https://login.microsoftonline.com/organizations/oauth2/v2.0/token")
                .post(requestBody)
                .build();
        final Response response = tryIoOperation(() -> okHttpClient.newCall(request).execute());
        final String jsonResponse = tryIoOperation(() -> response.body().string());
        final Token token = tryIoOperation(() -> objectMapper.readValue(jsonResponse, Token.class));
        token.setId("token");
        return tokenRepository.save(token);
    }

    private Token refresh(final String refreshToken) {
        final RequestBody requestBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .add("username", username)
                .add("password", password)
                .build();

        final Request request = new Request.Builder().url("https://login.microsoftonline.com/organizations/oauth2/v2.0/token")
                .post(requestBody)
                .build();
        final Response response = tryIoOperation(() -> okHttpClient.newCall(request).execute());
        final String jsonResponse = tryIoOperation(() -> response.body().string());
        final Token token = tryIoOperation(() -> objectMapper.readValue(jsonResponse, Token.class));

        final boolean receivedNewAccessToken = StringUtils.isNotBlank(token.getAccessToken());
        if (receivedNewAccessToken) {
            token.setId("token");
            return tokenRepository.save(token);
        } else {
            return login();
        }
    }
}
