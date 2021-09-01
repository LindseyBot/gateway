package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.OptType;
import net.lindseybot.entities.interaction.commands.builder.CommandBuilder;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.service.AuditService;
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
        return new CommandBuilder("hackban", Label.raw("Bans a user by id"))
            .permission(PermissionLevel.DEVELOPER)
            .addOption(OptType.LONG, "user", Label.raw("Target user's id"), true)
            .addOption(OptType.INT, "delDays", Label.raw("Days to delete messages"), false)
            .addOption(OptType.STRING, "reason", Label.raw("Reason for the ban"), false)
            .build();
    }

    @BotCommand("hackban")
    public void onCommand(ServerCommandEvent event) {
        long target = event.getOptions().getLong("user");
        int delDays = event.getOptions().getInt("delDays");
        String reason;
        if (!event.getOptions().has("reason")) {
            reason = "Banned by " + this.getAsTag(event.getMember());
        } else {
            reason = event.getOptions().getString("reason");
        }
        event.getGuild().ban(String.valueOf(target), delDays, reason)
            .queue((v) -> this.onBanned(event.getUnderlying(), target, delDays, reason));
        this.msg.reply(event, Label.raw("User banned."), true);
    }

    private void onBanned(SlashCommandEvent event, long target, int delDays, String reason) {
        this.audit.builder().from(event)
            .message("logs.ban")
            .field("target_id", target).field("target", "External User#000")
            .field("delDays", delDays)
            .field("reason", reason)
            .send();
    }

}
