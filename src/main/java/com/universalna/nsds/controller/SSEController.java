package com.universalna.nsds.controller;

import com.google.common.base.Throwables;
import com.universalna.nsds.controller.amqp.InsuranceCaseCreatedEventDto;
import com.universalna.nsds.model.Notification;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

import java.time.OffsetDateTime;

@Controller
@Api(value = "Контроллер для работы с SSE")
public class SSEController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSEController.class);

    private final ReplayProcessor<ServerSentEvent<Notification>> REPLAY_PROCESSOR = ReplayProcessor.create(1000);
    private final FluxSink<ServerSentEvent<Notification>> REPLAY_PROCESSOR_SINK = REPLAY_PROCESSOR.sink();

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation(value = "Метод для подписки на события SSE")
    public Flux<ServerSentEvent<Notification>> subscribe(@RequestHeader(name = "last-event-id", required = false) final String lastEventId) {
        return REPLAY_PROCESSOR.filter(x -> {
            if (lastEventId == null) {
                return true;
            }
            if (x.data() == null) {
                return false;
            }
            if (x.data().getId() == null) {
                return false;
            }
            return !x.data().getId().isBefore(OffsetDateTime.parse(lastEventId));
        });
    }

    public void send(final InsuranceCaseCreatedEventDto insuranceCaseCreatedEventDto) {
        try {
            final Notification notification = Notification.builder()
                    .noticeId(String.valueOf(insuranceCaseCreatedEventDto.getNoticeId()))
                    .insuranceCaseId(String.valueOf(insuranceCaseCreatedEventDto.getSettlementCaseId()))
                    .build();
            onPostMessage(notification, "InsuranceCaseCreated");
        } catch (Exception e) {
            LOGGER.error("Exception while publishing event to SSE: {}, {} ", e.getMessage(), Throwables.getStackTraceAsString(e));
        }
    }

    public void send(final Notification notification) {
        try {
            onPostMessage(notification, "metadataUpdate");
        } catch (Exception e) {
            LOGGER.error("Exception while publishing event to SSE: {}, {} ", e.getMessage(), Throwables.getStackTraceAsString(e));
        }
    }

    private void onPostMessage(final Notification msg, final String eventName) {
        final ServerSentEvent<Notification> event = ServerSentEvent.builder(msg)
                .event(eventName)
                .id(msg.getId().toString())
                .build();
        REPLAY_PROCESSOR_SINK.next(event);
    }
}
