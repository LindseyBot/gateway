package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMetaBase;
import net.lindseybot.entities.interaction.commands.OptionHolder;
import net.lindseybot.entities.interaction.request.CommandRequest;
import net.lindseybot.entities.interaction.response.InteractionResponse;
import net.lindseybot.entities.interaction.response.MessageResponse;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.framework.FakeBuilder;
import net.notfab.lindsey.core.framework.command.MethodReference;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.service.CommandService;
import net.notfab.lindsey.core.service.EventService;
import net.notfab.lindsey.core.service.Messenger;
import net.notfab.lindsey.core.service.PermissionsService;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class CommandListener implements Listener {

    private final CommandService manager;
    private final PermissionsService permissions;
    private final Messenger msg;
    private final RabbitTemplate rabbit;

    private final ThreadPoolTaskExecutor taskExecutor;

    public CommandListener(EventService events, CommandService manager,
                           PermissionsService permissions, Messenger msg, RabbitTemplate rabbit) {
        this.manager = manager;
        this.permissions = permissions;
        this.msg = msg;
        this.rabbit = rabbit;
        events.addListener(this);
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("commands-");
        taskExecutor.initialize();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSlashCommand(@NotNull ServerCommandEvent event) {
        CommandMetaBase meta = this.manager.findMeta(event.getPath());
        if (meta == null) {
            log.warn("Received invalid command: {}", event.getPath());
            return;
        }
        PermissionLevel access = this.manager.getPermission(event.getPath());
        if (!permissions.hasPermission(event.getMember(), access)) {
            this.msg.reply(event, Label.of("permissions.command"), true);
            return;
        }
        try {
            if (!manager.hasListener(event.getPath())) {
                log.info("Received remote command: {}", event.getPath());
                this.executeRemotely(event);
            } else {
                MethodReference method = manager.getListener(event.getPath());
                this.executeLocally(event, method);
            }
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to schedule command execution", e);
        } catch (TimeoutException e) {
            log.warn("Timed out during command execution: {}", event.getPath());
            event.getUnderlying().deferReply(meta.isEphemeral())
                .queue();
        }
    }

    private void executeLocally(ServerCommandEvent event, MethodReference method)
        throws ExecutionException, InterruptedException, TimeoutException {
        this.taskExecutor.submit(() -> {
            try {
                method.invoke(event);
            } catch (Exception ex) {
                log.error("Failed to execute command", ex);
            }
        }).get(1500, TimeUnit.MILLISECONDS);
    }

    private void executeRemotely(ServerCommandEvent event)
        throws ExecutionException, InterruptedException, TimeoutException {
        CommandRequest request = this.createRequest(event);
        this.taskExecutor.submit(() -> {
            try {
                InteractionResponse response = this.rabbit
                    .convertSendAndReceiveAsType("commands", event.getPath().replace("/", "."), request, this.typeReference());
                if (response instanceof MessageResponse msg) {
                    // TODO: Convert
                    log.debug("Remote Response: {}", msg);
                }
            } catch (Exception ex) {
                log.error("Failed to execute command", ex);
            }
        }).get(1500, TimeUnit.MILLISECONDS);
    }

    private ParameterizedTypeReference<InteractionResponse> typeReference() {
        return new ParameterizedTypeReference<>() {
            @Override
            public @NotNull Type getType() {
                return InteractionResponse.class;
            }
        };
    }

    private CommandRequest createRequest(ServerCommandEvent event) {
        String path = event.getPath().replace("/", ".");
        CommandRequest request = new CommandRequest();
        request.setPath(path);
        request.setGuild(FakeBuilder.toFake(event.getGuild()));
        request.setMember(FakeBuilder.toFake(event.getMember()));
        request.setChannel(FakeBuilder.toFake(event.getChannel()));
        OptionHolder holder = new OptionHolder();
        for (OptionMapping option : event.getOptions().options()) {
            switch (option.getType()) {
                case USER -> {
                    Member member = option.getAsMember();
                    if (member != null) {
                        holder.put(option.getName(), FakeBuilder.toFake(member));
                    } else {
                        holder.put(option.getName(), FakeBuilder.toFake(option.getAsUser()));
                    }
                }
                case CHANNEL -> {
                    GuildChannel channel = option.getAsGuildChannel();
                    if (channel instanceof TextChannel text) {
                        holder.put(option.getName(), FakeBuilder.toFake(text));
                    } else if (channel instanceof VoiceChannel voice) {
                        holder.put(option.getName(), FakeBuilder.toFake(voice));
                    }
                }
                case ROLE -> holder.put(option.getName(), FakeBuilder.toFake(option.getAsRole()));
                case INTEGER -> holder.put(option.getName(), option.getAsLong());
                case BOOLEAN -> holder.put(option.getName(), option.getAsBoolean());
                case STRING -> holder.put(option.getName(), option.getAsString());
            }
        }
        request.setOptions(holder);
        return request;
    }

}
