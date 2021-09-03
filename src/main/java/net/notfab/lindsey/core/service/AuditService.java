package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.notfab.lindsey.core.framework.audit.LogBuilder;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditService {

    private final GelfTransport transport;

    public AuditService(GelfTransport transport) {
        this.transport = transport;
    }

    public LogBuilder builder() {
        return new LogBuilder(this);
    }

    public void send(GelfMessage message) {
        try {
            this.transport.send(message);
        } catch (InterruptedException ex) {
            log.error("Failed to save guild log", ex);
        }
    }
}
