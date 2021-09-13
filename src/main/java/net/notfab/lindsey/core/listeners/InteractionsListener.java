package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.lindseybot.entities.interaction.buttons.ButtonMeta;
import net.lindseybot.entities.interaction.request.ButtonRequest;
import net.lindseybot.entities.interaction.response.ButtonResponse;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.framework.FakeBuilder;
import net.notfab.lindsey.core.framework.events.ButtonClickedEvent;
import net.notfab.lindsey.core.service.ButtonService;
import net.notfab.lindsey.core.service.Messenger;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class InteractionsListener implements Listener {

    private final ButtonService buttons;
    private final RabbitTemplate rabbit;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final Messenger msg;

    public InteractionsListener(ButtonService buttons, RabbitTemplate rabbit, Messenger msg) {
        this.buttons = buttons;
        this.rabbit = rabbit;
        this.msg = msg;
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("interactions-");
        taskExecutor.initialize();
    }

    @EventHandler(ignoreCancelled = true)
    public void onButtonClicked(@NotNull ButtonClickedEvent event) {
        String data;
        String method;
        if (event.getId().contains(":")) {
            method = event.getId().split(":")[0];
            data = event.getId().split(":")[1];
        } else {
            method = event.getId();
            data = null;
        }
        if (!this.buttons.exists(method)) {
            event.setCancelled(true);
            return;
        }
        ButtonRequest request = new ButtonRequest();
        request.setId(method);
        request.setData(data);
        request.setGuild(FakeBuilder.toFake(event.getGuild()));
        request.setMember(FakeBuilder.toFake(event.getMember()));
        request.setChannel(FakeBuilder.toFake(event.getChannel()));

        try {
            this.taskExecutor
                .submit(() -> this.execute(event.getUnderlying(), request))
                .get(1500, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException ex) {
            log.error("Failed to schedule command execution", ex);
        } catch (TimeoutException ex) {
            log.warn("Timed out during button execution: {}", request.getId());
            ButtonMeta meta = this.buttons.get(method);
            if (meta.isEdit()) {
                event.getUnderlying()
                    .deferEdit()
                    .queue();
            } else {
                event.getUnderlying()
                    .deferReply(meta.isEphemeral())
                    .queue();
            }
        }
    }

    private void execute(ButtonClickEvent event, ButtonRequest request) {
        try {
            ButtonResponse response = this.rabbit.convertSendAndReceiveAsType("buttons",
                request.getId(), request, ButtonResponse.typeReference());
            if (response == null) {
                log.error("Button response is null: {}", request.getId());
            } else if (response.isEdit()) {
                this.msg.edit(event, response);
            } else {
                this.msg.reply(event, response);
            }
        } catch (Exception ex) {
            log.error("Failed to execute button", ex);
        }
    }

}
