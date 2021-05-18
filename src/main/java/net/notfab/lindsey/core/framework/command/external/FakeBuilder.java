package net.notfab.lindsey.core.framework.command.external;

import net.dv8tion.jda.api.entities.*;
import net.notfab.lindsey.shared.rpc.*;

public class FakeBuilder {

    public static FGuild toFake(Guild guild) {
        FGuild fake = new FGuild();
        fake.setId(guild.getIdLong());
        fake.setName(guild.getName());
        fake.setIconUrl(guild.getIconUrl());
        return fake;
    }

    public static FRole toFake(Role role) {
        FRole fake = new FRole();
        fake.setId(role.getIdLong());
        fake.setName(role.getName());
        fake.setPosition(role.getPosition());
        return fake;
    }

    public static FChannel toFake(VoiceChannel channel) {
        FChannel fake = new FChannel();
        fake.setType(FChannelType.VOICE);
        fake.setId(channel.getIdLong());
        fake.setName(channel.getName());
        fake.setPosition(channel.getPosition());
        return fake;
    }

    public static FChannel toFake(TextChannel channel) {
        FChannel fake = new FChannel();
        fake.setType(FChannelType.TEXT);
        fake.setId(channel.getIdLong());
        fake.setName(channel.getName());
        fake.setPosition(channel.getPosition());
        fake.setNsfw(channel.isNSFW());
        return fake;
    }

    public static FMember toFake(Member member) {
        FMember fake = new FMember();
        fake.setId(member.getIdLong());
        fake.setName(member.getEffectiveName());
        fake.setDiscrim(member.getUser().getDiscriminator());
        fake.setAvatarUrl(member.getUser().getEffectiveAvatarUrl());
        fake.setGuildId(member.getGuild().getIdLong());
        fake.setGuildName(member.getGuild().getName());
        return fake;
    }

}
