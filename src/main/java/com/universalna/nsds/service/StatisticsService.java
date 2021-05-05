package com.universalna.nsds.service;

import com.universalna.nsds.model.FileUploadStatistics;

import java.time.OffsetDateTime;

public interface StatisticsService {

    FileUploadStatistics getUploadStatistics(OffsetDateTime from, OffsetDateTime to, final boolean showMetadata, final boolean countUniqueFiles, final boolean showStatisticsByRelations, final boolean includeDates);

}
