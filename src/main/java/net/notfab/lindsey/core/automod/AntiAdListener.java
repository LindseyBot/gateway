package net.notfab.lindsey.core.automod;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.lindseybot.enums.AutoModFeature;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.service.StrikeService;
import net.notfab.lindsey.shared.entities.profile.server.AntiAd;
import net.notfab.lindsey.shared.repositories.sql.server.AntiAdRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AntiAdListener implements AutoModerator {

    private final StrikeService strikes;
    private final AntiAdRepository repository;
    private final Translator i18n;
    private final Set<String> officialInvites = new HashSet<>();

    public AntiAdListener(StrikeService strikes, AntiAdRepository repository, Translator i18n) {
        this.strikes = strikes;
        this.repository = repository;
        this.i18n = i18n;
        this.officialInvites.add("hypesquad");
        this.officialInvites.add("discord-api");
        this.officialInvites.add("discord-townhall");
        this.officialInvites.add("discord-developers");
    }

    @Override
    public AutoModFeature feature() {
        return AutoModFeature.ANTI_AD;
    }

    @Override
    public boolean moderate(@NotNull Message message, @NotNull Member author) {
        if (message.getInvites().isEmpty()) {
            return false;
        }
        Guild guild = message.getGuild();
        AntiAd settings = this.repository.findById(guild.getIdLong())
            .orElse(new AntiAd());
        if (!settings.isEnabled()) {
            return false;
        }
        boolean found = false;
        for (String inviteCode : message.getInvites()) {
            Invite invite;
            try {
                invite = Invite.resolve(guild.getJDA(), inviteCode).complete();
            } catch (ErrorResponseException ex) {
                return false;
            }
            if (!this.isOffense(invite, guild)) {
                continue;
            }
            found = true;
            break;
        }
        if (!found) {
            return false;
        }
        this.strikes.strike(author, settings.getStrikes(), i18n.get(guild, "automod.antiad.reason"));
        message.delete()
            .reason("Advertising")
            .flatMap(aVoid -> message.getChannel()
                .sendMessage(i18n.get(guild, "automod.antiad.warn", author.getEffectiveName())))
            .delay(10, TimeUnit.SECONDS)
            .flatMap(Message::delete)
            .queue(Utils.noop(), Utils.noop());
        return true;
    }

    private boolean isOffense(Invite invite, Guild guild) {
        if (invite == null || invite.getType() != Invite.InviteType.GUILD
            || invite.getGuild() == null) {
            return false;
        }
        if (guild.getId().equals(invite.getGuild().getId())) {
            return false;
        }
        return !this.officialInvites.contains(invite.getCode());
    }

}
