package net.notfab.lindsey.framework.command;

import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinderUtil {

    private final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
    private final static Pattern FULL_USER_REF = Pattern.compile("(.{2,32})\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    private final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
    private final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
    private final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID

    // Prevent instantiation
    private FinderUtil() {
        throw new IllegalStateException("Not Supported");
    }

    public static TextChannel findTextChannel(String query, Guild guild) {
        // Mention
        Matcher channelMention = CHANNEL_MENTION.matcher(query);
        if (channelMention.matches()) {
            return guild.getTextChannelById(channelMention.replaceAll("$1"));
        }
        // Id
        if (DISCORD_ID.matcher(query).matches()) {
            return guild.getTextChannelById(query);
        }
        // -- Fuzzy
        if (query.startsWith("@")) {
            query = query.replaceFirst("@", "");
        }
        List<TextChannel> channels = guild.getTextChannels();
        // --
        int bestScore = 0;
        TextChannel bestChannel = null;
        for (TextChannel channel : channels) {
            int score = 0;
            String currentName = channel.getName();
            if (currentName.equals(query)) {
                score += 4;
            } else if (currentName.equalsIgnoreCase(query)) {
                score += 3;
            } else if (currentName.startsWith(query)) {
                score += 2;
            } else if (currentName.contains(query)) {
                score++;
            }
            if (score > bestScore) {
                bestChannel = channel;
                bestScore = score;
            }
        }
        return bestChannel;
    }

    public static VoiceChannel findVoiceChannel(String query, Guild guild) {
        // Mention
        Matcher channelMention = CHANNEL_MENTION.matcher(query);
        if (channelMention.matches()) {
            return guild.getVoiceChannelById(channelMention.replaceAll("$1"));
        }
        // Id
        if (DISCORD_ID.matcher(query).matches()) {
            return guild.getVoiceChannelById(query);
        }
        // -- Fuzzy
        if (query.startsWith("@")) {
            query = query.replaceFirst("@", "");
        }
        List<VoiceChannel> channels = guild.getVoiceChannels();
        // --
        int bestScore = 0;
        VoiceChannel bestChannel = null;
        for (VoiceChannel channel : channels) {
            int score = 0;
            String currentName = channel.getName();
            if (currentName.equals(query)) {
                score += 4;
            } else if (currentName.equalsIgnoreCase(query)) {
                score += 3;
            } else if (currentName.startsWith(query)) {
                score += 2;
            } else if (currentName.contains(query)) {
                score++;
            }
            if (score > bestScore) {
                bestChannel = channel;
                bestScore = score;
            }
        }
        return bestChannel;
    }

    public static Role findRole(String query, Guild guild) {
        // Mention
        Matcher roleMention = ROLE_MENTION.matcher(query);
        if (roleMention.matches()) {
            return guild.getRoleById(roleMention.replaceAll("$1"));
        }
        // Id
        if (DISCORD_ID.matcher(query).matches()) {
            return guild.getRoleById(query);
        }
        // -- Fuzzy
        if (query.startsWith("@")) {
            query = query.replaceFirst("@", "");
        }
        List<Role> roles = guild.getRoles();
        // --
        int bestScore = 0;
        Role bestRole = null;
        for (Role role : roles) {
            int score = 0;
            String currentName = role.getName();
            if (currentName.equals(query)) {
                score += 4;
            } else if (currentName.equalsIgnoreCase(query)) {
                score += 3;
            } else if (currentName.startsWith(query)) {
                score += 2;
            } else if (currentName.contains(query)) {
                score++;
            }
            if (score > bestScore) {
                bestRole = role;
                bestScore = score;
            }
        }
        return bestRole;
    }

    public static Member findMember(String query, Guild guild) {
        // Mention
        Matcher userMention = USER_MENTION.matcher(query);
        if (userMention.matches()) {
            return guild.retrieveMemberById(userMention.replaceAll("$1"))
                    .complete();
        }
        // Id
        if (DISCORD_ID.matcher(query).matches()) {
            return guild.retrieveMemberById(query)
                    .complete();
        }
        // User#Dis
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);
        if (fullRefMatch.matches()) {
            String name = fullRefMatch.replaceAll("$1");
            String disc = fullRefMatch.replaceAll("$2");
            List<Member> oneMember = guild.retrieveMembersByPrefix(name, 1).get();
            if (oneMember.isEmpty()) {
                return null;
            }
            Member member = oneMember.get(0);
            if (member.getUser().getDiscriminator().equals(disc)) {
                return member;
            } else {
                return null;
            }
        }
        // -- Fuzzy
        if (query.startsWith("@")) {
            query = query.replaceFirst("@", "");
        }
        List<Member> members = guild.retrieveMembersByPrefix(query, 10).get();
        // --
        int bestScore = 0;
        Member bestMember = null;
        for (Member member : members) {
            int score = 0;
            String currentName = member.getEffectiveName();
            if (currentName.equals(query)) {
                score += 4;
            } else if (currentName.equalsIgnoreCase(query)) {
                score += 3;
            } else if (currentName.startsWith(query)) {
                score += 2;
            } else if (currentName.contains(query)) {
                score++;
            }
            if (score > bestScore) {
                bestMember = member;
                bestScore = score;
            }
        }
        return bestMember;
    }

}
