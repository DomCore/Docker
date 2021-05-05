package com.universalna.nsds.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universalna.nsds.exception.IoExceptionHandler;
import com.universalna.nsds.model.FileUploadStatistics;
import com.universalna.nsds.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;

@RestController
public class StatisticsController implements StreamingController, IoExceptionHandler {

    static final String ROOT = "/statistics";

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(ROOT + "/upload")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\")")
    public ResponseEntity<FileUploadStatistics> getUploadStatistics(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
                                                                    @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to,
                                                                    @RequestParam(value = "metadata", required = false) final boolean showMetadata,
                                                                    @RequestParam(value = "countUniqueFiles", required = false) final boolean countUniqueFiles,
                                                                    @RequestParam(value = "statisticsByRelations", required = false) final boolean showStatisticsByRelations,
                                                                    @RequestParam(value = "dates", required = false) final boolean includeDates) {
        return ResponseEntity.ok(statisticsService.getUploadStatistics(from, to, showMetadata, countUniqueFiles, showStatisticsByRelations, includeDates));
    }

    @GetMapping(ROOT + "/upload/file")
    @PreAuthorize("@accessRolesValidator.hasAccess(\"nsds_admin\")")
    public ResponseEntity<StreamingResponseBody> downloadUploadStatisticsAsJsonFile(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
                                                                                    @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to,
                                                                                    @RequestParam(value = "metadata", required = false) final boolean showMetadata,
                                                                                    @RequestParam(value = "countUniqueFiles", required = false) final boolean countUniqueFiles,
                                                                                    @RequestParam(value = "statisticsByRelations", required = false) final boolean showStatisticsByRelations,
                                                                                    @RequestParam(value = "dates", required = false) final boolean includeDates) {
        final FileUploadStatistics uploadStatistics = statisticsService.getUploadStatistics(from, to, showMetadata, countUniqueFiles, showStatisticsByRelations, includeDates);
        final byte[] content = tryIoOperation(() -> objectMapper.writeValueAsBytes(uploadStatistics));
        return streamContent(new ByteArrayInputStream(content), "statistics-" + from + "-" + to + ".json");
    }

}
