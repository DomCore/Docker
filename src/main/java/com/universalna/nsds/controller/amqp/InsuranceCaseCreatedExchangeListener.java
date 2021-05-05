package com.universalna.nsds.controller.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.universalna.nsds.component.SearcherLogManager;
import com.universalna.nsds.controller.SSEController;
import com.universalna.nsds.model.Relation;
import com.universalna.nsds.persistence.redis.ProfitsoftSettlementNotificationEvent;
import com.universalna.nsds.persistence.redis.ProfitsoftSettlementNotificationEventRepository;
import com.universalna.nsds.service.FileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;

import static com.universalna.nsds.config.RabbitConfig.NSDS_SETTLEMENT_CASE_CREATED_QUEUE;
import static com.universalna.nsds.config.RabbitConfig.SETTLEMENT_CASE_CREATED_EXHANGE;

@Component
public class InsuranceCaseCreatedExchangeListener implements MessageListener {

    private static final Logger LOGGER = LogManager.getLogger(InsuranceCaseCreatedExchangeListener.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private SearcherLogManager searcherLogManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    @Autowired
    private SSEController sseController;

    @Autowired
    private ProfitsoftSettlementNotificationEventRepository profitsoftSettlementNotificationEventRepository;

    @Override
    @RabbitListener(bindings = {
            @QueueBinding(exchange = @Exchange(value = "${spring.rabbitmq.cluster}" + "." + SETTLEMENT_CASE_CREATED_EXHANGE, type = ExchangeTypes.FANOUT),
                    value = @Queue(value = "${spring.rabbitmq.cluster}" + "." + NSDS_SETTLEMENT_CASE_CREATED_QUEUE, durable = "true"))
    })
    public void onMessage(final Message message) {
        final String payload = new String(message.getBody());
        LOGGER.info("InsuranceCaseCreatedEventDto new message received: {}", payload);
        final InsuranceCaseCreatedEventDto event;
        try {
            event = objectMapper.readValue(payload, InsuranceCaseCreatedEventDto.class);
        } catch (IOException e) {
            LOGGER.error("Failed to deserialize InsuranceCaseCreatedEventDto: {}", payload);
            return;
        }
        final Set<ConstraintViolation<InsuranceCaseCreatedEventDto>> violations = validator.validate(event);
        if (violations.isEmpty()) {
            final String noticeId = String.valueOf(event.getNoticeId());
            final String notificationId = "NOTICE_ID: " + event.getNoticeId() + " CASE_ID: " + event.getSettlementCaseId();
            if (!profitsoftSettlementNotificationEventRepository.existsById(notificationId)) {
                try {
                    fileService.getRelatedMetadata(Relation.SETTLEMENT_NOTIFICATION, noticeId)
                            .forEach(m -> fileService.copyMetadata(m.getId(), Relation.INSURANCE_CASE, String.valueOf(event.getSettlementCaseId())));
                    profitsoftSettlementNotificationEventRepository.save(ProfitsoftSettlementNotificationEvent.builder().id(notificationId).build());
                    sseController.send(event);
                    searcherLogManager.add(event);
                } catch (Exception e) {
                    LOGGER.error("Exception caught while processing profitsoft event: {} , {}, {}", event, e, Throwables.getStackTraceAsString(e));
                }
            } else {
                LOGGER.info("Duplicate event: {}", event);
            }
        } else {
            LOGGER.debug("Constraint violations at InsuranceCaseCreatedEventDto, payload: {}", payload);
        }
    }
}
