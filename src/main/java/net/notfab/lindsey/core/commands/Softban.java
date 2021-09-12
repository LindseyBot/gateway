package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.OptType;
import net.lindseybot.entities.interaction.commands.builder.CommandBuilder;
import net.lindseybot.enums.Modules;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.service.AuditService;
import net.notfab.lindsey.core.service.Messenger;
import org.springframework.stereotype.Component;

@Component
public class Softban extends Command {

    private final Messenger msg;
    private final AuditService audit;

    public Softban(Messenger msg, AuditService audit) {
        this.msg = msg;
        this.audit = audit;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("softban", Label.of("commands.softban.description"), 1.0)
            .module(Modules.MODERATION)
            .privileged()
            .guilds(859946655310413844L)
            .addOption(OptType.MEMBER, "user", Label.of("commands.softban.user"), true)
            .addOption(OptType.STRING, "reason", Label.of("commands.softban.reason"), false)
            .build();
    }

    @BotCommand("softban")
    public void onCommand(ServerCommandEvent event) {
        Member member = event.getOptions().getMember("user");
        if (member == null) {
            this.msg.reply(event, Label.of("search.member"), true);
            return;
        }
        String reason;
        if (!event.getOptions().has("reason")) {
            reason = "Banned by " + this.getAsTag(event.getMember());
        } else {
            reason = event.getOptions().getString("reason");
        }
        event.getGuild().ban(member, 7, reason).queue((v) -> {
            this.onBanned(event.getUnderlying(), member, reason);
            event.getGuild().unban(member.getId())
                .reason(reason)
                .queue();
        });
        this.msg.reply(event, Label.of("commands.softban.banned"), true);
    }

    private void onBanned(SlashCommandEvent event, Member target, String reason) {
        this.audit.builder().from(event)
            .message("logs.softban")
            .target(target)
            .field("delDays", 7)
            .field("reason", reason)
            .send();
    }

}
