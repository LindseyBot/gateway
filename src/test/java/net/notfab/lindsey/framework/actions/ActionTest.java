package net.notfab.lindsey.framework.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.notfab.lindsey.framework.actions.impl.actions.AddRoleAction;
import net.notfab.lindsey.framework.actions.impl.conditions.NoOpCondition;
import net.notfab.lindsey.framework.actions.impl.triggers.MessageTrigger;
import net.notfab.lindsey.utils.DiscordMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionTest {

    private GuildAction action;

    @BeforeEach
    public void setUp() {
        List<Action> actions = new ArrayList<>();
        actions.add(new AddRoleAction("123123123123"));

        this.action = new GuildAction();
        action.setTrigger(new MessageTrigger());
        action.setCondition(new NoOpCondition());
        action.setActions(actions);
    }

    @Test
    public void testSerialization() {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = Assertions.assertDoesNotThrow(() -> objectMapper.writeValueAsString(this.action));
        GuildAction action = Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json, GuildAction.class));
        assertEquals(action, this.action);
    }

    @Test
    public void onMessageReceived() {
        GuildMessageReceivedEvent event = this.mockEvent();
        assertTrue(this.action.getTrigger() instanceof MessageTrigger);
        assertTrue(this.action.getCondition().is(event.getGuild(), event.getMember(), event.getChannel(), event.getMessage()));
        for (Action action : this.action.getActions()) {
            action.act(event.getGuild(), event.getMember(), event.getChannel(), event.getMessage());
        }
    }

    private GuildMessageReceivedEvent mockEvent() {
        Guild guild = DiscordMock.guild(2L, "guild");
        Member member = DiscordMock.member(guild, 3L, "SomeMember");
        Message message = DiscordMock.message(guild, member, "message content");
        User user = member.getUser();
        long messageId = message.getIdLong();
        TextChannel channel = DiscordMock.textChannel(guild, 4L, "channel");
        GuildMessageReceivedEvent event = mock(GuildMessageReceivedEvent.class);
        when(event.getMember()).thenReturn(member);
        when(event.getMessage()).thenReturn(message);
        when(event.getAuthor()).thenReturn(user);
        when(event.getGuild()).thenReturn(guild);
        when(event.getMessageId()).thenReturn(String.valueOf(messageId));
        when(event.getMessageIdLong()).thenReturn(messageId);
        when(event.getChannel()).thenReturn(channel);
        return event;
    }

}
