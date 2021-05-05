package com.universalna.nsds.service.postgres;

import com.universalna.nsds.model.FileUploadStatistics;
import com.universalna.nsds.persistence.jpa.MetadataRepository;
import com.universalna.nsds.persistence.jpa.entity.MetadataEntity;
import com.universalna.nsds.service.Mapper;
import com.universalna.nsds.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostgresStatisticsService implements StatisticsService {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private Mapper mapper;

    @Override
    public FileUploadStatistics getUploadStatistics(final OffsetDateTime from, final OffsetDateTime to, final boolean showMetadata, final boolean countUniqueFiles, final boolean showStatisticsByRelations, final boolean includeDates) {
        final Collection<MetadataEntity> allMetadataEntities = metadataRepository.findAllByTimestampAfterAndTimestampBefore(from, to);
        final Collection<MetadataEntity> uniqueMetadataEntities = allMetadataEntities.stream().filter(distinctByKey(m-> m.getFileStorageFileId() + m.getUploaderId())).collect(Collectors.toList());
        return FileUploadStatistics.builder()
                .from(includeDates ? from : null)
                .to(includeDates   ? to   : null)
                .metadataCounter((long) allMetadataEntities.size())
                .filesCounter(countUniqueFiles ? (long) uniqueMetadataEntities.size() : null)
                .relations(showStatisticsByRelations ? uniqueMetadataEntities.stream().collect(Collectors.groupingBy(m -> m.getRelation() + ":" + m.getRelationId(), Collectors.counting())) : null)
                .metadata(showMetadata ? uniqueMetadataEntities.stream().map(mapper::toModel).collect(Collectors.toList()): null)
                .build();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
