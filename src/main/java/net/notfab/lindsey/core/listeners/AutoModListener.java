package net.notfab.lindsey.core.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.eventti.ListenerPriority;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.events.ServerMessageReceivedEvent;
import net.notfab.lindsey.core.framework.events.ServerMessageUpdatedEvent;
import net.notfab.lindsey.core.service.AutoModService;
import net.notfab.lindsey.core.service.EventService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class AutoModListener implements Listener {

    private final AutoModService service;

    public AutoModListener(EventService events, AutoModService service) {
        this.service = service;
        events.addListener(this);
    }

    /**
     * Fired on guild message received, basic checks have already been done.
     *
     * @param event Cancellable event.
     */
    @EventHandler(priority = ListenerPriority.HIGH, ignoreCancelled = true)
    public void onGuildMessageReceived(@NotNull ServerMessageReceivedEvent event) {
        if (this.isExempt(event.getMember(), event.getChannel())) {
            return;
        }
        if (!this.service.isEnabled(event.getGuild().getIdLong())) {
            return;
        }
        boolean actionTaken = this.service.moderate(event.getMessage(), event.getMember());
        if (actionTaken) {
            event.setCancelled(true);
        }
    }

    /**
     * Fired on guild message updated, basic checks have already been done.
     *
     * @param event Cancellable event.
     */
    @EventHandler(priority = ListenerPriority.HIGH, ignoreCancelled = true)
    public void onGuildMessageUpdated(@NotNull ServerMessageUpdatedEvent event) {
        if (this.isExempt(event.getMember(), event.getChannel())) {
            return;
        }
        if (!this.service.isEnabled(event.getGuild().getIdLong())) {
            return;
        }
        boolean actionTaken = this.service.moderate(event.getMessage(), event.getMember());
        if (actionTaken) {
            event.setCancelled(true);
        }
    }

    private boolean isExempt(Member member, TextChannel channel) {
        if (Utils.isDiscordModerator(member)) {
            return true;
        }
        return channel.isNews();
    }

}
