package net.notfab.lindsey.core.framework;

import net.dv8tion.jda.api.entities.*;
import net.lindseybot.entities.discord.*;

import java.util.stream.Collectors;

public class FakeBuilder {

    public static FGuild toFake(Guild guild) {
        FGuild fake = new FGuild();
        fake.setId(guild.getIdLong());
        fake.setName(guild.getName());
        fake.setIconHash(guild.getIconId());
        return fake;
    }

    public static FRole toFake(Role role) {
        FRole fake = new FRole();
        fake.setId(role.getIdLong());
        fake.setName(role.getName());
        fake.setColor(role.getColorRaw());
        fake.setHoisted(role.isHoisted());
        fake.setPosition(role.getPosition());
        fake.setPermissions(String.valueOf(role.getPermissionsRaw()));
        fake.setManaged(role.isManaged());
        fake.setMentionable(role.isMentionable());
        return fake;
    }

    public static FChannel toFake(VoiceChannel channel) {
        FChannel fake = new FChannel();
        fake.setId(channel.getIdLong());
        fake.setName(channel.getName());
        fake.setType(FChannelType.VOICE);
        fake.setPosition(channel.getPosition());
        return fake;
    }

    public static FChannel toFake(TextChannel channel) {
        FChannel fake = new FChannel();
        fake.setId(channel.getIdLong());
        fake.setName(channel.getName());
        fake.setType(FChannelType.TEXT);
        fake.setPosition(channel.getPosition());
        fake.setNsfw(channel.isNSFW());
        return fake;
    }

    public static FUser toFake(User user) {
        FUser fake = new FUser();
        fake.setId(user.getIdLong());
        fake.setName(user.getName());
        fake.setDiscriminator(user.getDiscriminator());
        fake.setAvatarHash(user.getAvatarId());
        fake.setBot(user.isBot());
        fake.setFlags(user.getFlagsRaw());
        return fake;
    }

    public static FMember toFake(Member member) {
        FUser user = FakeBuilder.toFake(member.getUser());
        FMember fake = new FMember();
        fake.setUser(user);
        fake.setNickname(member.getNickname());
        //fake.setAvatarHash(member.getAvatarId());
        fake.setPending(member.isPending());
        fake.setRoles(member.getRoles().stream()
            .map(Role::getIdLong)
            .collect(Collectors.toList()));
        return fake;
    }

}
