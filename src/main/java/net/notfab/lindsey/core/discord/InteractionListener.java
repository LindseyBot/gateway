package net.notfab.lindsey.core.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.interactions.InteractionHookImpl;
import net.dv8tion.jda.internal.interactions.InteractionImpl;
import net.lindseybot.discord.bridge.Action;
import net.lindseybot.discord.bridge.InteractionData;
import net.lindseybot.discord.bridge.InteractionResponse;
import net.lindseybot.discord.bridge.actions.*;
import net.lindseybot.models.RedisConsumer;
import net.lindseybot.services.MessagingService;
import net.notfab.lindsey.core.framework.DiscordAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class InteractionListener implements RedisConsumer<InteractionResponse> {

    private final DiscordAdapter adapter;
    private final ShardManager api;

    public InteractionListener(MessagingService messaging, DiscordAdapter adapter, ShardManager api) {
        this.adapter = adapter;
        this.api = api;
        messaging.addConsumer("Lindsey:Interactions", this);
    }

    @Override
    public Class<InteractionResponse> getTClass() {
        return InteractionResponse.class;
    }

    @Override
    public void onMessage(InteractionResponse message) {
        InteractionData data = message.getData();
        Guild guild = this.api.getGuildById(data.getGuildId());
        if (guild == null) {
            return;
        }
        Member member = guild.retrieveMemberById(data.getUserId())
            .complete();
        if (member == null) {
            return;
        }
        TextChannel channel = guild.getTextChannelById(data.getChannelId());
        if (channel == null) {
            return;
        }
        InteractionHook hook;
        if (message.getData().getToken() != null) {
            hook = new InteractionImpl(0L, data.getType().getKey(), data.getToken(), guild, member, member.getUser(), channel)
                .getHook();
            ((InteractionHookImpl) hook).ack();
            ((InteractionHookImpl) hook).ready();
        } else {
            hook = null;
        }
        RestAction<?> rest = null;
        for (Action action : message.getActions()) {
            RestAction<?> act = this.toRestAction(action, guild, member, hook);
            if (action instanceof WaitAction wait && rest != null) {
                rest = rest.delay(wait.getTime(), TimeUnit.MILLISECONDS);
            } else if (act == null) {
                Message msg = this.adapter.toMessage(net.lindseybot.discord.Message.of("internal.error"), member);
                if (hook != null) {
                    rest = hook.sendMessage(msg);
                } else {
                    rest = channel.sendMessage(msg);
                }
                break;
            } else if (rest == null) {
                rest = act;
            } else {
                rest = rest.flatMap(m -> act);
            }
        }
        if (rest == null) {
            log.warn("Interaction without any execution received");
            return;
        }
        rest.queue();
    }

    private RestAction<?> toRestAction(Action action, Guild guild, Member member, InteractionHook hook) {
        if (action instanceof AddRoleAction data) {
            Role role = guild.getRoleById(data.getRoleId());
            if (role == null) {
                return null;
            }
            return guild.addRoleToMember(data.getUserId(), role);
        } else if (action instanceof BanAction data) {
            return guild.ban(String.valueOf(data.getTargetId()), data.getDelDays(), data.getReason());
        } else if (action instanceof KickAction data) {
            return guild.kick(String.valueOf(data.getTargetId()), data.getReason());
        } else if (action instanceof MessageAction data) {
            Message message = this.adapter.toMessage(data.getMessage(), member);
            if (data.isEdit()) {
                if (hook != null) {
                    return hook.editMessageById(data.getTargetId(), message);
                } else {
                    TextChannel targetChannel = guild.getTextChannelById(data.getChannelId());
                    if (targetChannel == null) {
                        return null;
                    }
                    return targetChannel.editMessageById(data.getTargetId(), message);
                }
            } else {
                if (hook != null) {
                    return hook.sendMessage(message)
                        .setEphemeral(data.getMessage().isEphemeral());
                } else {
                    TextChannel targetChannel = guild.getTextChannelById(data.getChannelId());
                    if (targetChannel == null) {
                        return null;
                    }
                    return targetChannel.sendMessage(message);
                }
            }
        } else if (action instanceof DeleteMessageAction data) {
            if (hook != null) {
                return hook.deleteMessageById(data.getMessageId());
            }
            TextChannel targetChannel = guild.getTextChannelById(data.getChannelId());
            if (targetChannel == null) {
                return null;
            }
            return targetChannel.deleteMessageById(data.getMessageId());
        } else if (action instanceof RemoveRoleAction data) {
            Role role = guild.getRoleById(data.getRoleId());
            if (role == null) {
                return null;
            }
            return guild.removeRoleFromMember(data.getUserId(), role);
        } else if (action instanceof UnbanAction data) {
            return guild.unban(String.valueOf(data.getTargetId()));
        } else {
            return null;
        }
    }

}
