package com.universalna.nsds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
@EnableRedisRepositories(basePackages = "com.universalna.nsds.persistence.redis", enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(@Value("${spring.redis.host}") final String host,
                                                         @Value("${spring.redis.port}") final int port) {
        final RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final JedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public RedisCustomConversions redisCustomConversions(final OffsetDateTimeToBytesConverter offsetToBytes,
                                                         final BytesToOffsetDateTimeConverter bytesToOffset) {
        return new RedisCustomConversions(Arrays.asList(offsetToBytes, bytesToOffset));
    }

    @Component
    @ReadingConverter
    public class BytesToOffsetDateTimeConverter implements Converter<byte[], OffsetDateTime> {
        @Override
        public OffsetDateTime convert(final byte[] source) {
            return OffsetDateTime.parse(new String(source), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    @Component
    @WritingConverter
    public class OffsetDateTimeToBytesConverter implements Converter<OffsetDateTime, byte[]> {
        @Override
        public byte[] convert(final OffsetDateTime source) {
            return source.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).getBytes();
        }
    }

}
