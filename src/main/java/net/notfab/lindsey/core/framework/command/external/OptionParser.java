package net.notfab.lindsey.core.framework.command.external;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.lindseybot.commands.request.CommandOption;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.FinderUtil;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionParser {

    protected static Object parse(CommandOption option, String argument, GuildMessageReceivedEvent event) throws BadArgumentException {
        switch (option.getType()) {
            case MEMBER: {
                Member member = FinderUtil.findMember(argument, event.getMessage());
                if (member == null) {
                    throw new BadArgumentException("search.member", argument);
                }
                return FakeBuilder.toFake(member);
            }
            case TEXT_CHANNEL: {
                TextChannel channel = FinderUtil.findTextChannel(argument, event.getGuild());
                if (channel == null) {
                    throw new BadArgumentException("search.channel", argument);
                }
                return FakeBuilder.toFake(channel);
            }
            case VOICE_CHANNEL: {
                VoiceChannel channel = FinderUtil.findVoiceChannel(argument, event.getGuild());
                if (channel == null) {
                    throw new BadArgumentException("search.voice", argument);
                }
                return FakeBuilder.toFake(channel);
            }
            case ROLE:
                Role role = FinderUtil.findRole(argument, event.getGuild());
                if (role == null) {
                    throw new BadArgumentException("search.role", argument);
                }
                return FakeBuilder.toFake(role);
            case BOOLEAN:
                Optional<Boolean> bool = Utils.parseBoolean(argument);
                if (bool.isEmpty()) {
                    throw new BadArgumentException("core.not_boolean", argument);
                }
                return bool.get();
            case INT: {
                long value;
                try {
                    value = Long.parseLong(argument);
                } catch (NumberFormatException ex) {
                    throw new BadArgumentException("core.not_number", argument);
                }
                if (value < option.getMinimum()) {
                    throw new BadArgumentException("core.too_small", argument, Math.floor(option.getMinimum()));
                } else if (value > option.getMaximum()) {
                    throw new BadArgumentException("core.too_big", argument, Math.floor(option.getMaximum()));
                }
                return value;
            }
            case DOUBLE: {
                double value;
                try {
                    value = Double.parseDouble(argument);
                } catch (NumberFormatException ex) {
                    throw new BadArgumentException("core.not_number", argument);
                }
                if (value < option.getMinimum()) {
                    throw new BadArgumentException("core.too_small", argument, option.getMinimum());
                } else if (value > option.getMaximum()) {
                    throw new BadArgumentException("core.too_big", argument, option.getMaximum());
                }
                return value;
            }
            case REGEX: {
                Pattern pattern = Pattern.compile(option.getPattern());
                Matcher matcher = pattern.matcher(argument);
                if (!matcher.find()) {
                    throw new BadArgumentException("core.invalid_argument", argument, option.getName());
                }
                return argument;
            }
            case ENUM: {
                Optional<String> optional = option.getEnumEntries().stream()
                    .filter(entry -> entry.equalsIgnoreCase(argument))
                    .findFirst();
                if (optional.isEmpty()) {
                    throw new BadArgumentException("core.not_enum", argument);
                }
                return argument;
            }
            default:
                return argument;
        }
    }

}
