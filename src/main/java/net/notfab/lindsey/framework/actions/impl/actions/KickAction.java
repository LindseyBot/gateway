package net.notfab.lindsey.framework.actions.impl.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.actions.Action;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KickAction implements Action {

    private String reason;

    @Override
    public void act(Guild guild, Member member, TextChannel channel, Message message) {
        if (member == null) {
            return;
        }
        if (!guild.getSelfMember().canInteract(member)) {
            return;
        }
        if (reason != null) {
            guild.kick(member, reason).queue();
        } else {
            guild.kick(member).queue();
        }
    }

}
