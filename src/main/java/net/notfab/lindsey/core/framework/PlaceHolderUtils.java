package net.notfab.lindsey.core.framework;

import net.dv8tion.jda.api.entities.Member;

public class PlaceHolderUtils {

    public static String replace(String message, Member member) {
        message = message.replace("${User.Name}", member.getEffectiveName());
        message = message.replace("${User.Id}", member.getId());
        message = message.replace("${User.Mention}", member.getAsMention());
        return message;
    }

}
