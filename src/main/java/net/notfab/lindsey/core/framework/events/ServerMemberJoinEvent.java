package net.notfab.lindsey.core.framework.events;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.notfab.eventti.Cancellable;
import net.notfab.eventti.Event;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServerMemberJoinEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    @NotNull
    private Member member;
    @NotNull
    private Guild guild;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

}
