package com.universalna.nsds.persistence.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@RedisHash("searcher")
public class SearcherLogEntry {

    private String id;

    private Long noticeId;

    private Long insuranceCaseId;

    private OffsetDateTime createdAt;

    private OffsetDateTime insuranceCaseIdReceivedAt;

}
