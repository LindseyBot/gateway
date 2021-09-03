package net.notfab.lindsey.core.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.bot")
public class AppSettings {

    private String token;
    private boolean beta;

}
