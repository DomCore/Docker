package com.universalna.nsds.controller;

import com.universalna.nsds.component.UUIDGenerator;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.persistence.redis.ProfitsoftSettlementNotificationEvent;
import com.universalna.nsds.persistence.redis.ProfitsoftSettlementNotificationEventRepository;
import com.universalna.nsds.persistence.redis.SearcherLogEntry;
import com.universalna.nsds.persistence.redis.SearcherLogRepository;
import com.universalna.nsds.service.search.profitsoft.ProfitsoftSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.universalna.nsds.controller.DebugController.ROOT;
import static java.time.temporal.ChronoUnit.SECONDS;

@Deprecated
@Validated
@RestController(ROOT)
public class DebugController {

    static final String ROOT = "/debug";

    @Autowired
    private SearcherLogRepository searcherLogRepository;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private ProfitsoftSearchService profitsoftSearchService;

    @Autowired
    private ProfitsoftSettlementNotificationEventRepository profitsoftSettlementNotificationEventRepository;

    @Autowired
    private UUIDGenerator uuidGenerator;


    @GetMapping(ROOT)
    @PreAuthorize("@accessRolesValidator.hasAccess( \"nsds_admin\")")
    public ResponseEntity<Collection<SearcherLogEntry>> getLast500SearcherLogEntries(@RequestParam(value = "seconds", required = false) @Valid @Min(0) final Long seconds) {
        final List<SearcherLogEntry> entries;
        if (seconds != null) {
            entries = StreamSupport.stream(searcherLogRepository.findAll().spliterator(), false)
                    .filter(entry -> {
                        if (entry.getInsuranceCaseIdReceivedAt() != null) {
                           return entry.getCreatedAt().plus(seconds, SECONDS).isBefore(entry.getInsuranceCaseIdReceivedAt());
                        }
                        return true;
                    })
                    .sorted(Comparator.comparing(SearcherLogEntry::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        } else {
            entries = StreamSupport.stream(searcherLogRepository.findAll().spliterator(), false)
                    .sorted(Comparator.comparing(SearcherLogEntry::getCreatedAt).reversed())
                    .limit(500)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping(ROOT + "/events")
    @PreAuthorize("@accessRolesValidator.hasAccess( \"nsds_admin\")")
    public ResponseEntity<Iterable<ProfitsoftSettlementNotificationEvent>> getProfitsoftEvents() {
        return ResponseEntity.ok(profitsoftSettlementNotificationEventRepository.findAll());
    }

    @PostMapping(ROOT + "/files")
    @PreAuthorize("@accessRolesValidator.hasAccess( \"nsds_admin\")")
    public ResponseEntity copyInconsistentFilesToInsuranceCaseFromSettlementNotification(@RequestBody final Set<String> settlementNotificationIds){
        final List<Map.Entry<String, List<MetadataEntity>>> saved = settlementNotificationIds.stream().map(notificationId -> {
            final String insuranceCaseId = profitsoftSearchService.getByNoticeId(Long.valueOf(notificationId)).getSettlementCaseId().toString();
            final Collection<MetadataEntity> notificationFiles = metadataRepository.findAllByRelationAndRelationId(Relation.SETTLEMENT_NOTIFICATION, notificationId);
            final Collection<MetadataEntity> insuranceCaseFiles = metadataRepository.findAllByRelationAndRelationId(Relation.INSURANCE_CASE, insuranceCaseId);
            final Collection<MetadataEntity> filesToCopyToInsuranceCase = notificationFiles.stream()
                    .filter(notificationFile -> insuranceCaseFiles.stream()
                            .noneMatch(insuranceCaseFile -> notificationFile.getFileStorageFileId().equals(insuranceCaseFile.getFileStorageFileId()))
                    )
                    .map(notificationFile -> copyMetadataEntity(notificationFile, insuranceCaseId))
                    .collect(Collectors.toList());
            final List<MetadataEntity> savedNewFiles = metadataRepository.saveAll(filesToCopyToInsuranceCase);
            return Map.entry(insuranceCaseId, savedNewFiles);
        })
                .collect(Collectors.toList());
        return ResponseEntity.ok(saved);
    }

    private MetadataEntity copyMetadataEntity(final MetadataEntity source, final String copyToRelationId) {
        return MetadataEntity.metadataEntityBuilder()
                .id(uuidGenerator.generate())
                .name(source.getName())
                .size(source.getSize())
                .relation(Relation.INSURANCE_CASE)
                .relationId(copyToRelationId)
                .description(source.getDescription())
                .uploaderId(source.getUploaderId())
                .timestamp(source.getTimestamp())
                .source(source.getSource())
                .origin(source.getOrigin())
                .status(source.getStatus())
                .fileStorageFileId(source.getFileStorageFileId())
                .tags(source.getTags())
                .build();
    }
}
