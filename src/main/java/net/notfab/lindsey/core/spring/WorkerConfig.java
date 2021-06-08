package net.notfab.lindsey.core.spring;

import net.lindseybot.controller.registry.ButtonRegistry;
import net.lindseybot.controller.registry.CommandRegistry;
import net.lindseybot.properties.ControllerProperties;
import net.lindseybot.services.EventService;
import net.lindseybot.services.MessagingService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

@Component
public class WorkerConfig {

    @Bean
    public EventService eventService() {
        return new EventService();
    }

    @Bean
    public MessagingService messagingService(JedisPool pool, EventService events) {
        MessagingService service = new MessagingService(pool, events);
        service.subscribe("GATEWAYS");
        return service;
    }

    @Bean
    public CommandRegistry commands(ControllerProperties properties,
                                    EventService service, MessagingService messaging) {
        return new CommandRegistry(properties, service, messaging);
    }

    @Bean
    public ButtonRegistry buttons(ControllerProperties properties,
                                  EventService service, MessagingService messaging) {
        return new ButtonRegistry(properties, service, messaging);
    }

}
