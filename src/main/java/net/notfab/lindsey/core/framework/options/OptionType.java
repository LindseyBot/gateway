package net.notfab.lindsey.core.framework.options;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.notfab.lindsey.core.framework.command.FinderUtil;

public enum OptionType {

    STRING, INT, BOOLEAN, TEXT_CHANNEL, VOICE_CHANNEL;

    public boolean check(Guild guild, Object value) {
        try {
            this.parse(guild, value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(Guild guild, Object value) {
        if (this == OptionType.STRING && value instanceof String) {
            return (T) value;
        } else if (this == OptionType.BOOLEAN) {
            String str = String.valueOf(value);
            if (value instanceof Boolean) {
                return (T) value;
            } else if (str.matches("^(no|n|false|f|off|disabled?)$")) {
                return (T) Boolean.FALSE;
            } else if (str.matches("^(yes|y|true|t|on|enabled?)$")) {
                return (T) Boolean.TRUE;
            } else {
                throw new IllegalArgumentException("Not a boolean");
            }
        } else if (this == OptionType.INT) {
            if (value instanceof Integer) {
                return (T) value;
            }
            String str = String.valueOf(value);
            try {
                return (T) Integer.valueOf(str);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Not a number");
            }
        } else if (this == OptionType.TEXT_CHANNEL) {
            if (value instanceof TextChannel) {
                return (T) value;
            }
            TextChannel channel = FinderUtil.findTextChannel(String.valueOf(value), guild);
            if (channel == null) {
                throw new IllegalArgumentException("Channel not found");
            }
            return (T) channel;
        } else if (this == OptionType.VOICE_CHANNEL) {
            if (value instanceof VoiceChannel) {
                return (T) value;
            }
            VoiceChannel channel = FinderUtil.findVoiceChannel(String.valueOf(value), guild);
            if (channel == null) {
                throw new IllegalArgumentException("Channel not found");
            }
            return (T) channel;
        } else {
            throw new IllegalArgumentException("Unknown OptionType");
        }
    }

}
