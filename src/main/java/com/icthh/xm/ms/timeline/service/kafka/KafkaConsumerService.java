package com.icthh.xm.ms.timeline.service.kafka;

import com.icthh.xm.ms.timeline.repository.kafka.TimelineEventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ConsumerFactory<String, String> consumerFactory;
    private final TimelineEventConsumer consumer;

    private Map<String, ConcurrentMessageListenerContainer<String, String>> consumers = new ConcurrentHashMap<>();

    /**
     * Create topic consumer.
     *
     * @param tenant the kafka topic
     */
    public void createKafkaConsumer(String tenant) {
        ConcurrentMessageListenerContainer<String, String> container = consumers.get(tenant);
        if (container != null) {
            if (!container.isRunning()) {
                container.start();
            }
        } else {
            ContainerProperties containerProps = new ContainerProperties(tenant);
            container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProps);
            container.setupMessageListener((MessageListener<String, String>) consumer::consumeEvent);
            container.setBeanName(tenant);
            container.start();
            consumers.put(tenant, container);
        }
    }

    /**
     * Delete topic consumer.
     *
     * @param tenant the kafka topic
     */
    public void deleteKafkaConsumer(String tenant) {
        if (consumers.get(tenant) != null) {
            consumers.get(tenant).stop();
            consumers.remove(tenant);
        }
    }

}
