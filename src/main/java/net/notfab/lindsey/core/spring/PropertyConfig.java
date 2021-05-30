package net.notfab.lindsey.core.spring;

import net.lindseybot.properties.ControllerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class PropertyConfig {

    @Bean
    @ConfigurationProperties(prefix = "bot.controller")
    public ControllerProperties controller() {
        return new ControllerProperties();
    }

}
