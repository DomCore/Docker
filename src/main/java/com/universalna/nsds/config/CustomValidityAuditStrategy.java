package com.universalna.nsds.config;

import com.universalna.nsds.component.PrincipalProvider;
import com.universalna.nsds.component.SpringSecurityKeycloak;
import org.hibernate.Session;
import org.hibernate.envers.configuration.internal.AuditEntitiesConfiguration;
import org.hibernate.envers.strategy.ValidityAuditStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Workaround to track lastModifiedBy and lastModifiedDate field for deleted entities
 */
public class CustomValidityAuditStrategy extends ValidityAuditStrategy {

    private final PrincipalProvider principalProvider = new SpringSecurityKeycloak();

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomValidityAuditStrategy.class);

    @Override
    public void perform(final Session session, final String entityName, final AuditEntitiesConfiguration audEntitiesCfg, final Serializable id, final Object data, final Object revision) {
        if (data instanceof Map) {
            final Map dataToUpdate = (Map) data;
            if (id instanceof String) {
                if (((String) id).equalsIgnoreCase("6a9a58b0-e5bc-4f1a-bc2d-0f834ce8a67b")) {
                    LOGGER.debug("6a9a58b0-e5bc-4f1a-bc2d-0f834ce8a67b {}", Arrays.toString(Arrays.stream(Thread.currentThread().getStackTrace()).map(s -> System.lineSeparator().concat(s.toString())).toArray()));
                }
            } else if (id instanceof UUID) {
                if (UUID.fromString("6a9a58b0-e5bc-4f1a-bc2d-0f834ce8a67b").equals(id)) {
                    LOGGER.debug("6a9a58b0-e5bc-4f1a-bc2d-0f834ce8a67b {}", Arrays.toString(Arrays.stream(Thread.currentThread().getStackTrace()).map(s -> System.lineSeparator().concat(s.toString())).toArray()));
                }
            }
            dataToUpdate.put("lastModifiedBy", principalProvider.getPrincipal());
            dataToUpdate.put("lastModifiedDate", OffsetDateTime.now());
        }
        super.perform(session, entityName, audEntitiesCfg, id, data, revision);
    }
}
