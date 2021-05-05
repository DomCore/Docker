package com.universalna.nsds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Map;

import static com.universalna.nsds.Profiles.METADATA;
import static com.universalna.nsds.config.JpaConfig.PERSISTENCE_PACKAGE;
import static org.hibernate.envers.configuration.EnversSettings.ALLOW_IDENTIFIER_REUSE;
import static org.hibernate.envers.configuration.EnversSettings.AUDIT_STRATEGY;
import static org.hibernate.envers.configuration.EnversSettings.AUDIT_STRATEGY_VALIDITY_END_REV_FIELD_NAME;
import static org.hibernate.envers.configuration.EnversSettings.AUDIT_STRATEGY_VALIDITY_REVEND_TIMESTAMP_FIELD_NAME;
import static org.hibernate.envers.configuration.EnversSettings.AUDIT_STRATEGY_VALIDITY_STORE_REVEND_TIMESTAMP;
import static org.hibernate.envers.configuration.EnversSettings.AUDIT_TABLE_SUFFIX;
import static org.hibernate.envers.configuration.EnversSettings.REVISION_FIELD_NAME;
import static org.hibernate.envers.configuration.EnversSettings.REVISION_TYPE_FIELD_NAME;
import static org.hibernate.envers.configuration.EnversSettings.STORE_DATA_AT_DELETE;

@Profile(METADATA)
@Configuration
@EnableJpaRepositories(basePackages = PERSISTENCE_PACKAGE)
public class JpaConfig {

    static final String PERSISTENCE_PACKAGE = "com.universalna.nsds.persistence";

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Value("${spring.datasource.url}") final String jdbcUrl,
                                                                       @Value("${spring.datasource.username}") final String username,
                                                                       @Value("${spring.datasource.password}") final String password,
                                                                       final JpaVendorAdapter jpaVendorAdapter,
                                                                       final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan(PERSISTENCE_PACKAGE);
        entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);

        final Map<String, Object> properties = entityManagerFactory.getJpaPropertyMap();
        properties.put("connection.driver_class","org.postgresql.Driver");
        properties.put("hibernate.connection.username",username);
        properties.put("hibernate.connection.password",password);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
        properties.put("hibernate.connection.url", jdbcUrl);
        properties.put("hibernate.cache.use_second_level_cache", "false");
        properties.put("hibernate.cache.use_query_cache", "false");
        properties.put("hibernate.hbm2ddl.auto", "none"); // default value is "none", was set explicitly
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.jdbc.batch_size", "25");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.connection.provider_class", "com.zaxxer.hikari.hibernate.HikariConnectionProvider");

        properties.put(AUDIT_TABLE_SUFFIX, "_AUDIT");
        properties.put(REVISION_FIELD_NAME, "AUDIT_REVISION");
        properties.put(REVISION_TYPE_FIELD_NAME, "ACTION_TYPE");
        properties.put(AUDIT_STRATEGY_VALIDITY_STORE_REVEND_TIMESTAMP, "true");
        properties.put(AUDIT_STRATEGY_VALIDITY_END_REV_FIELD_NAME, "AUDIT_REVISION_END");
        properties.put(AUDIT_STRATEGY, "com.universalna.nsds.config.CustomValidityAuditStrategy");
        properties.put(AUDIT_STRATEGY_VALIDITY_REVEND_TIMESTAMP_FIELD_NAME, "AUDIT_REVISION_END_TS");
        properties.put(ALLOW_IDENTIFIER_REUSE, "true");
        properties.put(STORE_DATA_AT_DELETE, "true");
        return entityManagerFactory;
    }

    @Bean
    @RequiresNewPropagationTransactionTemplate
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
    }

}
