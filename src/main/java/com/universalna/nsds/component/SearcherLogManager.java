package com.universalna.nsds.component;

import com.universalna.nsds.controller.amqp.InsuranceCaseCreatedEventDto;
import com.universalna.nsds.persistence.redis.SearcherLogEntry;
import com.universalna.nsds.persistence.redis.SearcherLogRepository;
import com.universalna.nsds.service.search.profitsoft.ClaimInfoDto;
import com.universalna.nsds.service.search.profitsoft.ProfitsoftSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.StreamSupport;

@Component
public class SearcherLogManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearcherLogManager.class);

    @Autowired
    private SearcherLogRepository searcherLogRepository;

    @Autowired
    private ProfitsoftSearchService profitsoftSearchService;

    public void add(final InsuranceCaseCreatedEventDto insuranceCaseCreatedEventDto) {
        final SearcherLogEntry entry = SearcherLogEntry.builder()
                .noticeId(insuranceCaseCreatedEventDto.getNoticeId())
                .createdAt(OffsetDateTime.now())
                .build();
        searcherLogRepository.save(entry);
    }

    @Scheduled(fixedDelay = 10000)
    public void checkAvailabilityOfInsuranceCaseId() {
        try {
            final Iterable<SearcherLogEntry> searcherLog = searcherLogRepository.findAll();
            StreamSupport.stream(searcherLog.spliterator(), false)
                    .filter(Objects::nonNull)
                    .filter(entry -> entry.getInsuranceCaseId() == null)
                    .map(entry -> {
                        ClaimInfoDto searchResult = profitsoftSearchService.getByNoticeId(entry.getNoticeId());
                        if (searchResult == null) {
                            LOGGER.info("checkAvailabilityOfInsuranceCaseId searchResult is null");
                            return entry;
                        }
                        Long insuranceCaseId = searchResult.getSettlementCaseId();
                        if (insuranceCaseId != null) {
                            entry.setInsuranceCaseId(insuranceCaseId);
                            entry.setInsuranceCaseIdReceivedAt(OffsetDateTime.now());
                        } else {
                            if (entry.getCreatedAt().plus(5, ChronoUnit.MINUTES).isBefore(OffsetDateTime.now())) {
                                LOGGER.debug("Searcher does not return insurance case ID for 5 or more minutes, notification ID: {}", entry.getNoticeId());
                            }
                        }
                        return entry;
                    })
                    .forEach(searcherLogRepository::save);
        } catch (Exception e) {
            LOGGER.error("Exception caught in scheduled task checkAvailabilityOfInsuranceCaseId", e);
        }
    }
}
