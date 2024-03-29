package net.notfab.lindsey.core.spring;

import net.lindseybot.controller.registry.ButtonRegistry;
import net.lindseybot.controller.registry.CommandRegistry;
import net.lindseybot.properties.ControllerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WorkerConfig {

    @Bean
    public CommandRegistry commands(ControllerProperties properties) {
        return new CommandRegistry(properties);
    }

    @Bean
    public ButtonRegistry buttons(ControllerProperties properties) {
        return new ButtonRegistry(properties);
    }

}
