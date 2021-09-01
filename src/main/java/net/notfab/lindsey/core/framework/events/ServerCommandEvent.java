package net.notfab.lindsey.core.framework.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.notfab.eventti.Cancellable;
import net.notfab.eventti.Event;
import net.notfab.lindsey.core.framework.command.OptionMapper;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServerCommandEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    @NotNull
    private Guild guild;

    @NotNull
    private Member member;

    @NotNull
    private TextChannel channel;

    @NotNull
    private String path;

    @NotNull
    private OptionMapper options;

    @NotNull
    private SlashCommandEvent underlying;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }


}
