package net.notfab.lindsey.core.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.logs")
public class GrayLogProperties {

    private String host;
    private int port;
    private int timeout;

}
