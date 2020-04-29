package net.notfab.lindsey.framework.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.Optional;
import java.util.regex.Pattern;

public class FinderUtil {

    private final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
    private final static Pattern FULL_USER_REF = Pattern.compile("(.{2,32})\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    private final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
    private final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
    private final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID

    // Prevent instantiation
    private FinderUtil() {
    }

    public static Optional<Member> findMember(String query, Guild guild) {
        if (DISCORD_ID.matcher(query).matches()) {
            try {
                return Optional.of(guild.retrieveMemberById(query).complete());
            } catch (ErrorResponseException ignored) {
                return Optional.empty();
            }
        } else {
            // TODO: Prefix search
            return Optional.empty();
        }
    }

}
