package net.notfab.lindsey.core.framework.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.notfab.eventti.Cancellable;
import net.notfab.eventti.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ButtonClickedEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    @NotNull
    private Guild guild;

    @NotNull
    private Member member;

    @NotNull
    private TextChannel channel;

    @NotNull
    private String id;

    @Nullable
    private Button button;

    @NotNull
    private ButtonClickEvent underlying;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }


}
