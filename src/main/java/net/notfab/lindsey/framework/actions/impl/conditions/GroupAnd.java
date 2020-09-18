package net.notfab.lindsey.framework.actions.impl.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.actions.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupAnd implements Condition {

    private List<Condition> conditions = new ArrayList<>();

    public GroupAnd(Condition... conditions) {
        this.conditions = Arrays.asList(conditions);
    }

    @Override
    public boolean is(Guild guild, Member member, TextChannel channel, Message message) {
        for (Condition condition : this.conditions) {
            boolean state = condition.is(guild, member, channel, message);
            if (!state) {
                return false;
            }
        }
        return true;
    }

}
