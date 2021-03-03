package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.entities.profile.member.Strike;
import net.notfab.lindsey.shared.entities.profile.server.AntiAd;
import net.notfab.lindsey.shared.repositories.sql.StrikeRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class AntiAdListener extends ListenerAdapter {

    private final Snowflake snowflake;
    private final ProfileManager profiles;
    private final StrikeRepository strikeRepository;
    private final Translator i18n;
    private final Set<String> officialInvites = new HashSet<>();

    public AntiAdListener(Lindsey lindsey, Snowflake snowflake, ProfileManager profiles,
                          StrikeRepository strikeRepository, Translator i18n) {
        lindsey.addEventListener(this);
        this.snowflake = snowflake;
        this.profiles = profiles;
        this.strikeRepository = strikeRepository;
        this.i18n = i18n;
        this.officialInvites.add("discord-api");
        this.officialInvites.add("hypesquad");
        this.officialInvites.add("discord-townhall");
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getInvites().isEmpty()) {
            return;
        }
        if (event.isWebhookMessage() || event.getAuthor().isBot()) {
            return;
        }
        //noinspection ConstantConditions
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }
        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            return;
        }
        ServerProfile profile = profiles.get(event.getGuild());
        if (profile.getAutoMod() == null) {
            return;
        }
        AntiAd settings = profile.getAutoMod()
            .getAntiAd();
        if (!settings.isEnabled()) {
            return;
        }
        boolean found = false;
        for (String inviteCode : message.getInvites()) {
            Invite invite;
            try {
                invite = Invite.resolve(event.getJDA(), inviteCode).complete();
            } catch (ErrorResponseException ex) {
                return;
            }
            if (!this.isOffense(invite, event.getGuild())) {
                continue;
            }
            found = true;
        }
        if (!found) {
            return;
        }

        Strike strike = new Strike();
        strike.setId(snowflake.next());
        strike.setGuild(event.getGuild().getIdLong());
        strike.setUser(event.getMember().getIdLong());
        strike.setAdmin(event.getGuild().getSelfMember().getIdLong());
        strike.setCount(settings.getStrikes());
        strike.setReason(i18n.get(event.getGuild(), "automod.antiad.reason"));
        this.strikeRepository.save(strike);

        event.getMessage().delete()
            .reason("Advertising")
            .flatMap(aVoid -> event.getChannel()
                .sendMessage(i18n.get(event.getGuild(), "automod.antiad.warn", event.getMember().getEffectiveName())))
            .delay(10, TimeUnit.SECONDS)
            .flatMap(Message::delete)
            .queue(Utils.noop(), Utils.noop());
    }

    private boolean isOffense(Invite invite, Guild guild) {
        if (invite == null || invite.getType() != Invite.InviteType.GUILD) {
            return false;
        }
        //noinspection ConstantConditions
        if (guild.getId().equals(invite.getGuild().getId())) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (this.officialInvites.contains(invite.getCode())) {
            return false;
        }
        return true;
    }

}
