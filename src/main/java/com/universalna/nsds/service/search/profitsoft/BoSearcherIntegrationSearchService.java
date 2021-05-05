package com.universalna.nsds.service.search.profitsoft;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.exception.IoExceptionHandler;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BoSearcherIntegrationSearchService implements ProfitsoftSearchService, IoExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoSearcherIntegrationSearchService.class);

    @Value("${bo.searcher.uri}")
    private String BO_SEARCHER_URI;

    @Value("${bo.searcher.api.uri}")
    private String BO_SEARCHER_INSURANCE_CASES_API;

    @Value("${bo.searcher.api.search.insurance.case.by.notification.id}")
    private String BO_SEARCHER_INSURANCE_CASE_BY_SETTLEMENT_NOTIFICATION_ID;

    @Value("${bo.searcher.api.search.settlement.case.by.id}")
    private String BO_SEARCHER_SETTLEMENT_CASE_BY_SETTLEMENT_CASE_ID;

    @Value("${bo.searcher.api.contract.search}")
    private String BO_SEARCHER_CONTRACTS_SEARCH_API;

    @Autowired
    private OkHttpClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UUIDGenerator uuidGenerator;

    @Override
    public Page<ClaimInfoDto> listClaimInfos(ClaimInfoQueryDto queryDto) {
        return searchInProfitsoft(BO_SEARCHER_INSURANCE_CASES_API, queryDto, new TypeReference<>() {});
    }

    @Override
    public Page<ContractInfoDto> searchContracts(final ContractInfoQueryDto queryDto) {
        return searchInProfitsoft(BO_SEARCHER_CONTRACTS_SEARCH_API, queryDto, new TypeReference<>() {});
    }

    @Override
    public ClaimInfoDto getByNoticeId(final Long id) {
        final String endpoint = BO_SEARCHER_INSURANCE_CASE_BY_SETTLEMENT_NOTIFICATION_ID.replace("{noticeId}", id.toString());
        return searchInProfitsoft(endpoint);
    }

    @Override
    public ClaimInfoDto getBySettlementCaseId(final Long id) {
        final String endpoint = BO_SEARCHER_SETTLEMENT_CASE_BY_SETTLEMENT_CASE_ID.replace("{settlementCaseId}", id.toString());
        return searchInProfitsoft(endpoint);
    }

    private ClaimInfoDto searchInProfitsoft(final String endpoint) {
        final UUID logId = uuidGenerator.generate();
        LOGGER.info("ID: {} , endpoint: {} , Searcher request body: {}", logId, endpoint, null);
        final String url = BO_SEARCHER_URI + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        Response response = tryIoOperation(call::execute);
        final String responseJson = tryIoOperation(() -> response.body().string());
        LOGGER.info("ID: {} Searcher response body: {}", logId, responseJson);
        return tryIoOperation(() -> objectMapper.readValue(responseJson, ClaimInfoDto.class));
    }

    private <T> T searchInProfitsoft(final String apiEndpoint, final Object requestObject, TypeReference<T> typeReference) {
        final UUID logId = uuidGenerator.generate();
        LOGGER.info("ID: {} , endpoint: {} , Searcher request body: {}", logId, apiEndpoint, requestObject);
        final String requestJson = tryIoOperation(() -> objectMapper.writeValueAsString(requestObject));
        Request request = new Request.Builder()
                .url(BO_SEARCHER_URI + apiEndpoint)
                .post(RequestBody.create(MediaType.get("application/json"), requestJson))
                .build();

        Call call = client.newCall(request);
        Response response = tryIoOperation(call::execute);
        final String responseJson = tryIoOperation(() -> response.body().string());
        LOGGER.info("ID: {} Searcher response body: {}", logId, responseJson);
        return tryIoOperation(() -> objectMapper.readValue(responseJson, typeReference));
    }
}
