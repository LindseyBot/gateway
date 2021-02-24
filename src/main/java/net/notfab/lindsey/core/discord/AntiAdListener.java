package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.MemberProfile;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.entities.profile.server.AntiAd;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AntiAdListener extends ListenerAdapter {

    private final ProfileManager profiles;
    private final Translator i18n;
    private final Set<String> officialInvites = new HashSet<>();

    public AntiAdListener(Lindsey lindsey, ProfileManager profiles, Translator i18n) {
        lindsey.addEventListener(this);
        this.profiles = profiles;
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
        MemberProfile memberProfile = this.profiles.get(event.getMember());
        memberProfile.setStrikes(memberProfile.getStrikes() + 1);
        this.profiles.save(memberProfile);
        if (settings.isBan() && memberProfile.getStrikes() >= settings.getStrikes()) {
            event.getMember().ban(7, "Advertising")
                .flatMap(aVoid -> event.getChannel()
                    .sendMessage(i18n.get(event.getGuild(), "automod.antiad.ban", event.getMember().getEffectiveName())))
                .map(msg -> {
                    if (event.getGuild().getId().equals("141555945586163712")) {
                        return msg.addReaction(Emotes.CLOWN.asReaction());
                    } else {
                        return msg;
                    }
                })
                .queue();
        } else {
            event.getMessage().delete()
                .reason("Advertising")
                .flatMap(aVoid -> event.getChannel()
                    .sendMessage(i18n.get(event.getGuild(), "automod.antiad.warn", event.getMember().getEffectiveName())))
                .queue();
        }
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
