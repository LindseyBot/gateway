package net.notfab.lindsey.framework.actions.impl.conditions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.Utils;
import net.notfab.lindsey.framework.actions.Condition;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRegex implements Condition {

    private String regex;

    @Override
    public boolean is(Guild guild, Member member, TextChannel channel, Message message) {
        if (message == null) {
            return false;
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message.getContentRaw());
            return Utils.timeout(matcher::find, 1, TimeUnit.SECONDS);
        } catch (Exception ex) {
            return false;
        }
    }

}
