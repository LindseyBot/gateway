package net.notfab.lindsey.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.notfab.eventti.Event;
import net.notfab.eventti.EventManager;
import net.notfab.eventti.Listener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventService {

    private final RabbitTemplate rabbit;
    private final ObjectMapper objectMapper;
    private final EventManager eventManager;

    public EventService(RabbitTemplate rabbit, ObjectMapper objectMapper) {
        this.rabbit = rabbit;
        this.objectMapper = objectMapper;
        this.eventManager = new EventManager();
    }

    /**
     * Fires an event locally.
     *
     * @param event Event to fire.
     */
    public void fire(Event event) {
        this.eventManager.fire(event);
    }

    /**
     * Publish an event to the network.
     *
     * @param to    RabbitMQ Exchange.
     * @param event Event.
     */
    public void publish(String to, Event event) {
        String message;
        try {
            message = this.objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error during event serialization", ex);
            return;
        }
        this.rabbit.convertAndSend(to, "", message);
    }

    @RabbitListener(queues = "#{anonQueue.name}")
    public void onMessage(@Payload String payload) {
        Event event;
        try {
            event = this.objectMapper.readValue(payload, Event.class);
        } catch (JsonProcessingException ex) {
            log.error("Error during event deserialization", ex);
            return;
        }
        this.eventManager.fire(event);
    }

    public void addListener(Listener listener) {
        this.eventManager.addListener(listener);
    }

}
