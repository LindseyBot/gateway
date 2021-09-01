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
public class Kick extends Command {

    private final Messenger msg;
    private final AuditService audit;

    public Kick(Messenger msg, AuditService audit) {
        this.msg = msg;
        this.audit = audit;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("kick", Label.raw("Kicks a user"))
            .permission(PermissionLevel.DEVELOPER)
            .addOption(OptType.USER, "user", Label.raw("Target user to kick"), true)
            .addOption(OptType.STRING, "reason", Label.raw("Reason for the kick"), false)
            .build();
    }

    @BotCommand("kick")
    public void onCommand(@NotNull ServerCommandEvent event) {
        Member target = event.getOptions().getMember("user");
        if (target == null) {
            this.msg.reply(event, Label.raw("Unknown user"), true);
            return;
        }
        String reason;
        if (!event.getOptions().has("reason")) {
            reason = "Kicked by " + this.getAsTag(event.getMember());
        } else {
            reason = event.getOptions().getString("reason");
        }
        target.kick(reason)
            .queue((v) -> this.onBanned(event.getUnderlying(), target, reason));
        this.msg.reply(event, Label.raw("User banned."), true);
    }

    private void onBanned(SlashCommandEvent event, Member member, String reason) {
        this.audit.builder().from(event)
            .target(member).message("logs.kick")
            .field("reason", reason)
            .send();
    }

}
