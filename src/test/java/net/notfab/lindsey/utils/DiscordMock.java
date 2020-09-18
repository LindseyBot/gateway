package net.notfab.lindsey.utils;

import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiscordMock {

    public static User user(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(String.valueOf(id));
        when(user.getIdLong()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(user.getAsMention()).thenReturn("<@" + id + ">");
        return user;
    }

    public static Member member(Guild guild, long id, String name) {
        User user = user(id, name);
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(String.valueOf(id));
        when(member.getIdLong()).thenReturn(id);
        when(member.getEffectiveName()).thenReturn(name);
        when(member.getNickname()).thenReturn(name);
        when(member.getUser()).thenReturn(user);
        when(member.getGuild()).thenReturn(guild);
        return member;
    }

    public static Role role(long id, String name) {
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(String.valueOf(id));
        when(role.getIdLong()).thenReturn(id);
        when(role.getAsMention()).thenReturn("<@&" + id + ">");
        when(role.getName()).thenReturn(name);
        return role;
    }

    public static Guild guild(long id, String name) {
        return guild(id, name, 1);
    }

    public static Guild guild(long id, String name, int roleCount) {
        List<Role> roles = new ArrayList<>();
        for (int i = 0; i < roleCount; i++) {
            roles.add(DiscordMock.role(i, "role" + i));
        }
        return guild(id, name, roles);
    }

    public static Guild guild(long id, String name, List<Role> roles) {
        Guild guild = mock(Guild.class);
        Member owner = member(guild, id, "owner");
        Member bot = member(guild, 0L, "bot");
        Role everyone = role(id, "everyone");
        when(guild.getId()).thenReturn(String.valueOf(id));
        when(guild.getIdLong()).thenReturn(id);
        when(guild.getName()).thenReturn(name);
        when(guild.getOwner()).thenReturn(owner);
        when(guild.getSelfMember()).thenReturn(bot);
        when(guild.getRoles()).thenReturn(roles);
        when(guild.getRoleById(anyLong())).thenReturn(roles.get(0));
        when(guild.getRoleById(anyString())).thenReturn(roles.get(0));
        when(guild.getPublicRole()).thenReturn(everyone);
        return guild;
    }

    public static TextChannel textChannel(Guild guild, long id, String name) {
        TextChannel channel = mock(TextChannel.class);
        when(channel.getId()).thenReturn(String.valueOf(id));
        when(channel.getIdLong()).thenReturn(id);
        when(channel.getName()).thenReturn(name);
        when(channel.getGuild()).thenReturn(guild);
        return channel;
    }

    public static Message message(Guild guild, Member member, String content) {
        User user = member.getUser();
        Message message = mock(Message.class);
        when(message.getGuild()).thenReturn(guild);
        when(message.getId()).thenReturn("0");
        when(message.getIdLong()).thenReturn(0L);
        when(message.getAuthor()).thenReturn(user);
        when(message.getMember()).thenReturn(member);
        when(message.getContentDisplay()).thenReturn(content);
        when(message.getContentRaw()).thenReturn(content);
        when(message.getContentStripped()).thenReturn(content);
        return message;
    }

}
