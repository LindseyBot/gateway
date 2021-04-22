package net.notfab.lindsey.core.framework.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.service.EventService;
import net.notfab.lindsey.core.spring.config.ControllerProperties;
import net.notfab.lindsey.shared.entities.commands.ExternalCommand;
import net.notfab.lindsey.shared.entities.events.CommandCreatedEvent;
import net.notfab.lindsey.shared.entities.events.CommandRemovedEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CommandRegistry implements Listener {

    private final OkHttpClient okHttpClient;
    private final ControllerProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, ExternalCommand> commands = new HashMap<>();

    public CommandRegistry(ControllerProperties properties, EventService events, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.okHttpClient = new OkHttpClient.Builder().build();
        events.addListener(this);
    }

    @EventHandler
    public void onCreate(CommandCreatedEvent event) {
        ExternalCommand command = event.getCommand();
        long before = this.commands.size();
        this.commands.put(command.getName(), command);
        if (!command.getAliases().isEmpty()) {
            command.getAliases().forEach(name -> this.commands.put(name, command));
        }
        log.info("Registered " + (this.commands.size() - before) + " commands from event.");
    }

    @EventHandler
    public void onRemove(CommandRemovedEvent event) {
        for (String name : event.getCommandNames()) {
            this.commands.remove(name);
        }
        log.info("Removed " + event.getCommandNames().size() + " commands from event.");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        Request request = new Request.Builder()
            .url(this.properties.getUrl() + "/commands")
            .addHeader("Authorization", "Bearer " + this.properties.getToken())
            .addHeader("X-Worker-Id", this.properties.getId())
            .get().build();
        try {
            Response response = this.okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Invalid response code " + response.code());
            }
            try (ResponseBody body = response.body()) {
                if (body == null) {
                    throw new IOException("Missing body");
                }
                ExternalCommand[] commandList = objectMapper.readValue(body.bytes(), ExternalCommand[].class);
                for (ExternalCommand command : commandList) {
                    CommandCreatedEvent event = new CommandCreatedEvent();
                    event.setCommand(command);
                    this.onCreate(event);
                }
                log.info("Loaded " + commandList.length + " commands with " + (this.commands.size() - commandList.length) + " aliases.");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load commands", ex);
        }
    }

    public ExternalCommand get(String commandName) {
        return this.commands.get(commandName.toLowerCase());
    }

}
