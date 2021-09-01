package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class Ban extends Command {

    private final Messenger msg;
    private final AuditService audit;

    public Ban(Messenger msg, AuditService audit) {
        this.msg = msg;
        this.audit = audit;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("ban", Label.raw("Bans a user"))
            .permission(PermissionLevel.DEVELOPER)
            .addOption(OptType.USER, "user", Label.raw("Target user to ban"), true)
            .addOption(OptType.INT, "delDays", Label.raw("Days to delete messages"), false)
            .addOption(OptType.STRING, "reason", Label.raw("Reason for the ban"), false)
            .build();
    }

    @BotCommand("ban")
    public void onCommand(@NotNull ServerCommandEvent event) {
        Member target = event.getOptions().getMember("user");
        if (target == null) {
            this.msg.reply(event, Label.raw("Unknown user"), true);
            return;
        }
        int delDays = event.getOptions().getInt("delDays");
        String reason;
        if (!event.getOptions().has("reason")) {
            reason = "Banned by " + this.getAsTag(event.getMember());
        } else {
            reason = event.getOptions().getString("reason");
        }
        target.ban(delDays, reason)
            .queue((v) -> this.onBanned(event.getUnderlying(), target, delDays, reason));
        this.msg.reply(event, Label.raw("User banned."), true);
    }

    private void onBanned(SlashCommandEvent event, Member member, int delDays, String reason) {
        this.audit.builder().from(event)
            .target(member).message("logs.ban")
            .field("delDays", delDays)
            .field("reason", reason)
            .send();
    }

}
