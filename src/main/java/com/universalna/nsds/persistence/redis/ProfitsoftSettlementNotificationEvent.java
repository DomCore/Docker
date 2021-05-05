package com.universalna.nsds.persistence.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@RedisHash(value = "profitsoft", timeToLive = 604800) // ttl one week
public class ProfitsoftSettlementNotificationEvent {

    private String id;
}
