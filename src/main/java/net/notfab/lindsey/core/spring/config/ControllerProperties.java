package net.notfab.lindsey.core.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bot.controller")
public class ControllerProperties {

    private String url;
    private String token;
    private String id;

}
