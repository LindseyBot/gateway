package net.notfab.lindsey.framework.actions.impl.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.actions.Condition;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageContains implements Condition {

    private String content;
    private boolean ignoreCase = false;

    @Override
    public boolean is(Guild guild, Member member, TextChannel channel, Message message) {
        if (message == null) {
            return false;
        }
        if (ignoreCase) {
            return message.getContentDisplay().toLowerCase().contains(this.content.toLowerCase());
        } else {
            return message.getContentDisplay().contains(this.content);
        }
    }

}
