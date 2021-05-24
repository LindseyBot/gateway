package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.notfab.lindsey.core.framework.LogBuilder;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditService {

    private final GelfTransport transport;
    private final Translator translator;

    public AuditService(GelfTransport transport, Translator translator) {
        this.transport = transport;
        this.translator = translator;
    }

    public LogBuilder builder() {
        return new LogBuilder(translator, this);
    }

    public void send(GelfMessage message) {
        try {
            this.transport.send(message);
        } catch (InterruptedException ex) {
            log.error("Failed to save guild log", ex);
        }
    }
}
