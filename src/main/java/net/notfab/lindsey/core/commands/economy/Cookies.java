package net.notfab.lindsey.core.commands.economy;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.OptType;
import net.lindseybot.entities.interaction.commands.builder.CommandBuilder;
import net.lindseybot.entities.interaction.commands.builder.SubCommandBuilder;
import net.lindseybot.enums.Modules;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.service.EconomyService;
import net.notfab.lindsey.core.service.Messenger;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class Cookies extends Command {

    private final Messenger msg;
    private final ProfileManager profiles;
    private final EconomyService economy;

    public Cookies(Messenger msg, ProfileManager profiles, EconomyService economy) {
        this.msg = msg;
        this.profiles = profiles;
        this.economy = economy;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("cookies", Label.of("commands.cookies.description"))
            .permission(PermissionLevel.EVERYONE)
            .module(Modules.ECONOMY)
            .addSubcommand(
                new SubCommandBuilder("daily", Label.of("commands.cookies.daily.description"))
                    .ephemeral()
                    .build())
            .addSubcommand(
                new SubCommandBuilder("send", Label.of("commands.cookies.send.description"))
                    .ephemeral()
                    .addOption(OptType.USER, "target", Label.of("commands.cookies.send.target"), true)
                    .addOption(OptType.INT, "amount", Label.of("commands.cookies.send.amount"), true)
                    .build())
            .build();
    }

    @BotCommand("cookies/daily")
    public void onDaily(ServerCommandEvent event) {
        Member member = event.getMember();
        UserProfile profile = this.profiles.get(member.getUser());
        if (this.isSameDay(profile.getLastDailyCookies(), System.currentTimeMillis())) {
            long next = Instant.ofEpochMilli(System.currentTimeMillis())
                .truncatedTo(ChronoUnit.DAYS)
                .plus(1, ChronoUnit.DAYS)
                .getEpochSecond();
            this.msg.reply(event, Label.of("commands.cookies.daily.fail", "<t:" + next + ":R>"), true);
            return;
        }
        long streak;
        if (isStreak(profile.getLastDailyCookies())) {
            streak = profile.getCookieStreak() + 1;
        } else {
            streak = 1L;
        }
        profile.setCookieStreak(streak);
        profile.setLastDailyCookies(System.currentTimeMillis());
        profiles.save(profile);
        economy.pay(member.getUser(), streak * 15);
        this.msg.reply(event, Label.of("commands.cookies.daily.received", streak * 15, streak), true);
    }

    private boolean isSameDay(long one, long two) {
        return Instant.ofEpochMilli(one).truncatedTo(ChronoUnit.DAYS)
            .equals(Instant.ofEpochMilli(two).truncatedTo(ChronoUnit.DAYS));
    }

    private boolean isStreak(long last) {
        return Instant.ofEpochMilli(last).truncatedTo(ChronoUnit.DAYS)
            .equals(Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS));
    }

    @BotCommand("cookies/send")
    public void onSend(ServerCommandEvent event) {
        Member target = event.getOptions().getMember("target");
        if (target == null) {
            this.msg.reply(event, Label.of("search.member"), true);
            return;
        }
        long amount = event.getOptions().getLong("amount");
        if (amount <= 0) {
            this.msg.reply(event, Label.of("commands.cookies.send.invalid"), true);
            return;
        }
        Member self = event.getMember();
        if (!this.economy.has(self, amount)) {
            this.msg.reply(event, Label.of("economy.not_enough"), true);
            return;
        } else if (self.equals(target)) {
            this.msg.reply(event, Label.of("validation.self"), true);
            return;
        }
        this.economy.pay(self, -amount);
        this.economy.pay(target, amount);
        this.msg.reply(event, Label.of("commands.cookies.send.sent", self.getAsMention(), amount, target.getAsMention()), false);
    }

}

