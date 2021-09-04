package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.OptType;
import net.lindseybot.entities.interaction.commands.builder.CommandBuilder;
import net.lindseybot.enums.Modules;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.service.AuditService;
import net.notfab.lindsey.core.service.Messenger;
import org.springframework.stereotype.Component;

@Component
public class Hackban extends Command {

    private final Messenger msg;
    private final AuditService audit;

    public Hackban(Messenger msg, AuditService audit) {
        this.msg = msg;
        this.audit = audit;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("hackban", Label.of("commands.hackban.description"), 1.0)
            .module(Modules.MODERATION)
            .permission(PermissionLevel.ADMIN)
            .guilds(859946655310413844L)
            .addOption(OptType.LONG, "user", Label.of("commands.hackban.user"), true)
            .addOption(OptType.STRING, "reason", Label.of("commands.hackban.reason"), false)
            .build();
    }

    @BotCommand("hackban")
    public void onCommand(ServerCommandEvent event) {
        long target = event.getOptions().getLong("user");
        String reason;
        if (!event.getOptions().has("reason")) {
            reason = "Banned by " + this.getAsTag(event.getMember());
        } else {
            reason = event.getOptions().getString("reason");
        }
        event.getGuild().ban(String.valueOf(target), 7, reason)
            .queue((v) -> this.onBanned(event.getUnderlying(), target, reason));
        this.msg.reply(event, Label.raw("User banned."), true);
    }

    private void onBanned(SlashCommandEvent event, long target, String reason) {
        this.audit.builder().from(event)
            .message("logs.ban")
            .field("target_id", target).field("target", "External User#000")
            .field("delDays", 7)
            .field("reason", reason)
            .send();
    }

}
