package com.icthh.xm.ms.timeline.repository.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.icthh.xm.commons.config.client.repository.ConfigurationModel;
import com.icthh.xm.commons.config.domain.Configuration;
import com.icthh.xm.commons.logging.util.MdcUtils;
import com.icthh.xm.commons.messaging.event.system.SystemEvent;
import com.icthh.xm.commons.messaging.event.system.SystemEventType;
import com.icthh.xm.ms.timeline.config.Constants;
import com.icthh.xm.ms.timeline.service.tenant.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SystemTopicConsumer {

    private final KafkaService kafkaService;
    private final Optional<ConfigurationModel> configurationModel;

    /**
     * Consume tenant command event message.
     * @param message the tenant command event message
     */
    @Retryable(maxAttemptsExpression = "${application.retry.max-attempts}",
        backoff = @Backoff(delayExpression = "${application.retry.delay}",
            multiplierExpression = "${application.retry.multiplier}"))
    public void consumeEvent(ConsumerRecord<String, String> message) {
        MdcUtils.putRid();
        try {
            log.info("Consume event from topic [{}]", message.topic());
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            try {
                SystemEvent event = mapper.readValue(message.value(), SystemEvent.class);

                log.info("Process event from topic [{}], type='{}', source='{}', event_id ='{}'",
                    message.topic(), event.getEventType(), event.getMessageSource(), event.getEventId());

                String tenant = Objects.toString(event.getDataMap().get(Constants.EVENT_TENANT), null);
                String command = event.getEventType();
                switch (command.toUpperCase()) {
                    case SystemEventType.CREATE_COMMAND:
                        kafkaService.createKafkaConsumer(tenant);
                        break;
                    case SystemEventType.DELETE_COMMAND:
                        kafkaService.deleteKafkaConsumer(tenant);
                        break;
                    case SystemEventType.SAVE_CONFIGURATION:
                        onSaveConfiguration(event);
                        break;
                    default:
                        log.info("Event ignored with type='{}', source='{}', event_id='{}'",
                            event.getEventType(), event.getMessageSource(), event.getEventId());
                        break;
                }

            } catch (IOException e) {
                log.error("System topic message has incorrect format: '{}' ", message.value(), e);
            }

        } finally {
            MdcUtils.removeRid();
        }
    }

    private void onSaveConfiguration(SystemEvent event) {
        String path = Objects.toString(event.getDataMap().get("path"), null);
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Event '" + event.getEventType() + "' configuration path can't be blank");
        }
        String commit = Objects.toString(event.getDataMap().get("commit"), null);
        if (StringUtils.isBlank(commit)) {
            throw new IllegalArgumentException("Event '" + event.getEventType() + "' configuration commit can't be blank");
        }
        configurationModel.ifPresent(model -> model.updateConfiguration(new Configuration(path, null, commit)));
    }
}
