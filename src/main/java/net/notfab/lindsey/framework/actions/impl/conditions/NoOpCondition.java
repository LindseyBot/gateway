package net.notfab.lindsey.framework.actions.impl.conditions;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.actions.Condition;

@Data
public class NoOpCondition implements Condition {

    @Override
    public boolean is(Guild guild, Member member, TextChannel channel, Message message) {
        return true;
    }

}
