package net.notfab.lindsey.core.spring;

import net.notfab.lindsey.core.spring.config.GrayLogProperties;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class AuditLoggerConfig {

    private final GrayLogProperties properties;

    public AuditLoggerConfig(GrayLogProperties properties) {
        this.properties = properties;
    }

    @Bean
    public GelfTransport transport() {
        GelfConfiguration configuration = new GelfConfiguration(new InetSocketAddress(properties.getHost(), properties.getPort()))
            .transport(GelfTransports.UDP)
            .queueSize(512)
            .connectTimeout(properties.getTimeout())
            .reconnectDelay(1000)
            .sendBufferSize(32768);
        return GelfTransports.create(configuration);
    }

}
